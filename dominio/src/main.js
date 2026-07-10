import { PLAYER_IDS } from './types.js';
import { GameState } from './gameState.js';
import { DOMManager } from './domManager.js';
import { RandomAI, GreedyAI } from './aiEngine.js';
import { fetchBoardConfigs, fetchRanking } from './apiClient.js';
import {
  savePlayerName,
  loadPlayerName,
  saveRankingEntry,
  loadRanking,
  seedRankingIfEmpty,
} from './storageManager.js';

const delay = ms => new Promise(res => setTimeout(res, ms));

function waitForTurnAdvance(gs) {
  return new Promise(resolve => {
    const done = () => {
      gs.removeEventListener('turn-ended', done);
      gs.removeEventListener('game-over',  done);
      resolve();
    };
    gs.addEventListener('turn-ended', done);
    gs.addEventListener('game-over',  done);
  });
}

async function runGameLoop(gs, dm, ai) {
  while (!gs.getState().gameOver) {
    gs.beginTurn();
    const { currentPlayer } = gs.getState();

    if (currentPlayer === PLAYER_IDS.AI) {
      await delay(300);
      if (!gs.getState().gameOver) {
        const action = ai.selectMove(gs.getState());
        if (action) gs.applyAction(action);
      }
      await dm.flushAnimations();
    } else {
      await waitForTurnAdvance(gs);
      await dm.flushAnimations();
    }
  }
}

function renderRankingTable() {
  const entries = loadRanking().slice(0, 10);
  const tbody   = document.getElementById('ranking-body');
  tbody.innerHTML = '';

  if (entries.length === 0) {
    const tr = document.createElement('tr');
    tr.innerHTML = `<td colspan="5" class="py-4 text-center text-slate-500">Sin partidas registradas aún.</td>`;
    tbody.appendChild(tr);
    return;
  }

  entries.forEach((e, i) => {
    const icon = e.winner === 'player' ? '🏆' : e.winner === 'ai' ? '🤖' : '🤝';
    const date = new Date(e.date).toLocaleDateString('es-AR', { day: '2-digit', month: '2-digit', year: '2-digit' });
    const tr   = document.createElement('tr');
    tr.className = 'hover:bg-slate-700/50 transition-colors';
    tr.innerHTML = `
      <td class="py-2 pr-2 text-slate-500 text-right">${i + 1}</td>
      <td class="py-2 font-medium">${e.playerName} <span aria-label="resultado">${icon}</span></td>
      <td class="py-2 text-right font-bold text-yellow-400">${e.playerScore}</td>
      <td class="py-2 text-right text-slate-400">${e.turns}</td>
      <td class="py-2 text-right text-slate-500 hidden sm:table-cell">${date}</td>
    `;
    tbody.appendChild(tr);
  });
}

function openRankingModal() {
  renderRankingTable();
  document.getElementById('ranking-modal').showModal();
}

function startGame({ boardConfig, playerName, ai }) {
  const gs = new GameState(boardConfig);
  const dm = new DOMManager(gs);

  document.getElementById('setup-screen').hidden = true;
  document.getElementById('game-screen').hidden  = false;

  dm.init(playerName);

  gs.addEventListener('game-over', ({ detail: { winner, scores } }) => {
    const { turn } = gs.getState();
    saveRankingEntry({
      id:          Date.now().toString(),
      playerName,
      playerScore: scores[PLAYER_IDS.HUMAN],
      aiScore:     scores[PLAYER_IDS.AI],
      turns:       turn,
      winner:      winner === PLAYER_IDS.HUMAN ? 'player' : winner === PLAYER_IDS.AI ? 'ai' : 'draw',
      date:        new Date().toISOString(),
    });
  }, { once: true });

  const ac         = new AbortController();
  const { signal } = ac;

  const cleanup = () => {
    ac.abort();
    dm.destroy();
    document.getElementById('gameover-overlay').hidden = true;
    document.getElementById('game-screen').hidden      = true;
    document.getElementById('setup-screen').hidden     = false;
  };

  document.addEventListener('dominio:restart', cleanup, { signal });
  document.addEventListener('dominio:quit',    cleanup, { signal });

  runGameLoop(gs, dm, ai);
}

async function main() {
  const nameInput = document.getElementById('player-name');
  nameInput.value = loadPlayerName('Jugador');
  nameInput.addEventListener('change', () => savePlayerName(nameInput.value.trim() || 'Jugador'));

  const startBtn    = document.getElementById('start-btn');
  const boardSelect = document.getElementById('board-select');

  startBtn.disabled = true;
  const { configs, error: boardError } = await fetchBoardConfigs();

  boardSelect.innerHTML = '';
  configs.forEach(cfg => {
    const opt       = document.createElement('option');
    opt.value       = cfg.id;
    opt.textContent = `${cfg.name} — ${cfg.description}`;
    boardSelect.appendChild(opt);
  });
  startBtn.disabled = false;

  if (boardError) {
    console.warn('[Dominio] Board fetch failed, using default:', boardError);
  }

  const { entries, error: rankingError } = await fetchRanking();
  if (rankingError) console.warn('[Dominio] Ranking fetch failed:', rankingError);
  seedRankingIfEmpty(entries);

  const modal    = document.getElementById('ranking-modal');
  const closeBtn = document.getElementById('close-ranking-btn');
  closeBtn.addEventListener('click', () => modal.close());
  modal.addEventListener('click', e => { if (e.target === modal) modal.close(); });
  document.getElementById('open-ranking-btn').addEventListener('click', openRankingModal);

  const form = document.getElementById('setup-form');
  form.addEventListener('submit', e => {
    e.preventDefault();
    const name        = nameInput.value.trim() || 'Jugador';
    const difficulty  = document.querySelector('input[name="difficulty"]:checked')?.value ?? 'easy';
    const configId    = boardSelect.value;
    const boardConfig = configs.find(c => c.id === configId) ?? configs[0];
    const ai          = difficulty === 'normal' ? GreedyAI : RandomAI;

    savePlayerName(name);
    startGame({ boardConfig, playerName: name, ai });
  });
}

main();
