import { describe, it, expect, beforeEach } from 'vitest';
import { DOMManager } from '../src/domManager.js';
import { GameState } from '../src/gameState.js';
import { ACTION_TYPES, PLAYER_IDS } from '../src/types.js';
import { saveRankingEntry } from '../src/storageManager.js';
import { mountFixture } from './helpers/domFixture.js';

const CLASSIC = {
  playerLayout: ['R', 'R', 'R', 'E', 'E', 'E', 'B', 'B'],
  aiLayout:     ['R', 'R', 'R', 'E', 'E', 'E', 'B', 'B'],
};

function cell(row, col) {
  return document.querySelector(`#board [data-row="${row}"][data-col="${col}"]`);
}

/** Resolves every currently-pending animation by firing animationend on animated elements. */
function settleAnimations() {
  document.querySelectorAll('.piece-anim-move, .piece-anim-attack, .piece-anim-mutate')
    .forEach(el => el.dispatchEvent(new Event('animationend')));
}

beforeEach(() => {
  localStorage.clear();
  mountFixture();
});

describe('DOMManager.init', () => {
  it('builds a 64-cell grid and mounts all 16 starting pieces', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init('Martín');

    expect(document.querySelectorAll('#board [role="gridcell"]')).toHaveLength(64);
    expect(document.querySelectorAll('#board [data-piece-id]')).toHaveLength(16);
    expect(document.getElementById('player-hud-name').textContent).toBe('Martín');
    expect(document.getElementById('player-pieces').textContent).toBe('8');
    expect(document.getElementById('ai-pieces').textContent).toBe('8');
    expect(document.getElementById('turn-player-label').textContent).toBe('Tu turno');
    expect(document.getElementById('turn-counter').textContent).toBe('1');
  });

  it('defaults the player HUD name when none is given', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    expect(document.getElementById('player-hud-name').textContent).toBe('Jugador');
  });
});

describe('board click interaction', () => {
  it('selecting a piece highlights it and its legal targets', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();

    cell(0, 0).dispatchEvent(new MouseEvent('click', { bubbles: true }));

    expect(cell(0, 0).classList.contains('cell-selected')).toBe(true);
    expect(cell(1, 0).classList.contains('cell-valid-move')).toBe(true);
  });

  it('clicking a valid move target moves the piece and clears the selection', async () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();

    cell(0, 0).dispatchEvent(new MouseEvent('click', { bubbles: true }));
    cell(1, 0).dispatchEvent(new MouseEvent('click', { bubbles: true }));
    settleAnimations();
    await dm.flushAnimations();

    expect(cell(0, 0).classList.contains('cell-selected')).toBe(false);
    expect(cell(1, 0).querySelector('[data-piece-id]')).not.toBeNull();
    expect(gs.getState().currentPlayer).toBe(PLAYER_IDS.AI);
  });

  it('clicking a different own piece reselects instead of deselecting', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();

    cell(0, 0).dispatchEvent(new MouseEvent('click', { bubbles: true }));
    cell(0, 1).dispatchEvent(new MouseEvent('click', { bubbles: true }));

    expect(cell(0, 0).classList.contains('cell-selected')).toBe(false);
    expect(cell(0, 1).classList.contains('cell-selected')).toBe(true);
  });

  it('clicking an invalid target clears the selection', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();

    cell(0, 0).dispatchEvent(new MouseEvent('click', { bubbles: true }));
    cell(4, 4).dispatchEvent(new MouseEvent('click', { bubbles: true })); // empty, not adjacent

    expect(cell(0, 0).classList.contains('cell-selected')).toBe(false);
  });

  it('ignores clicks when it is not the human turn or the game is over', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    gs._currentPlayer = PLAYER_IDS.AI;

    cell(0, 0).dispatchEvent(new MouseEvent('click', { bubbles: true }));
    expect(cell(0, 0).classList.contains('cell-selected')).toBe(false);
  });

  it('clicking an empty cell with no prior selection selects nothing', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    cell(3, 3).dispatchEvent(new MouseEvent('click', { bubbles: true }));
    expect(cell(3, 3).classList.contains('cell-selected')).toBe(false);
    expect(document.getElementById('mutate-btn').hidden).toBe(true);
  });

  it('ignores clicks outside any cell', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    expect(() => document.getElementById('board').dispatchEvent(new MouseEvent('click', { bubbles: true }))).not.toThrow();
  });

  it('an attack removes the defender element from the DOM', async () => {
    const gs = new GameState({ playerLayout: [], aiLayout: [] });
    gs._board[3][3] = { id: 'atk', owner: PLAYER_IDS.HUMAN, type: 'rock' };
    gs._board[3][4] = { id: 'def', owner: PLAYER_IDS.AI, type: 'mist' };
    gs._board[6][6] = { id: 'safe', owner: PLAYER_IDS.AI, type: 'rock' };
    const dm = new DOMManager(gs);
    dm.init();

    cell(3, 3).dispatchEvent(new MouseEvent('click', { bubbles: true }));
    cell(3, 4).dispatchEvent(new MouseEvent('click', { bubbles: true }));
    settleAnimations();
    await dm.flushAnimations();

    expect(cell(3, 4).querySelector('[data-piece-id]')).toBeNull();
    expect(document.getElementById('player-score').textContent).toBe('10');
  });
});

describe('keyboard interaction', () => {
  it('arrow keys move focus within board bounds', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();

    cell(0, 0).focus();
    cell(0, 0).dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowRight', bubbles: true }));
    expect(document.activeElement).toBe(cell(0, 1));

    // Clamped at the top edge.
    cell(0, 1).dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowUp', bubbles: true }));
    expect(document.activeElement).toBe(cell(0, 1));
  });

  it('Escape clears the current selection', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    cell(0, 0).dispatchEvent(new MouseEvent('click', { bubbles: true }));
    cell(0, 0).dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true }));
    expect(cell(0, 0).classList.contains('cell-selected')).toBe(false);
  });

  it('Enter/Space activates the focused cell as a click', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    cell(0, 0).focus();
    cell(0, 0).dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }));
    expect(cell(0, 0).classList.contains('cell-selected')).toBe(true);
  });

  it('ignores unrelated keys entirely', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    cell(0, 0).focus();
    expect(() =>
      cell(0, 0).dispatchEvent(new KeyboardEvent('keydown', { key: 'x', bubbles: true }))
    ).not.toThrow();
    expect(cell(0, 0).classList.contains('cell-selected')).toBe(false);
  });

  it('ignores keydowns once the game is over', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    gs._gameOver = true;
    cell(0, 0).focus();
    expect(() =>
      cell(0, 0).dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowRight', bubbles: true }))
    ).not.toThrow();
  });

  it('ignores arrow keys when focus is outside the board cells', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    document.getElementById('quit-btn').focus();
    const board = document.getElementById('board');
    expect(() =>
      board.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowRight', bubbles: true }))
    ).not.toThrow();
    expect(document.activeElement).toBe(document.getElementById('quit-btn'));
  });

  it('ignores Enter/Space when focus is outside the board cells', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    document.getElementById('quit-btn').focus();
    const board = document.getElementById('board');
    expect(() =>
      board.dispatchEvent(new KeyboardEvent('keydown', { key: 'Enter', bubbles: true }))
    ).not.toThrow();
  });
});

describe('mutate / cancel action bar', () => {
  it('mutate button applies the mutation and hides itself again', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();

    cell(0, 0).dispatchEvent(new MouseEvent('click', { bubbles: true }));
    expect(document.getElementById('mutate-btn').hidden).toBe(false);

    document.getElementById('mutate-btn').dispatchEvent(new MouseEvent('click', { bubbles: true }));
    expect(gs.getState().board[0][0].type).toBe('spine');
    expect(document.getElementById('mutate-btn').hidden).toBe(true);
  });

  it('cancel button clears the selection', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    cell(0, 0).dispatchEvent(new MouseEvent('click', { bubbles: true }));
    document.getElementById('cancel-selection-btn').dispatchEvent(new MouseEvent('click', { bubbles: true }));
    expect(cell(0, 0).classList.contains('cell-selected')).toBe(false);
  });

  it('does nothing if mutate is clicked with no valid mutate action', () => {
    const gs = new GameState(CLASSIC);
    gs._energy.human = 0;
    const dm = new DOMManager(gs);
    dm.init();
    cell(0, 0).dispatchEvent(new MouseEvent('click', { bubbles: true }));
    expect(() =>
      document.getElementById('mutate-btn').dispatchEvent(new MouseEvent('click', { bubbles: true }))
    ).not.toThrow();
  });
});

describe('invalid action feedback', () => {
  it('shakes the selected cell on an invalid action and cleans up after the animation', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    cell(0, 0).dispatchEvent(new MouseEvent('click', { bubbles: true }));

    gs._emit('action-invalid', { reason: 'illegal-move' });
    expect(cell(0, 0).classList.contains('cell-shake')).toBe(true);
    cell(0, 0).dispatchEvent(new Event('animationend'));
    expect(cell(0, 0).classList.contains('cell-shake')).toBe(false);
  });

  it('does nothing when no cell is selected', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    expect(() => gs._emit('action-invalid', { reason: 'not-your-piece' })).not.toThrow();
  });
});

describe('ranking modal', () => {
  it('renders "no games" message when ranking is empty', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    dm.openRankingModal();
    expect(document.getElementById('ranking-body').textContent).toContain('Sin partidas registradas');
    expect(document.getElementById('ranking-modal').open).toBe(true);
  });

  it('renders ranking rows sorted by score with win/loss/draw icons', () => {
    saveRankingEntry({ playerName: 'A', playerScore: 50, turns: 12, winner: 'player', date: new Date().toISOString() });
    saveRankingEntry({ playerName: 'B', playerScore: 30, turns: 20, winner: 'ai', date: new Date().toISOString() });
    saveRankingEntry({ playerName: 'C', playerScore: 10, turns: 5, winner: 'draw', date: new Date().toISOString() });

    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    dm.openRankingModal();

    const rows = document.querySelectorAll('#ranking-body tr');
    expect(rows).toHaveLength(3);
    expect(rows[0].textContent).toContain('A');
    expect(rows[0].textContent).toContain('🏆');
  });

  it('close button and backdrop click close the modal', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    dm.openRankingModal();

    document.getElementById('close-ranking-btn').dispatchEvent(new MouseEvent('click', { bubbles: true }));
    expect(document.getElementById('ranking-modal').open).toBe(false);

    dm.openRankingModal();
    const modal = document.getElementById('ranking-modal');
    modal.dispatchEvent(Object.assign(new MouseEvent('click', { bubbles: true }), {}));
    // Simulate a genuine backdrop click: event target is the modal itself.
    const evt = new MouseEvent('click', { bubbles: true });
    Object.defineProperty(evt, 'target', { value: modal });
    modal.dispatchEvent(evt);
    expect(modal.open).toBe(false);
  });

  it('the in-game ranking button opens the modal', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    document.getElementById('ranking-btn-game').dispatchEvent(new MouseEvent('click', { bubbles: true }));
    expect(document.getElementById('ranking-modal').open).toBe(true);
  });
});

describe('game-over overlay', () => {
  it('shows the win overlay with final scores', () => {
    const gs = new GameState({ playerLayout: [], aiLayout: [] });
    gs._board[3][3] = { id: 'atk', owner: PLAYER_IDS.HUMAN, type: 'rock' };
    gs._board[3][4] = { id: 'def', owner: PLAYER_IDS.AI, type: 'mist' };
    const dm = new DOMManager(gs);
    dm.init();

    gs.applyAction({ type: ACTION_TYPES.ATTACK, from: { row: 3, col: 3 }, to: { row: 3, col: 4 } });

    expect(document.getElementById('gameover-overlay').hidden).toBe(false);
    expect(document.getElementById('gameover-title').textContent).toBe('¡Ganaste!');
    expect(document.getElementById('gameover-scores').textContent).toContain('10');
  });

  it('shows the draw overlay copy for the draw winner', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    gs._emit('game-over', { winner: 'draw', scores: { human: 5, ai: 5 } });
    expect(document.getElementById('gameover-title').textContent).toBe('Empate');
  });

  it('falls back to the draw copy for an unrecognized winner value', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    gs._emit('game-over', { winner: 'timeout', scores: { human: 5, ai: 5 } });
    expect(document.getElementById('gameover-title').textContent).toBe('Empate');
  });

  it('play-again dispatches dominio:restart and hides the overlay', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    gs._emit('game-over', { winner: 'human', scores: { human: 5, ai: 0 } });

    let restarted = false;
    document.addEventListener('dominio:restart', () => { restarted = true; });
    document.getElementById('play-again-btn').dispatchEvent(new MouseEvent('click', { bubbles: true }));

    expect(restarted).toBe(true);
    expect(document.getElementById('gameover-overlay').hidden).toBe(true);
  });

  it('gameover ranking button opens the ranking modal', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    gs._emit('game-over', { winner: 'human', scores: { human: 5, ai: 0 } });
    document.getElementById('gameover-ranking-btn').dispatchEvent(new MouseEvent('click', { bubbles: true }));
    expect(document.getElementById('ranking-modal').open).toBe(true);
  });

  it('quit button dispatches dominio:quit', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    let quit = false;
    document.addEventListener('dominio:quit', () => { quit = true; });
    document.getElementById('quit-btn').dispatchEvent(new MouseEvent('click', { bubbles: true }));
    expect(quit).toBe(true);
  });
});

describe('mutation animation refreshes the piece element', () => {
  it('updates the rendered letter/classes after a mutate event settles', async () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();

    gs.applyAction({ type: ACTION_TYPES.MUTATE, from: { row: 0, col: 0 } });
    settleAnimations();
    await dm.flushAnimations();

    const pieceEl = cell(0, 0).querySelector('[data-piece-id]');
    expect(pieceEl.dataset.type).toBe('spine');
    expect(pieceEl.textContent).toBe('E');
  });
});

describe('defensive guards', () => {
  it('ignores a piece-moved event for an untracked piece id', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    expect(() =>
      gs._emit('piece-moved', { from: { row: 0, col: 0 }, to: { row: 1, col: 0 }, pieceId: 'ghost', owner: 'human' })
    ).not.toThrow();
  });

  it('ignores a piece-mutated event for an untracked piece id', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    expect(() =>
      gs._emit('piece-mutated', { pos: { row: 0, col: 0 }, owner: 'human', oldType: 'rock', newType: 'spine', pieceId: 'ghost' })
    ).not.toThrow();
  });

  it('skips rendering the energy bar when its element is missing from the page', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    document.getElementById('player-energy-bar').remove();
    expect(() => gs._emit('energy-changed', { owner: PLAYER_IDS.HUMAN, newEnergy: 9 })).not.toThrow();
  });

  it('renders an empty class list for a piece with an unrecognized type', () => {
    const gs = new GameState({ playerLayout: [], aiLayout: [] });
    gs._board[0][0] = { id: 'weird', owner: PLAYER_IDS.HUMAN, type: 'unknown' };
    const dm = new DOMManager(gs);
    dm.init();
    const pieceEl = cell(0, 0).querySelector('[data-piece-id="weird"]');
    expect(pieceEl).not.toBeNull();
  });
});

describe('destroy', () => {
  it('removes gameState and board listeners so further events are inert', () => {
    const gs = new GameState(CLASSIC);
    const dm = new DOMManager(gs);
    dm.init();
    dm.destroy();

    cell(0, 0).dispatchEvent(new MouseEvent('click', { bubbles: true }));
    expect(cell(0, 0).classList.contains('cell-selected')).toBe(false);

    expect(() => gs._emit('energy-changed', { owner: 'human', newEnergy: 9 })).not.toThrow();
  });
});
