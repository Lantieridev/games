import {
  PLAYER_IDS,
  ACTION_TYPES,
  MUTATION_CYCLE,
  LAYOUT_CHAR,
  ENERGY_START,
  ENERGY_CAP,
  ENERGY_REGEN,
  MUTATION_COST,
  MAX_TURNS,
  CENTER_CELLS,
  SCORE_CAPTURE,
  SCORE_MUTATE_PENALTY,
} from './types.js';
import { isLegalMove, isLegalAttack, isLegalMutate } from './validation.js';
import { applyCenterBonus, applyEconomyBonus } from './scoring.js';

const DIRS = [[-1, 0], [1, 0], [0, -1], [0, 1]];

/**
 * Owns all game state and rule enforcement.
 * Dispatches CustomEvents on itself — DOMManager subscribes via addEventListener.
 *
 * Public API:
 *   beginTurn()          — call before each player's turn to grant energy
 *   applyAction(action)  — validate + apply; returns boolean success
 *   getLegalMoves(pos)   — all legal actions for piece at pos
 *   getState()           — read-only snapshot of current state
 *
 * CustomEvents dispatched on this instance:
 *   'piece-moved'    → { from, to, pieceId, owner }
 *   'piece-attacked' → { attackerPos, defenderPos, owner }
 *   'piece-mutated'  → { pos, owner, oldType, newType }
 *   'energy-changed' → { owner, newEnergy }
 *   'score-updated'  → { owner, delta, reason }
 *   'turn-ended'     → { nextOwner }
 *   'turn-skipped'   → { owner }
 *   'game-over'      → { winner, scores }
 *   'action-invalid' → { reason }
 */
export class GameState extends EventTarget {
  constructor(config) {
    super();
    this._config        = config ?? null;
    this._board         = null;
    this._energy        = { [PLAYER_IDS.HUMAN]: ENERGY_START, [PLAYER_IDS.AI]: ENERGY_START };
    this._scores        = { [PLAYER_IDS.HUMAN]: 0, [PLAYER_IDS.AI]: 0 };
    this._turn          = 0;
    this._currentPlayer = PLAYER_IDS.HUMAN;
    this._gameOver      = false;

    if (config) this._initBoard(config);
  }

  // ── Public API ────────────────────────────────────────────────────

  /**
   * Grants +1 energy (capped at 10) to the current player.
   * Must be called by main.js before the player/AI acts each turn.
   */
  beginTurn() {
    if (this._gameOver) return;
    const owner = this._currentPlayer;
    const next = Math.min(this._energy[owner] + ENERGY_REGEN, ENERGY_CAP);
    this._energy[owner] = next;
    this._emit('energy-changed', { owner, newEnergy: next });

    // A player with zero legal actions (boxed in, no attacks, no energy to
    // mutate) would otherwise stall the game forever — skip their turn.
    if (!this._hasAnyLegalAction(owner)) {
      this._emit('turn-skipped', { owner });
      this._endTurn();
    }
  }

  /**
   * Validates and applies an action for the current player.
   * @param {{ type: string, from: {row,col}, to?: {row,col} }} action
   * @returns {boolean} true on success
   */
  applyAction(action) {
    if (this._gameOver) {
      this._emit('action-invalid', { reason: 'game-over' });
      return false;
    }

    const { type, from } = action;
    const piece = this._board[from.row]?.[from.col];

    if (!piece || piece.owner !== this._currentPlayer) {
      this._emit('action-invalid', { reason: 'not-your-piece' });
      return false;
    }

    switch (type) {
      case ACTION_TYPES.MOVE:   return this._applyMove(action, piece);
      case ACTION_TYPES.ATTACK: return this._applyAttack(action, piece);
      case ACTION_TYPES.MUTATE: return this._applyMutate(action, piece);
      default:
        this._emit('action-invalid', { reason: 'unknown-action-type' });
        return false;
    }
  }

  /**
   * Returns all legal actions for the piece at pos for the current player.
   * @param {{ row: number, col: number }} pos
   * @returns {Array}
   */
  getLegalMoves({ row, col }) {
    if (this._gameOver) return [];
    const piece = this._board[row]?.[col];
    if (!piece || piece.owner !== this._currentPlayer) return [];

    const from = { row, col };
    const moves = [];

    for (const [dr, dc] of DIRS) {
      const to = { row: row + dr, col: col + dc };
      if (isLegalMove(this._board, from, to)) {
        moves.push({ type: ACTION_TYPES.MOVE, from, to });
      }
      if (isLegalAttack(this._board, from, to)) {
        moves.push({ type: ACTION_TYPES.ATTACK, from, to });
      }
    }

    if (isLegalMutate(this._energy, this._currentPlayer)) {
      moves.push({ type: ACTION_TYPES.MUTATE, from });
    }

    return moves;
  }

  /**
   * Returns a read-only deep snapshot of the current state.
   */
  getState() {
    return {
      board:         this._board.map(row => row.map(cell => cell ? { ...cell } : null)),
      energy:        { ...this._energy },
      scores:        { ...this._scores },
      turn:          this._turn,
      currentPlayer: this._currentPlayer,
      gameOver:      this._gameOver,
    };
  }

  // ── Board initialisation ──────────────────────────────────────────

  _initBoard(config) {
    this._board = Array.from({ length: 8 }, () => Array(8).fill(null));

    config.playerLayout.forEach((char, col) => {
      this._board[0][col] = { id: `human-${col}`, owner: PLAYER_IDS.HUMAN, type: LAYOUT_CHAR[char] };
    });

    config.aiLayout.forEach((char, col) => {
      this._board[7][col] = { id: `ai-${col}`, owner: PLAYER_IDS.AI, type: LAYOUT_CHAR[char] };
    });
  }

  // ── Action handlers ───────────────────────────────────────────────

  _applyMove({ from, to }, piece) {
    if (!isLegalMove(this._board, from, to)) {
      this._emit('action-invalid', { reason: 'illegal-move' });
      return false;
    }

    this._board[to.row][to.col] = piece;
    this._board[from.row][from.col] = null;

    this._emit('piece-moved', { from, to, pieceId: piece.id, owner: piece.owner });
    this._endTurn();
    return true;
  }

  _applyAttack({ from, to }, piece) {
    if (!isLegalAttack(this._board, from, to)) {
      this._emit('action-invalid', { reason: 'illegal-attack' });
      return false;
    }

    // Attacker STAYS — only the defender is removed from the board.
    const defender = this._board[to.row][to.col];
    const defenderId = defender.id;
    this._board[to.row][to.col] = null;

    this._scores[piece.owner] += SCORE_CAPTURE;
    this._emit('piece-attacked', { attackerPos: from, defenderPos: to, owner: piece.owner, attackerId: piece.id, defenderId });
    this._emit('score-updated', { owner: piece.owner, delta: SCORE_CAPTURE, reason: 'capture' });

    // Check: did we eliminate the last enemy piece?
    const enemy = piece.owner === PLAYER_IDS.HUMAN ? PLAYER_IDS.AI : PLAYER_IDS.HUMAN;
    if (this._countPieces(enemy) === 0) {
      this._turn++;
      this._finalizeGame(piece.owner);
      return true;
    }

    this._endTurn();
    return true;
  }

  _applyMutate({ from }, piece) {
    if (!isLegalMutate(this._energy, piece.owner)) {
      this._emit('action-invalid', { reason: 'insufficient-energy' });
      return false;
    }

    const oldType = piece.type;
    const newType = MUTATION_CYCLE[piece.type];

    // Mutate in-place — same piece object, same board position.
    piece.type = newType;
    this._energy[piece.owner] -= MUTATION_COST;
    this._scores[piece.owner] += SCORE_MUTATE_PENALTY;

    this._emit('piece-mutated', { pos: from, owner: piece.owner, oldType, newType, pieceId: piece.id });
    this._emit('energy-changed', { owner: piece.owner, newEnergy: this._energy[piece.owner] });
    this._emit('score-updated', { owner: piece.owner, delta: SCORE_MUTATE_PENALTY, reason: 'mutate-penalty' });

    this._endTurn();
    return true;
  }

  // ── Turn advancement ──────────────────────────────────────────────

  _endTurn() {
    this._turn++;

    if (this._turn >= MAX_TURNS) {
      this._finalizeGame(null); // null = determine winner by score/pieces
      return;
    }

    this._currentPlayer = this._currentPlayer === PLAYER_IDS.HUMAN ? PLAYER_IDS.AI : PLAYER_IDS.HUMAN;
    this._emit('turn-ended', { nextOwner: this._currentPlayer });
  }

  // ── Game over ─────────────────────────────────────────────────────

  /**
   * Applies end-of-game bonuses, determines winner, emits 'game-over'.
   * @param {string|null} forcedWinner — non-null when a player lost all pieces
   */
  _finalizeGame(forcedWinner) {
    this._gameOver = true;
    this._applyEndBonuses();

    const winner = forcedWinner ?? this._determineWinner();
    this._emit('game-over', { winner, scores: { ...this._scores } });
  }

  _applyEndBonuses() {
    for (const owner of [PLAYER_IDS.HUMAN, PLAYER_IDS.AI]) {
      const centerBonus = applyCenterBonus(owner, this._board);
      const econBonus   = applyEconomyBonus(owner, this._countPieces(owner));

      if (centerBonus > 0) {
        this._scores[owner] += centerBonus;
        this._emit('score-updated', { owner, delta: centerBonus, reason: 'center-bonus' });
      }
      if (econBonus > 0) {
        this._scores[owner] += econBonus;
        this._emit('score-updated', { owner, delta: econBonus, reason: 'economy-bonus' });
      }
    }
  }

  /**
   * Determines winner at turn limit.
   * Priority: most score → most pieces → most center cells → draw
   */
  _determineWinner() {
    const hScore  = this._scores[PLAYER_IDS.HUMAN];
    const aScore  = this._scores[PLAYER_IDS.AI];
    const hPieces = this._countPieces(PLAYER_IDS.HUMAN);
    const aPieces = this._countPieces(PLAYER_IDS.AI);

    if (hScore  !== aScore)  return hScore  > aScore  ? PLAYER_IDS.HUMAN : PLAYER_IDS.AI;
    if (hPieces !== aPieces) return hPieces > aPieces ? PLAYER_IDS.HUMAN : PLAYER_IDS.AI;

    const hCenter = this._countCenterPieces(PLAYER_IDS.HUMAN);
    const aCenter = this._countCenterPieces(PLAYER_IDS.AI);
    if (hCenter !== aCenter) return hCenter > aCenter ? PLAYER_IDS.HUMAN : PLAYER_IDS.AI;

    return 'draw';
  }

  // ── Helpers ───────────────────────────────────────────────────────

  /**
   * True if `owner` has at least one legal move, attack, or mutate available.
   * Mutate only counts if `owner` actually has a piece on the board — energy
   * alone isn't enough, there must be something to mutate.
   */
  _hasAnyLegalAction(owner) {
    let hasPiece = false;
    for (let r = 0; r < 8; r++) {
      for (let c = 0; c < 8; c++) {
        const piece = this._board[r][c];
        if (!piece || piece.owner !== owner) continue;
        hasPiece = true;
        const from = { row: r, col: c };
        for (const [dr, dc] of DIRS) {
          const to = { row: r + dr, col: c + dc };
          if (isLegalMove(this._board, from, to))   return true;
          if (isLegalAttack(this._board, from, to)) return true;
        }
      }
    }
    return hasPiece && isLegalMutate(this._energy, owner);
  }

  _countPieces(owner) {
    let n = 0;
    for (let r = 0; r < 8; r++) {
      for (let c = 0; c < 8; c++) {
        if (this._board[r][c]?.owner === owner) n++;
      }
    }
    return n;
  }

  _countCenterPieces(owner) {
    return CENTER_CELLS.filter(([r, c]) => this._board[r][c]?.owner === owner).length;
  }

  _emit(eventName, detail) {
    this.dispatchEvent(new CustomEvent(eventName, { detail }));
  }
}
