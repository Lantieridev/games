import { ACTION_TYPES, PLAYER_IDS, CENTER_CELLS } from './types.js';
import { isLegalMove, isLegalAttack, isLegalMutate } from './validation.js';

const DIRS = [[-1, 0], [1, 0], [0, -1], [0, 1]];

function isCenterCell(pos) {
  return CENTER_CELLS.some(([r, c]) => r === pos.row && c === pos.col);
}

function pickRandom(arr, rng) {
  return arr[Math.floor(rng() * arr.length)];
}

/**
 * Collects all legal actions for the AI player from a state snapshot.
 */
function getAllAIActions(state) {
  const { board, energy } = state;
  const owner = PLAYER_IDS.AI;
  const actions = [];

  for (let r = 0; r < 8; r++) {
    for (let c = 0; c < 8; c++) {
      const piece = board[r][c];
      if (!piece || piece.owner !== owner) continue;

      const from = { row: r, col: c };

      for (const [dr, dc] of DIRS) {
        const to = { row: r + dr, col: c + dc };
        if (isLegalMove(board, from, to)) {
          actions.push({ type: ACTION_TYPES.MOVE, from, to });
        }
        if (isLegalAttack(board, from, to)) {
          actions.push({ type: ACTION_TYPES.ATTACK, from, to });
        }
      }

      if (isLegalMutate(energy, owner)) {
        actions.push({ type: ACTION_TYPES.MUTATE, from });
      }
    }
  }

  return actions;
}

/**
 * Greedy heuristic score for an action.
 * attack: +30  (immediate threat elimination)
 * center move/attack: +15  (board control)
 * mutate: 0 net  (+5 if energy OK, -5 fixed cost = 0; skip unless forced)
 */
function greedyScore(action) {
  let score = 0;
  if (action.type === ACTION_TYPES.ATTACK) score += 30;
  if (action.to && isCenterCell(action.to)) score += 15;
  if (action.type === ACTION_TYPES.MUTATE) score -= 5; // net: discourages mutating unless necessary
  return score;
}

/**
 * Easy AI: selects uniformly at random from all legal actions.
 * @param {object} state — GameState snapshot (from getState())
 * @param {() => number} [rng] — injectable RNG source (0-1), defaults to Math.random.
 *   Pass a seeded generator in tests to make full AI-driven games deterministic.
 */
export const RandomAI = {
  selectMove(state, rng = Math.random) {
    const actions = getAllAIActions(state);
    return actions.length > 0 ? pickRandom(actions, rng) : null;
  },
};

/**
 * Normal AI: greedy score maximisation with random tiebreaking.
 * Attacks are strongly preferred; center moves are preferred over edge moves.
 * Mutation is treated as neutral (score 0) and only selected when no other option exists.
 * @param {object} state
 * @param {() => number} [rng] — injectable RNG source, see RandomAI.
 */
export const GreedyAI = {
  selectMove(state, rng = Math.random) {
    const actions = getAllAIActions(state);
    if (actions.length === 0) return null;

    let best = -Infinity;
    let candidates = [];

    for (const action of actions) {
      const s = greedyScore(action);
      if (s > best) {
        best = s;
        candidates = [action];
      } else if (s === best) {
        candidates.push(action);
      }
    }

    return pickRandom(candidates, rng);
  },
};
