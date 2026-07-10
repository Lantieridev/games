import { describe, it, expect } from 'vitest';
import {
  scoreCapture,
  scoreMutate,
  applyCenterBonus,
  applyEconomyBonus,
  computeScore,
} from '../src/scoring.js';

function emptyBoard() {
  return Array.from({ length: 8 }, () => Array(8).fill(null));
}

describe('scoreCapture / scoreMutate', () => {
  it('returns the fixed capture and mutate deltas', () => {
    expect(scoreCapture()).toBe(10);
    expect(scoreMutate()).toBe(-5);
  });
});

describe('applyCenterBonus', () => {
  it('returns 0 when fewer than 3 of the 4 center cells are controlled', () => {
    const board = emptyBoard();
    board[3][3] = { owner: 'human' };
    board[3][4] = { owner: 'human' };
    expect(applyCenterBonus('human', board)).toBe(0);
  });

  it('returns 15 when exactly 3 of the 4 center cells are controlled', () => {
    const board = emptyBoard();
    board[3][3] = { owner: 'human' };
    board[3][4] = { owner: 'human' };
    board[4][3] = { owner: 'human' };
    expect(applyCenterBonus('human', board)).toBe(15);
  });

  it('returns 15 when all 4 center cells are controlled', () => {
    const board = emptyBoard();
    board[3][3] = { owner: 'human' };
    board[3][4] = { owner: 'human' };
    board[4][3] = { owner: 'human' };
    board[4][4] = { owner: 'human' };
    expect(applyCenterBonus('human', board)).toBe(15);
  });
});

describe('applyEconomyBonus', () => {
  it('returns 0 below the 6-piece threshold', () => {
    expect(applyEconomyBonus('human', 5)).toBe(0);
  });

  it('returns 5 at or above the 6-piece threshold', () => {
    expect(applyEconomyBonus('human', 6)).toBe(5);
    expect(applyEconomyBonus('human', 8)).toBe(5);
  });
});

describe('computeScore', () => {
  it('sums accumulated score with center and economy bonuses', () => {
    const board = emptyBoard();
    for (let c = 0; c < 6; c++) board[0][c] = { owner: 'human' };
    board[3][3] = { owner: 'human' };
    board[3][4] = { owner: 'human' };
    board[4][3] = { owner: 'human' };

    const state = { scores: { human: 40, ai: 0 } };
    // 40 base + 15 center bonus (3 center cells) + 5 economy bonus (9 pieces)
    expect(computeScore('human', board, state)).toBe(60);
  });

  it('applies no bonuses when thresholds are not met', () => {
    const board = emptyBoard();
    board[0][0] = { owner: 'human' };
    const state = { scores: { human: 10, ai: 0 } };
    expect(computeScore('human', board, state)).toBe(10);
  });
});
