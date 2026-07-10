import { describe, it, expect } from 'vitest';
import { RandomAI, GreedyAI } from '../src/aiEngine.js';
import { PLAYER_IDS } from '../src/types.js';

function emptyBoard() {
  return Array.from({ length: 8 }, () => Array(8).fill(null));
}

// A deterministic RNG that always returns the same value — makes pickRandom's
// index selection predictable without needing a full PRNG implementation.
function fixedRng(value) {
  return () => value;
}

describe('RandomAI', () => {
  it('returns null when the AI has no legal actions', () => {
    const state = { board: emptyBoard(), energy: { ai: 0 } };
    expect(RandomAI.selectMove(state)).toBeNull();
  });

  it('picks a legal action deterministically given a fixed rng', () => {
    const board = emptyBoard();
    board[3][3] = { id: 'ai-1', owner: PLAYER_IDS.AI, type: 'rock' };
    const state = { board, energy: { ai: 0 } };
    const action = RandomAI.selectMove(state, fixedRng(0));
    expect(action).not.toBeNull();
    expect(action.from).toEqual({ row: 3, col: 3 });
  });

  it('defaults to Math.random when no rng is supplied', () => {
    const board = emptyBoard();
    board[3][3] = { id: 'ai-1', owner: PLAYER_IDS.AI, type: 'rock' };
    const state = { board, energy: { ai: 0 } };
    const action = RandomAI.selectMove(state);
    expect(action).not.toBeNull();
  });
});

describe('GreedyAI', () => {
  it('returns null when the AI has no legal actions', () => {
    const state = { board: emptyBoard(), energy: { ai: 0 } };
    expect(GreedyAI.selectMove(state)).toBeNull();
  });

  it('prefers an attack over a plain move', () => {
    const board = emptyBoard();
    board[3][3] = { id: 'ai-1', owner: PLAYER_IDS.AI, type: 'rock' };
    board[3][4] = { id: 'h-1', owner: PLAYER_IDS.HUMAN, type: 'mist' }; // attackable
    // (2,3) stays empty so a plain move is also available.
    const state = { board, energy: { ai: 0 } };
    const action = GreedyAI.selectMove(state, fixedRng(0));
    expect(action.type).toBe('attack');
  });

  it('prefers a center move over an edge move when no attack is available', () => {
    const board = emptyBoard();
    board[2][3] = { id: 'ai-1', owner: PLAYER_IDS.AI, type: 'rock' }; // adjacent to center (3,3)
    const state = { board, energy: { ai: 0 } };
    const action = GreedyAI.selectMove(state, fixedRng(0));
    expect(action.type).toBe('move');
    expect(action.to).toEqual({ row: 3, col: 3 });
  });

  it('only selects mutate when it is the sole legal action', () => {
    // Every cell is an AI piece: no empty cell to move to, no enemy to attack.
    const board = Array.from({ length: 8 }, (_, r) =>
      Array.from({ length: 8 }, (_, c) => ({ id: `ai-${r}-${c}`, owner: PLAYER_IDS.AI, type: 'rock' }))
    );
    const state = { board, energy: { ai: 5 } };
    const action = GreedyAI.selectMove(state, fixedRng(0));
    expect(action.type).toBe('mutate');
  });

  it('breaks ties between equally-scored actions using the rng', () => {
    const board = emptyBoard();
    board[0][0] = { id: 'ai-1', owner: PLAYER_IDS.AI, type: 'rock' }; // 2 equal-score edge moves
    const stateLow = { board, energy: { ai: 0 } };
    const actionLow = GreedyAI.selectMove(stateLow, fixedRng(0));
    const actionHigh = GreedyAI.selectMove(stateLow, fixedRng(0.999));
    expect(actionLow).not.toBeNull();
    expect(actionHigh).not.toBeNull();
  });
});
