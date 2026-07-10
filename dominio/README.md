# ⚔️ Dominio | Cyclic-Dominance Strategy Game

![JavaScript](https://img.shields.io/badge/JavaScript-ES6-yellow?style=for-the-badge&logo=javascript)
![Vite](https://img.shields.io/badge/Vite-5-blue?style=for-the-badge&logo=vite)
![Tailwind](https://img.shields.io/badge/Tailwind-CSS-38bdf8?style=for-the-badge&logo=tailwindcss)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)
![Coverage](https://img.shields.io/badge/coverage-100%25%20lines-brightgreen?style=for-the-badge)

> Part of the [games](https://github.com/Lantieridev/games) monorepo.

> [!IMPORTANT]
> **Rock-Paper-Scissors, but on a board:** an 8×8 strategy game where pieces beat each other in a rotating dominance cycle instead of a fixed hierarchy.

A browser-based 8×8 strategy game built with vanilla JavaScript (ES6 modules), Vite 5, and Tailwind CSS.
Players move, attack, and mutate pieces under a cyclic-dominance combat system until one side is eliminated or 100 actions are exhausted.

---

## Install & run

```bash
npm install
npm run dev             # http://localhost:5173
npm run build           # production build → dist/
npm run preview         # preview production build
npm test                # run the test suite
npm run test:coverage   # run with coverage thresholds enforced
```

---

## How to play

### Objective
Eliminate all enemy pieces **or** outscore the AI when the 100-action limit is reached.

### Piece types and dominance

| Type    | Symbol | Beats  |
|---------|--------|--------|
| Roca    | R      | Bruma  |
| Espina  | E      | Roca   |
| Bruma   | B      | Espina |

Cyclic: Roca > Bruma > Espina > Roca.  
An attack is only legal when the attacker's type **beats** the defender's type.

### Actions (each costs 1 turn)

- **Move** — slide one step orthogonally to an empty cell.
- **Attack** — step into an adjacent enemy cell where the attacker beats the defender.  
  The attacker **stays in place**; the defender is removed.
- **Mutate** — cycle the piece type (Roca → Espina → Bruma → Roca), costs **2 energy**.

### Energy

- Both players start with **5 energy** (cap 10).
- +1 energy is granted at the **start** of each player's turn.
- Mutation costs 2 energy.

### Scoring

| Event            | Points |
|------------------|--------|
| Capture          | +10    |
| Mutate           | −5     |
| Center bonus (≥ 3 of 4 center cells at game end) | +15 |
| Economy bonus (≥ 6 pieces at game end) | +5 |

### Winning at turn limit

Priority: **most points → most pieces → most center cells → draw**.

---

## Controls

| Input           | Action                    |
|-----------------|---------------------------|
| Click piece     | Select / reselect         |
| Click target    | Move or attack            |
| Mutar button    | Mutate selected piece     |
| Cancelar button | Deselect                  |
| Arrow keys      | Navigate board            |
| Enter / Space   | Activate focused cell     |
| Escape          | Deselect                  |

---

## AI difficulty

| Level   | Algorithm |
|---------|-----------|
| Fácil   | `RandomAI` — picks any legal action uniformly at random. |
| Normal  | `GreedyAI` — maximises a heuristic: attacks (+30), center moves (+15), mutations (−5). Tiebreaks randomly. |

---

## Board variants

| Name       | Layout description                              |
|------------|-------------------------------------------------|
| Clásico    | Both sides start grouped by type (RRREEEBB).    |
| Espejado   | Player grouped; AI mirrored (BBEEERRR).         |
| Alternado  | Pieces interleaved for immediate contact.       |
| Flanqueado | Brumas on the flanks for both sides.            |

Variants are loaded from `/data/boards.json`; if the fetch fails the classic layout is used.

---

## Architecture

```
src/
  main.js          — entry point: setup wiring + game loop
  gameState.js     — GameState (extends EventTarget): all rules and events
  domManager.js    — DOMManager: renders board, queues animations, handles input
  aiEngine.js      — RandomAI and GreedyAI strategy objects
  validation.js    — pure move/attack/mutate legality functions
  scoring.js       — pure scoring helpers and end-game bonuses
  animations.js    — CSS animation helpers (animationend-based promises)
  apiClient.js     — fetch board configs and ranking seed from /data/
  storageManager.js — localStorage persistence (player name + ranking)
  types.js         — all shared constants and enums
test/
  *.test.js        — one suite per src/ module, plus domManager.test.js (DOM) and main.test.js (E2E)
  helpers/         — index.html DOM fixture + jsdom <dialog> polyfill
```

**GameState** dispatches eight `CustomEvent`s on itself:
`piece-moved`, `piece-attacked`, `piece-mutated`, `energy-changed`,
`score-updated`, `turn-ended`, `game-over`, `action-invalid`.

**DOMManager** subscribes to those events and drives all visual updates.  
It dispatches `dominio:restart` and `dominio:quit` on `document` for main.js to handle.

---

## Testing

141 tests (Vitest + jsdom), 100% line/statement/function coverage and 99%+ branch coverage, enforced in CI:

- **Unit** — `validation.js`, `scoring.js`, `gameState.js`, `aiEngine.js`, `storageManager.js`, `apiClient.js`, `animations.js`: every rule, branch, and edge case (dominance cycle, turn-limit tie-break cascade, ranking dedup/cap, fetch failure fallbacks).
- **DOM** — `domManager.js` mounted against the real `index.html` markup via jsdom: clicks, keyboard navigation, selection highlighting, the ranking modal, the game-over overlay.
- **End-to-end** — `test/main.test.js` drives the real public entry point: fills the setup form, submits it, plays a full game to a win purely through simulated board clicks, and asserts the ranking entry lands in `localStorage`. The AI's `Math.random` source is stubbed to a fixed value so the full game loop — including the AI's own moves — is deterministic instead of a documented "can't test this" gap.

## Tech decisions

- **No framework** — the DOM event system + `CustomEvent` + `EventTarget` are sufficient for this scope. Adding React/Vue would increase bundle size with no architectural benefit.
- **Animation queue** — animations chain via `Promise`s (`_animChain`) so moves, attacks, and mutations never overlap visually.
- **Strategy pattern for AI** — `RandomAI` and `GreedyAI` are plain objects with a `selectMove(state)` method. Swapping algorithms requires zero changes to the game loop.
- **Pure validation/scoring** — `validation.js` and `scoring.js` have no side effects and are unit-testable without a DOM.
- **Tailwind CDN (play)** — chosen for rapid iteration; a production build would use the CLI instead.

---

## 📜 Academic Context
Originally developed as a technical challenge for a Frontend curriculum at UTN FRC, this version has been cleaned up for portfolio presentation.

---

*Developed by [Martin Lantieri](https://github.com/Lantieridev) - 2026*
