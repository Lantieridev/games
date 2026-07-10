import {
  CENTER_CELLS,
  SCORE_CAPTURE,
  SCORE_CENTER_BONUS,
  SCORE_ECONOMY_BONUS,
  SCORE_MUTATE_PENALTY,
  CENTER_CONTROL_MIN,
  ECONOMY_MIN_PIECES,
} from './types.js';

/** +10 per enemy capture. */
export function scoreCapture() {
  return SCORE_CAPTURE;
}

/** -5 per mutation performed. */
export function scoreMutate() {
  return SCORE_MUTATE_PENALTY;
}

/**
 * Returns +15 if owner controls ≥3 of the 4 center cells, else 0.
 * Applied at game end.
 */
export function applyCenterBonus(owner, board) {
  let controlled = 0;
  for (const [r, c] of CENTER_CELLS) {
    if (board[r][c]?.owner === owner) controlled++;
  }
  return controlled >= CENTER_CONTROL_MIN ? SCORE_CENTER_BONUS : 0;
}

/**
 * Returns +5 if remaining piece count ≥ 6, else 0.
 * Applied at game end.
 * @param {string} _owner  — kept for API symmetry with applyCenterBonus
 * @param {number} remaining — piece count
 */
export function applyEconomyBonus(_owner, remaining) {
  return remaining >= ECONOMY_MIN_PIECES ? SCORE_ECONOMY_BONUS : 0;
}

/**
 * Returns the projected final score: accumulated score + end bonuses.
 * Used for winner determination and AI evaluation.
 */
export function computeScore(owner, board, state) {
  const remaining = countPieces(owner, board);
  return (
    state.scores[owner]
    + applyCenterBonus(owner, board)
    + applyEconomyBonus(owner, remaining)
  );
}

function countPieces(owner, board) {
  let n = 0;
  for (let r = 0; r < 8; r++) {
    for (let c = 0; c < 8; c++) {
      if (board[r][c]?.owner === owner) n++;
    }
  }
  return n;
}
