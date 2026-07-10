export const PIECE_TYPES = Object.freeze({
  ROCK:  'rock',
  SPINE: 'spine',
  MIST:  'mist',
});

export const PLAYER_IDS = Object.freeze({
  HUMAN: 'human',
  AI:    'ai',
});

export const ACTION_TYPES = Object.freeze({
  MOVE:   'move',
  ATTACK: 'attack',
  MUTATE: 'mutate',
});

// key beats value (attacker → defeated defender)
export const DOMINANCE_MAP = Object.freeze({
  rock:  'mist',
  mist:  'spine',
  spine: 'rock',
});

// mutation cycle: each type advances to next
export const MUTATION_CYCLE = Object.freeze({
  rock:  'spine',
  spine: 'mist',
  mist:  'rock',
});

export const ENERGY_START   = 5;
export const ENERGY_CAP     = 10;
export const ENERGY_REGEN   = 1;
export const MUTATION_COST  = 2;

export const MAX_TURNS = 100;

// [row, col] of the 4 center cells
export const CENTER_CELLS = Object.freeze([
  [3, 3], [3, 4],
  [4, 3], [4, 4],
]);

export const SCORE_CAPTURE         = 10;
export const SCORE_CENTER_BONUS    = 15;
export const SCORE_ECONOMY_BONUS   = 5;
export const SCORE_MUTATE_PENALTY  = -5;
export const CENTER_CONTROL_MIN    = 3;
export const ECONOMY_MIN_PIECES    = 6;

// Layout shorthand used in boards.json  (R = Rock, E = Spine/Espina, B = Mist/Bruma)
export const LAYOUT_CHAR = Object.freeze({
  R: PIECE_TYPES.ROCK,
  E: PIECE_TYPES.SPINE,
  B: PIECE_TYPES.MIST,
});
