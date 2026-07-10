import { describe, it, expect, beforeEach } from 'vitest';
import { GameState } from '../src/gameState.js';
import { ACTION_TYPES, PLAYER_IDS } from '../src/types.js';

const CLASSIC = {
  playerLayout: ['R', 'R', 'R', 'E', 'E', 'E', 'B', 'B'],
  aiLayout:     ['R', 'R', 'R', 'E', 'E', 'E', 'B', 'B'],
};

function listen(gs, event) {
  const events = [];
  gs.addEventListener(event, e => events.push(e.detail));
  return events;
}

describe('GameState construction', () => {
  it('places pieces per layout and starts human turn 0 with 5 energy each', () => {
    const gs = new GameState(CLASSIC);
    const state = gs.getState();
    expect(state.currentPlayer).toBe(PLAYER_IDS.HUMAN);
    expect(state.turn).toBe(0);
    expect(state.gameOver).toBe(false);
    expect(state.energy).toEqual({ human: 5, ai: 5 });
    expect(state.board[0][0]).toEqual({ id: 'human-0', owner: 'human', type: 'rock' });
    expect(state.board[7][7]).toEqual({ id: 'ai-7', owner: 'ai', type: 'mist' });
    expect(state.board[1][0]).toBeNull();
  });

  it('tolerates construction without a config (board left null until initialized elsewhere)', () => {
    const gs = new GameState();
    expect(gs.getState).toBeDefined();
  });
});

describe('beginTurn', () => {
  it('grants +1 energy to the current player, capped at 10', () => {
    const gs = new GameState(CLASSIC);
    gs.beginTurn();
    expect(gs.getState().energy.human).toBe(6);
  });

  it('does nothing once the game is over', () => {
    const gs = new GameState(CLASSIC);
    gs._gameOver = true;
    gs.beginTurn();
    expect(gs.getState().energy.human).toBe(5);
  });

  it('auto-skips the turn when the current player has zero legal actions', () => {
    const gs = new GameState({ playerLayout: [], aiLayout: [] });
    // Human has no pieces at all → no legal move/attack, and 0 energy < mutate cost initially.
    const skipped = listen(gs, 'turn-skipped');
    const turnEnded = listen(gs, 'turn-ended');
    gs.beginTurn();
    expect(skipped).toEqual([{ owner: PLAYER_IDS.HUMAN }]);
    expect(turnEnded).toEqual([{ nextOwner: PLAYER_IDS.AI }]);
    expect(gs.getState().currentPlayer).toBe(PLAYER_IDS.AI);
  });

  it('skips the turn when pieces exist but have neither moves/attacks nor enough energy to mutate', () => {
    const gs = new GameState({ playerLayout: [], aiLayout: [] });
    // Fill the whole board with the human's own pieces: no empty cell to
    // move to and no enemy to attack, so only mutate could save the turn.
    for (let r = 0; r < 8; r++) {
      for (let c = 0; c < 8; c++) {
        gs._board[r][c] = { id: `h-${r}-${c}`, owner: PLAYER_IDS.HUMAN, type: 'rock' };
      }
    }
    gs._energy.human = 0; // +1 from beginTurn → 1, still below the mutate cost of 2
    const skipped = listen(gs, 'turn-skipped');
    gs.beginTurn();
    expect(skipped).toEqual([{ owner: PLAYER_IDS.HUMAN }]);
  });

  it('does not skip when the only legal action is an attack', () => {
    const gs = new GameState({ playerLayout: [], aiLayout: [] });
    // Fill the board with human pieces except one enemy cell the lone
    // attacker piece can capture — no empty cells, so only the attack works.
    for (let r = 0; r < 8; r++) {
      for (let c = 0; c < 8; c++) {
        gs._board[r][c] = { id: `h-${r}-${c}`, owner: PLAYER_IDS.HUMAN, type: 'spine' };
      }
    }
    gs._board[3][3] = { id: 'atk', owner: PLAYER_IDS.HUMAN, type: 'rock' };
    gs._board[3][4] = { id: 'def', owner: PLAYER_IDS.AI, type: 'mist' };
    gs._energy.human = 0;
    const skipped = listen(gs, 'turn-skipped');
    gs.beginTurn();
    expect(skipped).toEqual([]);
  });

  it('does not skip when the only legal action is mutation', () => {
    // A single boxed-in human piece surrounded by its own team on every side,
    // but with enough energy to mutate.
    const gs = new GameState({ playerLayout: [], aiLayout: [] });
    gs._board[3][3] = { id: 'p', owner: PLAYER_IDS.HUMAN, type: 'rock' };
    gs._board[2][3] = { id: 'n', owner: PLAYER_IDS.HUMAN, type: 'rock' };
    gs._board[4][3] = { id: 's', owner: PLAYER_IDS.HUMAN, type: 'rock' };
    gs._board[3][2] = { id: 'w', owner: PLAYER_IDS.HUMAN, type: 'rock' };
    gs._board[3][4] = { id: 'e', owner: PLAYER_IDS.HUMAN, type: 'rock' };
    const skipped = listen(gs, 'turn-skipped');
    gs.beginTurn();
    expect(skipped).toEqual([]);
    expect(gs.getState().currentPlayer).toBe(PLAYER_IDS.HUMAN);
  });
});

describe('applyAction — move', () => {
  it('moves a piece to an empty adjacent cell and ends the turn', () => {
    const gs = new GameState(CLASSIC);
    const moved = listen(gs, 'piece-moved');
    const ok = gs.applyAction({ type: ACTION_TYPES.MOVE, from: { row: 0, col: 0 }, to: { row: 1, col: 0 } });
    expect(ok).toBe(true);
    expect(moved).toHaveLength(1);
    const state = gs.getState();
    expect(state.board[1][0]).toEqual({ id: 'human-0', owner: 'human', type: 'rock' });
    expect(state.board[0][0]).toBeNull();
    expect(state.currentPlayer).toBe(PLAYER_IDS.AI);
    expect(state.turn).toBe(1);
  });

  it('rejects moving a piece that is not owned by the current player', () => {
    const gs = new GameState(CLASSIC);
    const invalid = listen(gs, 'action-invalid');
    const ok = gs.applyAction({ type: ACTION_TYPES.MOVE, from: { row: 7, col: 0 }, to: { row: 6, col: 0 } });
    expect(ok).toBe(false);
    expect(invalid).toEqual([{ reason: 'not-your-piece' }]);
  });

  it('rejects moving from an empty cell', () => {
    const gs = new GameState(CLASSIC);
    const invalid = listen(gs, 'action-invalid');
    const ok = gs.applyAction({ type: ACTION_TYPES.MOVE, from: { row: 3, col: 3 }, to: { row: 3, col: 4 } });
    expect(ok).toBe(false);
    expect(invalid).toEqual([{ reason: 'not-your-piece' }]);
  });

  it('rejects an illegal move (occupied target)', () => {
    const gs = new GameState(CLASSIC);
    const invalid = listen(gs, 'action-invalid');
    const ok = gs.applyAction({ type: ACTION_TYPES.MOVE, from: { row: 0, col: 0 }, to: { row: 0, col: 1 } });
    expect(ok).toBe(false);
    expect(invalid).toEqual([{ reason: 'illegal-move' }]);
  });

  it('rejects an unknown action type', () => {
    const gs = new GameState(CLASSIC);
    const invalid = listen(gs, 'action-invalid');
    const ok = gs.applyAction({ type: 'fly', from: { row: 0, col: 0 } });
    expect(ok).toBe(false);
    expect(invalid).toEqual([{ reason: 'unknown-action-type' }]);
  });

  it('rejects any action once the game is over', () => {
    const gs = new GameState(CLASSIC);
    gs._gameOver = true;
    const invalid = listen(gs, 'action-invalid');
    const ok = gs.applyAction({ type: ACTION_TYPES.MOVE, from: { row: 0, col: 0 }, to: { row: 1, col: 0 } });
    expect(ok).toBe(false);
    expect(invalid).toEqual([{ reason: 'game-over' }]);
  });
});

describe('applyAction — attack', () => {
  let gs;
  beforeEach(() => {
    gs = new GameState({ playerLayout: [], aiLayout: [] });
  });

  it('removes the defender, keeps the attacker, and awards +10 score', () => {
    gs._board[3][3] = { id: 'atk', owner: PLAYER_IDS.HUMAN, type: 'rock' };
    gs._board[3][4] = { id: 'def', owner: PLAYER_IDS.AI, type: 'mist' };
    gs._board[6][6] = { id: 'ai-safe', owner: PLAYER_IDS.AI, type: 'rock' }; // so AI isn't eliminated

    const attacked = listen(gs, 'piece-attacked');
    const scored = listen(gs, 'score-updated');
    const ok = gs.applyAction({ type: ACTION_TYPES.ATTACK, from: { row: 3, col: 3 }, to: { row: 3, col: 4 } });

    expect(ok).toBe(true);
    expect(attacked).toEqual([{
      attackerPos: { row: 3, col: 3 }, defenderPos: { row: 3, col: 4 },
      owner: 'human', attackerId: 'atk', defenderId: 'def',
    }]);
    expect(scored).toEqual([{ owner: 'human', delta: 10, reason: 'capture' }]);

    const state = gs.getState();
    expect(state.board[3][3]).toEqual({ id: 'atk', owner: 'human', type: 'rock' });
    expect(state.board[3][4]).toBeNull();
    expect(state.scores.human).toBe(10);
  });

  it('rejects an illegal attack', () => {
    gs._board[3][3] = { id: 'atk', owner: PLAYER_IDS.HUMAN, type: 'mist' };
    gs._board[3][4] = { id: 'def', owner: PLAYER_IDS.AI, type: 'rock' };
    const invalid = listen(gs, 'action-invalid');
    const ok = gs.applyAction({ type: ACTION_TYPES.ATTACK, from: { row: 3, col: 3 }, to: { row: 3, col: 4 } });
    expect(ok).toBe(false);
    expect(invalid).toEqual([{ reason: 'illegal-attack' }]);
  });

  it('computes the human as the enemy when the AI is the attacker', () => {
    gs._board[3][3] = { id: 'atk', owner: PLAYER_IDS.AI, type: 'rock' };
    gs._board[3][4] = { id: 'def', owner: PLAYER_IDS.HUMAN, type: 'mist' };
    gs._board[6][6] = { id: 'human-safe', owner: PLAYER_IDS.HUMAN, type: 'rock' }; // human survives
    gs._currentPlayer = PLAYER_IDS.AI;

    const gameOver = listen(gs, 'game-over');
    const ok = gs.applyAction({ type: ACTION_TYPES.ATTACK, from: { row: 3, col: 3 }, to: { row: 3, col: 4 } });

    expect(ok).toBe(true);
    expect(gameOver).toEqual([]); // human still has a piece elsewhere, game continues
    expect(gs.getState().currentPlayer).toBe(PLAYER_IDS.HUMAN);
  });

  it('ends the game immediately when the last enemy piece is eliminated', () => {
    gs._board[3][3] = { id: 'atk', owner: PLAYER_IDS.HUMAN, type: 'rock' };
    gs._board[3][4] = { id: 'def', owner: PLAYER_IDS.AI, type: 'mist' };

    const gameOver = listen(gs, 'game-over');
    const turnEnded = listen(gs, 'turn-ended');
    const ok = gs.applyAction({ type: ACTION_TYPES.ATTACK, from: { row: 3, col: 3 }, to: { row: 3, col: 4 } });

    expect(ok).toBe(true);
    expect(turnEnded).toEqual([]); // no turn-ended — the game ended instead
    expect(gameOver).toHaveLength(1);
    expect(gameOver[0].winner).toBe(PLAYER_IDS.HUMAN);
    expect(gs.getState().gameOver).toBe(true);
  });
});

describe('applyAction — mutate', () => {
  let gs;
  beforeEach(() => {
    gs = new GameState({ playerLayout: [], aiLayout: [] });
    gs._board[0][0] = { id: 'p', owner: PLAYER_IDS.HUMAN, type: 'rock' };
  });

  it('cycles the piece type, spends energy, applies the score penalty', () => {
    const mutated = listen(gs, 'piece-mutated');
    const energyChanged = listen(gs, 'energy-changed');
    const scored = listen(gs, 'score-updated');

    const ok = gs.applyAction({ type: ACTION_TYPES.MUTATE, from: { row: 0, col: 0 } });

    expect(ok).toBe(true);
    expect(mutated).toEqual([{ pos: { row: 0, col: 0 }, owner: 'human', oldType: 'rock', newType: 'spine', pieceId: 'p' }]);
    expect(energyChanged).toEqual([{ owner: 'human', newEnergy: 3 }]);
    expect(scored).toEqual([{ owner: 'human', delta: -5, reason: 'mutate-penalty' }]);
    expect(gs.getState().board[0][0].type).toBe('spine');
    expect(gs.getState().scores.human).toBe(-5);
  });

  it('cycles through the full rock → spine → mist → rock loop', () => {
    gs._energy.human = 10;
    gs.applyAction({ type: ACTION_TYPES.MUTATE, from: { row: 0, col: 0 } });
    gs._currentPlayer = PLAYER_IDS.HUMAN; // force back to human for this isolated test
    expect(gs.getState().board[0][0].type).toBe('spine');

    gs.applyAction({ type: ACTION_TYPES.MUTATE, from: { row: 0, col: 0 } });
    gs._currentPlayer = PLAYER_IDS.HUMAN;
    expect(gs.getState().board[0][0].type).toBe('mist');

    gs.applyAction({ type: ACTION_TYPES.MUTATE, from: { row: 0, col: 0 } });
    expect(gs.getState().board[0][0].type).toBe('rock');
  });

  it('rejects mutation when energy is insufficient', () => {
    gs._energy.human = 1;
    const invalid = listen(gs, 'action-invalid');
    const ok = gs.applyAction({ type: ACTION_TYPES.MUTATE, from: { row: 0, col: 0 } });
    expect(ok).toBe(false);
    expect(invalid).toEqual([{ reason: 'insufficient-energy' }]);
  });
});

describe('getLegalMoves', () => {
  it('returns empty array once the game is over', () => {
    const gs = new GameState(CLASSIC);
    gs._gameOver = true;
    expect(gs.getLegalMoves({ row: 0, col: 0 })).toEqual([]);
  });

  it('returns empty array for an empty cell or opponent piece', () => {
    const gs = new GameState(CLASSIC);
    expect(gs.getLegalMoves({ row: 3, col: 3 })).toEqual([]);
    expect(gs.getLegalMoves({ row: 7, col: 0 })).toEqual([]);
  });

  it('lists moves, attacks, and mutate for a piece with options', () => {
    const gs = new GameState({ playerLayout: [], aiLayout: [] });
    gs._board[3][3] = { id: 'atk', owner: PLAYER_IDS.HUMAN, type: 'rock' };
    gs._board[3][4] = { id: 'def', owner: PLAYER_IDS.AI, type: 'mist' };
    const moves = gs.getLegalMoves({ row: 3, col: 3 });
    const types = moves.map(m => m.type);
    expect(types).toContain(ACTION_TYPES.MOVE);
    expect(types).toContain(ACTION_TYPES.ATTACK);
    expect(types).toContain(ACTION_TYPES.MUTATE);
  });
});

describe('turn limit resolution', () => {
  function boardWith(pieces) {
    const gs = new GameState({ playerLayout: [], aiLayout: [] });
    for (const [r, c, piece] of pieces) gs._board[r][c] = piece;
    return gs;
  }

  it('declares the higher-score player the winner at the turn limit', () => {
    const gs = boardWith([
      [0, 0, { id: 'h', owner: PLAYER_IDS.HUMAN, type: 'rock' }],
      [7, 7, { id: 'a', owner: PLAYER_IDS.AI, type: 'rock' }],
    ]);
    gs._scores.human = 20;
    gs._scores.ai = 5;
    gs._turn = 99;
    const gameOver = listen(gs, 'game-over');

    gs.applyAction({ type: ACTION_TYPES.MOVE, from: { row: 0, col: 0 }, to: { row: 1, col: 0 } });

    expect(gs.getState().gameOver).toBe(true);
    expect(gameOver[0].winner).toBe(PLAYER_IDS.HUMAN);
  });

  it('declares the AI the winner when it has the higher score', () => {
    const gs = boardWith([
      [0, 0, { id: 'h', owner: PLAYER_IDS.HUMAN, type: 'rock' }],
      [7, 7, { id: 'a', owner: PLAYER_IDS.AI, type: 'rock' }],
    ]);
    gs._scores.human = 5;
    gs._scores.ai = 20;
    gs._turn = 99;
    const gameOver = listen(gs, 'game-over');

    gs.applyAction({ type: ACTION_TYPES.MOVE, from: { row: 0, col: 0 }, to: { row: 1, col: 0 } });

    expect(gameOver[0].winner).toBe(PLAYER_IDS.AI);
  });

  it('falls back to piece count when scores are tied', () => {
    const gs = boardWith([
      [0, 0, { id: 'h1', owner: PLAYER_IDS.HUMAN, type: 'rock' }],
      [0, 1, { id: 'h2', owner: PLAYER_IDS.HUMAN, type: 'rock' }],
      [7, 7, { id: 'a1', owner: PLAYER_IDS.AI, type: 'rock' }],
    ]);
    gs._scores.human = 10;
    gs._scores.ai = 10;
    gs._turn = 99;
    const gameOver = listen(gs, 'game-over');

    gs.applyAction({ type: ACTION_TYPES.MOVE, from: { row: 0, col: 0 }, to: { row: 1, col: 0 } });

    expect(gameOver[0].winner).toBe(PLAYER_IDS.HUMAN);
  });

  it('falls back to the AI on piece count when scores are tied', () => {
    const gs = boardWith([
      [0, 0, { id: 'h1', owner: PLAYER_IDS.HUMAN, type: 'rock' }],
      [7, 7, { id: 'a1', owner: PLAYER_IDS.AI, type: 'rock' }],
      [6, 6, { id: 'a2', owner: PLAYER_IDS.AI, type: 'rock' }],
    ]);
    gs._scores.human = 10;
    gs._scores.ai = 10;
    gs._turn = 99;
    const gameOver = listen(gs, 'game-over');

    gs.applyAction({ type: ACTION_TYPES.MOVE, from: { row: 0, col: 0 }, to: { row: 1, col: 0 } });

    expect(gameOver[0].winner).toBe(PLAYER_IDS.AI);
  });

  it('falls back to center-cell control when score and piece count are tied', () => {
    const gs = boardWith([
      [3, 3, { id: 'h1', owner: PLAYER_IDS.HUMAN, type: 'rock' }],
      [0, 0, { id: 'h2', owner: PLAYER_IDS.HUMAN, type: 'rock' }],
      [7, 7, { id: 'a1', owner: PLAYER_IDS.AI, type: 'rock' }],
      [6, 6, { id: 'a2', owner: PLAYER_IDS.AI, type: 'rock' }],
    ]);
    gs._scores.human = 10;
    gs._scores.ai = 10;
    gs._turn = 99;
    const gameOver = listen(gs, 'game-over');

    gs.applyAction({ type: ACTION_TYPES.MOVE, from: { row: 0, col: 0 }, to: { row: 1, col: 0 } });

    expect(gameOver[0].winner).toBe(PLAYER_IDS.HUMAN);
  });

  it('falls back to the AI on center-cell control when score and pieces are tied', () => {
    const gs = boardWith([
      [0, 0, { id: 'h1', owner: PLAYER_IDS.HUMAN, type: 'rock' }],
      [6, 6, { id: 'h2', owner: PLAYER_IDS.HUMAN, type: 'rock' }],
      [3, 3, { id: 'a1', owner: PLAYER_IDS.AI, type: 'rock' }],
      [7, 7, { id: 'a2', owner: PLAYER_IDS.AI, type: 'rock' }],
    ]);
    gs._scores.human = 10;
    gs._scores.ai = 10;
    gs._turn = 99;
    const gameOver = listen(gs, 'game-over');

    gs.applyAction({ type: ACTION_TYPES.MOVE, from: { row: 0, col: 0 }, to: { row: 1, col: 0 } });

    expect(gameOver[0].winner).toBe(PLAYER_IDS.AI);
  });

  it('declares a draw when score, pieces, and center control are all tied', () => {
    const gs = boardWith([
      [0, 0, { id: 'h1', owner: PLAYER_IDS.HUMAN, type: 'rock' }],
      [7, 7, { id: 'a1', owner: PLAYER_IDS.AI, type: 'rock' }],
    ]);
    gs._scores.human = 10;
    gs._scores.ai = 10;
    gs._turn = 99;
    const gameOver = listen(gs, 'game-over');

    gs.applyAction({ type: ACTION_TYPES.MOVE, from: { row: 0, col: 0 }, to: { row: 1, col: 0 } });

    expect(gameOver[0].winner).toBe('draw');
  });

  it('applies center and economy end bonuses before determining the winner', () => {
    const pieces = [];
    for (let c = 0; c < 6; c++) pieces.push([0, c, { id: `h${c}`, owner: PLAYER_IDS.HUMAN, type: 'rock' }]);
    pieces.push([3, 3, { id: 'hc1', owner: PLAYER_IDS.HUMAN, type: 'rock' }]);
    pieces.push([3, 4, { id: 'hc2', owner: PLAYER_IDS.HUMAN, type: 'rock' }]);
    pieces.push([4, 3, { id: 'hc3', owner: PLAYER_IDS.HUMAN, type: 'rock' }]);
    pieces.push([7, 7, { id: 'a1', owner: PLAYER_IDS.AI, type: 'rock' }]);
    const gs = boardWith(pieces);
    gs._turn = 99;
    const scored = listen(gs, 'score-updated');

    gs.applyAction({ type: ACTION_TYPES.MOVE, from: { row: 0, col: 0 }, to: { row: 1, col: 0 } });

    const reasons = scored.map(s => s.reason);
    expect(reasons).toContain('center-bonus');
    expect(reasons).toContain('economy-bonus');
    expect(gs.getState().scores.human).toBe(20); // 15 center + 5 economy
  });
});
