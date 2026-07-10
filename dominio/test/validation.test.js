import { describe, it, expect } from 'vitest';
import { isLegalMove, isLegalAttack, isLegalMutate } from '../src/validation.js';

function emptyBoard() {
  return Array.from({ length: 8 }, () => Array(8).fill(null));
}

describe('isLegalMove', () => {
  it('allows a single orthogonal step onto an empty cell', () => {
    const board = emptyBoard();
    expect(isLegalMove(board, { row: 3, col: 3 }, { row: 3, col: 4 })).toBe(true);
  });

  it('rejects diagonal steps', () => {
    const board = emptyBoard();
    expect(isLegalMove(board, { row: 3, col: 3 }, { row: 4, col: 4 })).toBe(false);
  });

  it('rejects steps of more than 1 cell', () => {
    const board = emptyBoard();
    expect(isLegalMove(board, { row: 3, col: 3 }, { row: 3, col: 5 })).toBe(false);
  });

  it('rejects moving onto an occupied cell', () => {
    const board = emptyBoard();
    board[3][4] = { id: 'x', owner: 'human', type: 'rock' };
    expect(isLegalMove(board, { row: 3, col: 3 }, { row: 3, col: 4 })).toBe(false);
  });

  it('rejects out-of-bounds origin or destination', () => {
    const board = emptyBoard();
    expect(isLegalMove(board, { row: -1, col: 0 }, { row: 0, col: 0 })).toBe(false);
    expect(isLegalMove(board, { row: 0, col: 0 }, { row: -1, col: 0 })).toBe(false);
    expect(isLegalMove(board, { row: 0, col: 0 }, { row: 8, col: 0 })).toBe(false);
  });
});

describe('isLegalAttack', () => {
  it('allows an attack when attacker type beats defender type', () => {
    const board = emptyBoard();
    board[3][3] = { id: 'a', owner: 'human', type: 'rock' };
    board[3][4] = { id: 'b', owner: 'ai', type: 'mist' };
    expect(isLegalAttack(board, { row: 3, col: 3 }, { row: 3, col: 4 })).toBe(true);
  });

  it('rejects an attack when attacker type loses to defender type', () => {
    const board = emptyBoard();
    board[3][3] = { id: 'a', owner: 'human', type: 'mist' };
    board[3][4] = { id: 'b', owner: 'ai', type: 'rock' };
    expect(isLegalAttack(board, { row: 3, col: 3 }, { row: 3, col: 4 })).toBe(false);
  });

  it('rejects attacking your own piece', () => {
    const board = emptyBoard();
    board[3][3] = { id: 'a', owner: 'human', type: 'rock' };
    board[3][4] = { id: 'b', owner: 'human', type: 'mist' };
    expect(isLegalAttack(board, { row: 3, col: 3 }, { row: 3, col: 4 })).toBe(false);
  });

  it('rejects attacking an empty cell', () => {
    const board = emptyBoard();
    board[3][3] = { id: 'a', owner: 'human', type: 'rock' };
    expect(isLegalAttack(board, { row: 3, col: 3 }, { row: 3, col: 4 })).toBe(false);
  });

  it('rejects attacking from an empty cell', () => {
    const board = emptyBoard();
    board[3][4] = { id: 'b', owner: 'ai', type: 'mist' };
    expect(isLegalAttack(board, { row: 3, col: 3 }, { row: 3, col: 4 })).toBe(false);
  });

  it('rejects a non-adjacent attack', () => {
    const board = emptyBoard();
    board[3][3] = { id: 'a', owner: 'human', type: 'rock' };
    board[3][5] = { id: 'b', owner: 'ai', type: 'mist' };
    expect(isLegalAttack(board, { row: 3, col: 3 }, { row: 3, col: 5 })).toBe(false);
  });

  it('rejects out-of-bounds attack coordinates', () => {
    const board = emptyBoard();
    board[0][0] = { id: 'a', owner: 'human', type: 'rock' };
    expect(isLegalAttack(board, { row: 0, col: 0 }, { row: -1, col: 0 })).toBe(false);
  });

  it('covers the full dominance cycle: rock > mist > spine > rock', () => {
    const board = emptyBoard();
    board[0][0] = { id: 'a', owner: 'human', type: 'mist' };
    board[0][1] = { id: 'b', owner: 'ai', type: 'spine' };
    expect(isLegalAttack(board, { row: 0, col: 0 }, { row: 0, col: 1 })).toBe(true);

    board[1][0] = { id: 'c', owner: 'human', type: 'spine' };
    board[1][1] = { id: 'd', owner: 'ai', type: 'rock' };
    expect(isLegalAttack(board, { row: 1, col: 0 }, { row: 1, col: 1 })).toBe(true);
  });
});

describe('isLegalMutate', () => {
  it('allows mutation when energy meets the cost', () => {
    expect(isLegalMutate({ human: 2 }, 'human')).toBe(true);
    expect(isLegalMutate({ human: 5 }, 'human')).toBe(true);
  });

  it('rejects mutation when energy is below the cost', () => {
    expect(isLegalMutate({ human: 1 }, 'human')).toBe(false);
  });

  it('treats a missing owner entry as zero energy', () => {
    expect(isLegalMutate({}, 'human')).toBe(false);
  });
});
