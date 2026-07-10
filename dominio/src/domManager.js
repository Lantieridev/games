import { PLAYER_IDS, ACTION_TYPES } from './types.js';
import { animateMove, animateAttack, animateMutate } from './animations.js';
import { loadRanking } from './storageManager.js';

const TYPE_LABELS  = { rock: 'Roca', spine: 'Espina', mist: 'Bruma' };
const PIECE_LETTER = { rock: 'R', spine: 'E', mist: 'B' };

function cellBaseCls(row, col) {
  return (row + col) % 2 === 0
    ? 'bg-slate-600 hover:bg-slate-500'
    : 'bg-slate-700 hover:bg-slate-600';
}

function pieceCls(type, owner) {
  const SOLID = {
    rock:  'bg-amber-600  text-white',
    spine: 'bg-green-600  text-white',
    mist:  'bg-blue-600   text-white',
  };
  const OUTLINE = {
    rock:  'border-2 border-amber-400  text-amber-300',
    spine: 'border-2 border-green-400  text-green-300',
    mist:  'border-2 border-blue-400   text-blue-300',
  };
  return (owner === PLAYER_IDS.HUMAN ? SOLID : OUTLINE)[type] ?? '';
}

export class DOMManager {
  /**
   * @param {import('./gameState.js').GameState} gameState
   */
  constructor(gameState) {
    this._gs         = gameState;
    this._cellEls    = null;          // Array[8][8] of HTMLElement
    this._pieceEls   = new Map();     // pieceId → HTMLElement
    this._selected   = null;          // { row, col } | null
    this._validMoves = [];
    this._animChain  = Promise.resolve();
    this._gsHandlers = {};            // event name → bound handler (for cleanup)
  }

  // ── Public API ────────────────────────────────────────────────────

  /**
   * Mount the board, subscribe to gameState events, render initial pieces.
   * @param {string} playerName — shown in the player HUD
   */
  init(playerName = 'Jugador') {
    this._buildBoardGrid();
    this._subscribeToGameState();
    this._attachInputHandlers();
    this._renderInitialBoard(playerName);
    this._setupActionBar();
    this._setupRankingModal();
    this._setupGameButtons();
  }

  /** Remove all listeners attached during init. */
  destroy() {
    for (const [evt, handler] of Object.entries(this._gsHandlers)) {
      this._gs.removeEventListener(evt, handler);
    }
    const board = document.getElementById('board');
    board?.removeEventListener('click',   this._boardClickHandler);
    board?.removeEventListener('keydown', this._boardKeyHandler);
  }

  /**
   * Returns a Promise that resolves when all queued animations have settled.
   * Call this in the game loop after applyAction.
   */
  flushAnimations() {
    return this._animChain;
  }

  /** Opens the ranking modal (callable from main.js too). */
  openRankingModal() {
    this._renderRankingTable();
    document.getElementById('ranking-modal').showModal();
  }

  // ── Board grid ────────────────────────────────────────────────────

  _buildBoardGrid() {
    const boardEl = document.getElementById('board');
    boardEl.innerHTML = '';
    this._cellEls = Array.from({ length: 8 }, (_, r) =>
      Array.from({ length: 8 }, (_, c) => {
        const cell = document.createElement('div');
        cell.className = [
          'relative flex items-center justify-center',
          'transition-colors duration-100 outline-none',
          cellBaseCls(r, c),
        ].join(' ');
        cell.setAttribute('role',         'gridcell');
        cell.setAttribute('aria-rowindex', String(r + 1));
        cell.setAttribute('aria-colindex', String(c + 1));
        cell.setAttribute('aria-label',   `Fila ${r + 1}, columna ${c + 1}`);
        cell.setAttribute('tabindex',     '-1');
        cell.dataset.row = r;
        cell.dataset.col = c;
        boardEl.appendChild(cell);
        return cell;
      })
    );
    // First cell focusable by default
    this._cellEls[0][0].setAttribute('tabindex', '0');
  }

  _renderInitialBoard(playerName) {
    const { board, energy, scores } = this._gs.getState();
    for (let r = 0; r < 8; r++) {
      for (let c = 0; c < 8; c++) {
        if (board[r][c]) this._mountPiece(board[r][c], r, c);
      }
    }
    document.getElementById('player-hud-name').textContent = playerName;
    this._renderEnergyBar(PLAYER_IDS.HUMAN, energy.human);
    this._renderEnergyBar(PLAYER_IDS.AI,    energy.ai);
    this._updateScores(scores);
    this._refreshPieceCounts();
    this._updateTurnIndicator(PLAYER_IDS.HUMAN, 1);
  }

  // ── Piece elements ────────────────────────────────────────────────

  _mountPiece(piece, row, col) {
    const el = this._createPieceEl(piece);
    this._cellEls[row][col].appendChild(el);
    this._pieceEls.set(piece.id, el);
    this._updateCellAria({ row, col });
  }

  _createPieceEl(piece) {
    const el = document.createElement('div');
    el.className = [
      'w-4/5 h-4/5 rounded-full flex items-center justify-center',
      'font-bold text-sm select-none pointer-events-none',
      pieceCls(piece.type, piece.owner),
    ].join(' ');
    el.dataset.pieceId = piece.id;
    el.dataset.owner   = piece.owner;
    el.dataset.type    = piece.type;
    el.setAttribute('role',       'img');
    el.setAttribute('aria-label', this._pieceAriaLabel(piece));
    el.textContent = PIECE_LETTER[piece.type];
    return el;
  }

  _refreshPieceElClasses(pieceEl, piece) {
    pieceEl.className = [
      'w-4/5 h-4/5 rounded-full flex items-center justify-center',
      'font-bold text-sm select-none pointer-events-none',
      pieceCls(piece.type, piece.owner),
    ].join(' ');
    pieceEl.textContent = PIECE_LETTER[piece.type];
    pieceEl.dataset.type = piece.type;
    pieceEl.setAttribute('aria-label', this._pieceAriaLabel(piece));
  }

  _pieceAriaLabel(piece) {
    const owner = piece.owner === PLAYER_IDS.HUMAN ? 'tuya' : 'de la máquina';
    return `${TYPE_LABELS[piece.type]} ${owner}`;
  }

  // ── GameState event subscriptions ─────────────────────────────────

  _subscribeToGameState() {
    const bind = (evt, fn) => {
      const h = fn.bind(this);
      this._gsHandlers[evt] = h;
      this._gs.addEventListener(evt, h);
    };
    bind('piece-moved',    this._onPieceMoved);
    bind('piece-attacked', this._onPieceAttacked);
    bind('piece-mutated',  this._onPieceMutated);
    bind('energy-changed', this._onEnergyChanged);
    bind('score-updated',  this._onScoreUpdated);
    bind('turn-ended',     this._onTurnEnded);
    bind('game-over',      this._onGameOver);
    bind('action-invalid', this._onActionInvalid);
  }

  _onPieceMoved({ detail: { from, to, pieceId } }) {
    const pieceEl = this._pieceEls.get(pieceId);
    if (!pieceEl) return;
    this._cellEls[to.row][to.col].appendChild(pieceEl);
    this._queueAnimation(() => animateMove(pieceEl));
    this._updateCellAria(from);
    this._updateCellAria(to);
    this._refreshPieceCounts();
  }

  _onPieceAttacked({ detail: { attackerPos, defenderPos, attackerId, defenderId } }) {
    const attackerEl  = this._pieceEls.get(attackerId);
    const defenderEl  = this._pieceEls.get(defenderId);

    this._queueAnimation(async () => {
      await animateAttack(attackerEl);
      if (defenderEl) {
        defenderEl.remove();
        this._pieceEls.delete(defenderId);
      }
    });

    this._updateCellAria(attackerPos);
    this._updateCellAria(defenderPos);
    this._refreshPieceCounts();
  }

  _onPieceMutated({ detail: { pos, pieceId } }) {
    const pieceEl = this._pieceEls.get(pieceId);
    if (!pieceEl) return;

    this._queueAnimation(async () => {
      await animateMutate(pieceEl);
      const piece = this._gs.getState().board[pos.row][pos.col];
      if (piece) this._refreshPieceElClasses(pieceEl, piece);
    });

    this._updateCellAria(pos);
    this._refreshPieceCounts();
  }

  _onEnergyChanged({ detail: { owner, newEnergy } }) {
    this._renderEnergyBar(owner, newEnergy);
  }

  _onScoreUpdated() {
    this._updateScores(this._gs.getState().scores);
  }

  _onTurnEnded({ detail: { nextOwner } }) {
    this._clearSelection();
    const { turn } = this._gs.getState();
    this._updateTurnIndicator(nextOwner, turn + 1);
  }

  _onGameOver({ detail: { winner, scores } }) {
    this._clearSelection();
    this._showGameOverOverlay(winner, scores);
  }

  _onActionInvalid() {
    if (!this._selected) return;
    const cell = this._cellEls[this._selected.row][this._selected.col];
    cell.classList.add('cell-shake');
    cell.addEventListener('animationend', () => cell.classList.remove('cell-shake'), { once: true });
  }

  // ── Input handlers ────────────────────────────────────────────────

  _attachInputHandlers() {
    this._boardClickHandler = this._handleBoardClick.bind(this);
    this._boardKeyHandler   = this._handleBoardKeydown.bind(this);
    const board = document.getElementById('board');
    board.addEventListener('click',   this._boardClickHandler);
    board.addEventListener('keydown', this._boardKeyHandler);
  }

  _handleBoardClick(e) {
    const state = this._gs.getState();
    if (state.currentPlayer !== PLAYER_IDS.HUMAN || state.gameOver) return;

    const cellEl = e.target.closest('[data-row]');
    if (!cellEl) return;

    const row = +cellEl.dataset.row;
    const col = +cellEl.dataset.col;

    if (!this._selected) {
      this._trySelect({ row, col });
      return;
    }

    // Check if clicked cell is a valid move/attack target
    const matched = this._validMoves.find(m => m.to?.row === row && m.to?.col === col);
    if (matched) {
      this._clearSelection();
      this._gs.applyAction(matched);
      return;
    }

    // Clicking own piece → reselect
    if (state.board[row][col]?.owner === PLAYER_IDS.HUMAN) {
      this._trySelect({ row, col });
    } else {
      this._clearSelection();
    }
  }

  _handleBoardKeydown(e) {
    const state = this._gs.getState();
    if (state.gameOver) return;

    if (e.key === 'Escape') { this._clearSelection(); return; }

    const DIRS = { ArrowUp: [-1,0], ArrowDown: [1,0], ArrowLeft: [0,-1], ArrowRight: [0,1] };
    if (DIRS[e.key]) {
      e.preventDefault();
      const focused = document.activeElement;
      if (focused?.dataset?.row === undefined) return;
      const r = +focused.dataset.row;
      const c = +focused.dataset.col;
      const [dr, dc] = DIRS[e.key];
      const nr = Math.max(0, Math.min(7, r + dr));
      const nc = Math.max(0, Math.min(7, c + dc));
      this._cellEls[nr][nc].focus();
      return;
    }

    if ((e.key === 'Enter' || e.key === ' ') && document.activeElement?.dataset?.row !== undefined) {
      e.preventDefault();
      document.activeElement.click();
    }
  }

  // ── Selection ─────────────────────────────────────────────────────

  _trySelect(pos) {
    const { board } = this._gs.getState();
    if (board[pos.row][pos.col]?.owner !== PLAYER_IDS.HUMAN) return;
    this._clearSelection();
    this._selected   = pos;
    this._validMoves = this._gs.getLegalMoves(pos);
    this._cellEls[pos.row][pos.col].classList.add('cell-selected');
    for (const m of this._validMoves) {
      if (!m.to) continue;
      const cls = m.type === ACTION_TYPES.ATTACK ? 'cell-valid-atk' : 'cell-valid-move';
      this._cellEls[m.to.row][m.to.col].classList.add(cls);
    }
    this._syncActionBar();
  }

  _clearSelection() {
    if (this._selected) {
      this._cellEls[this._selected.row][this._selected.col].classList.remove('cell-selected');
    }
    for (const m of this._validMoves) {
      if (m.to) this._cellEls[m.to.row][m.to.col].classList.remove('cell-valid-move', 'cell-valid-atk');
    }
    this._selected   = null;
    this._validMoves = [];
    this._syncActionBar();
  }

  // ── Action bar ────────────────────────────────────────────────────

  _setupActionBar() {
    document.getElementById('mutate-btn').addEventListener('click', () => {
      const mutate = this._validMoves.find(m => m.type === ACTION_TYPES.MUTATE);
      if (mutate) { this._clearSelection(); this._gs.applyAction(mutate); }
    });
    document.getElementById('cancel-selection-btn').addEventListener('click', () => this._clearSelection());
  }

  _syncActionBar() {
    const canMutate    = this._validMoves.some(m => m.type === ACTION_TYPES.MUTATE);
    const hasSelection = this._selected !== null;
    document.getElementById('mutate-btn').hidden           = !canMutate;
    document.getElementById('cancel-selection-btn').hidden = !hasSelection;
  }

  // ── HUD ───────────────────────────────────────────────────────────

  _renderEnergyBar(owner, energy) {
    const id = owner === PLAYER_IDS.HUMAN ? 'player-energy-bar' : 'ai-energy-bar';
    const bar = document.getElementById(id);
    if (!bar) return;
    bar.innerHTML = '';
    bar.setAttribute('aria-valuenow', energy);
    for (let i = 0; i < 10; i++) {
      const pip = document.createElement('span');
      pip.className = `inline-block w-2 h-3 rounded-sm ${i < energy ? 'bg-yellow-400' : 'bg-slate-600'}`;
      pip.setAttribute('aria-hidden', 'true');
      bar.appendChild(pip);
    }
  }

  _updateScores(scores) {
    const ps = document.getElementById('player-score');
    const as = document.getElementById('ai-score');
    if (ps) ps.textContent = scores.human;
    if (as) as.textContent = scores.ai;
  }

  _refreshPieceCounts() {
    const { board } = this._gs.getState();
    for (const owner of [PLAYER_IDS.HUMAN, PLAYER_IDS.AI]) {
      const pfx = owner === PLAYER_IDS.HUMAN ? 'player' : 'ai';
      let rock = 0, spine = 0, mist = 0;
      for (let r = 0; r < 8; r++) {
        for (let c = 0; c < 8; c++) {
          const p = board[r][c];
          if (!p || p.owner !== owner) continue;
          if      (p.type === 'rock')  rock++;
          else if (p.type === 'spine') spine++;
          else                          mist++;
        }
      }
      document.getElementById(`${pfx}-pieces`).textContent      = rock + spine + mist;
      document.getElementById(`${pfx}-rock-count`).textContent   = rock;
      document.getElementById(`${pfx}-spine-count`).textContent  = spine;
      document.getElementById(`${pfx}-mist-count`).textContent   = mist;
    }
  }

  _updateTurnIndicator(player, turnNumber) {
    const label = document.getElementById('turn-player-label');
    if (player === PLAYER_IDS.HUMAN) {
      label.textContent = 'Tu turno';
      label.className   = 'text-yellow-400';
    } else {
      label.textContent = 'Máquina…';
      label.className   = 'text-slate-400 animate-pulse';
    }
    document.getElementById('turn-counter').textContent = Math.min(turnNumber, 100);
  }

  _updateCellAria(pos) {
    const cell  = this._cellEls[pos.row][pos.col];
    const piece = this._gs.getState().board[pos.row][pos.col];
    const desc  = piece
      ? ` — ${TYPE_LABELS[piece.type]} ${piece.owner === PLAYER_IDS.HUMAN ? '(tuya)' : '(máquina)'}`
      : '';
    cell.setAttribute('aria-label', `Fila ${pos.row + 1}, columna ${pos.col + 1}${desc}`);
  }

  // ── Ranking modal ─────────────────────────────────────────────────

  _setupRankingModal() {
    const modal = document.getElementById('ranking-modal');
    document.getElementById('close-ranking-btn').addEventListener('click', () => modal.close());
    document.getElementById('ranking-btn-game')?.addEventListener('click', () => this.openRankingModal());
    modal.addEventListener('click', e => { if (e.target === modal) modal.close(); });
    // Close on Escape (native for <dialog>)
  }

  _renderRankingTable() {
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
      const tr = document.createElement('tr');
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

  // ── Game over overlay ─────────────────────────────────────────────

  _showGameOverOverlay(winner, scores) {
    const MAP = {
      human: { emoji: '🏆', title: '¡Ganaste!',  desc: 'Dominaste el tablero.' },
      ai:    { emoji: '😞', title: 'Perdiste',    desc: 'La máquina controló el tablero.' },
      draw:  { emoji: '🤝', title: 'Empate',      desc: 'Nadie pudo dominar el tablero.' },
    };
    const { emoji, title, desc } = MAP[winner] ?? MAP.draw;

    document.getElementById('gameover-emoji').textContent = emoji;
    document.getElementById('gameover-title').textContent = title;
    document.getElementById('gameover-desc').textContent  = desc;
    document.getElementById('gameover-scores').innerHTML  = `
      <div class="flex justify-between">
        <span>Tu puntaje</span>
        <span class="font-bold text-yellow-400">${scores.human}</span>
      </div>
      <div class="flex justify-between">
        <span>Máquina</span>
        <span class="font-bold text-slate-300">${scores.ai}</span>
      </div>
    `;
    document.getElementById('gameover-overlay').hidden = false;
  }

  _setupGameButtons() {
    document.getElementById('play-again-btn')?.addEventListener('click', () => {
      document.getElementById('gameover-overlay').hidden = true;
      document.dispatchEvent(new CustomEvent('dominio:restart'));
    });

    document.getElementById('gameover-ranking-btn')?.addEventListener('click', () => {
      document.getElementById('gameover-overlay').hidden = true;
      this.openRankingModal();
    });

    document.getElementById('quit-btn')?.addEventListener('click', () => {
      document.dispatchEvent(new CustomEvent('dominio:quit'));
    });
  }

  // ── Animation queue ───────────────────────────────────────────────

  _queueAnimation(fn) {
    this._animChain = this._animChain.then(fn).catch(() => {});
  }
}
