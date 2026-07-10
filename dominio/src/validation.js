import { DOMINANCE_MAP, MUTATION_COST } from './types.js';

const BOARD_SIZE = 8;

function inBounds(row, col) {
  return row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE;
}

function isOrthogonalAdjacent(from, to) {
  const dr = Math.abs(to.row - from.row);
  const dc = Math.abs(to.col - from.col);
  return (dr === 1 && dc === 0) || (dr === 0 && dc === 1);
}

/**
 * Returns true if moving from → to is legal:
 * must be 1 orthogonal step to an empty cell.
 */
export function isLegalMove(board, from, to) {
  if (!inBounds(from.row, from.col) || !inBounds(to.row, to.col)) return false;
  if (!isOrthogonalAdjacent(from, to)) return false;
  return board[to.row][to.col] === null;
}

/**
 * Returns true if attacking from → to is legal:
 * must be 1 orthogonal step to an enemy piece that the attacker's type dominates.
 */
export function isLegalAttack(board, from, to) {
  if (!inBounds(from.row, from.col) || !inBounds(to.row, to.col)) return false;
  if (!isOrthogonalAdjacent(from, to)) return false;
  const attacker = board[from.row][from.col];
  const defender = board[to.row][to.col];
  if (!attacker || !defender) return false;
  if (attacker.owner === defender.owner) return false;
  return DOMINANCE_MAP[attacker.type] === defender.type;
}

/**
 * Returns true if the owner has enough energy to mutate.
 * @param {{ [owner]: number }} energy
 */
export function isLegalMutate(energy, owner) {
  return (energy[owner] ?? 0) >= MUTATION_COST;
}
