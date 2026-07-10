# 🔥 Dominio Elemental | Java Card Engine

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-3.9-blue?style=for-the-badge&logo=apache-maven)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

> Part of the [games](https://github.com/Lantieridev/games) monorepo.

> [!IMPORTANT]
> A 36-card, elemental combination game for the terminal: Human vs. AI, played to whoever scores the most points before the deck runs out.

Welcome to **Dominio Elemental**, a card game built around a 4-element, 9-value deck (36 cards total: Fuego, Aire, Tierra, Agua × 1-9). Draw, combine, and extend combinations on a shared table to score points before your opponent does.

---

## 🚀 How to Play

### Setup
- 36 cards: 4 elements (Fuego, Aire, Tierra, Agua) × values 1-9.
- Each player starts with 5 cards.
- Each turn: draw a card → create or extend a combination (optional) → discard if your hand exceeds 7 cards.
- The game ends when the deck runs out; highest score wins.

### Combinations

| Type | Rule | Score |
|---|---|---|
| **Secuencia** | Same element, 3+ consecutive values | Sum of the values |
| **Tríada** | Same value, 2-4 distinct elements | Value × 3 (flat — doesn't grow with card count) |
| **Cadena Reactiva** | Exactly Fuego → Aire → Tierra → Agua, one of each | Sum of the values × 2 |

Extending an existing combination on the table scores the actual point *delta* — adding a card to a Secuencia earns its value, but a Tríada's score doesn't change when a 3rd/4th card is added, so extending one earns nothing extra.

### AI Opponent
The AI plays autonomously each turn: checks for extendable combinations first, then looks for a Cadena Reactiva (highest priority), then a Tríada, then a Secuencia — playing every combination it can find before ending its turn.

---

## 🛠️ Tech Stack

*   **Java 21** — records for immutable cards, sealed-style combination hierarchy (`Combination` → `Sequence`/`Triad`/`ReactiveChain`).
*   **Maven** — build automation and dependency management.
*   **JUnit 5** — 78 tests, 96%+ line / 87%+ branch coverage, enforced in CI via a JaCoCo gate. Includes a genuine end-to-end run of the real entry point (`App.main()`) driven through simulated console I/O with a seeded, reproducible shuffle — not just isolated unit tests.
*   **GitHub Actions** — CI runs `mvn verify` on every push.

---

## 📖 How to Run

```powershell
mvn compile exec:java   # play
mvn verify               # run tests + enforce the coverage gate
```

---

## 📜 Academic Context
Originally developed as a final exam for the Backend Programming curriculum at UTN FRC ("Dominio Elemental" — see the full original assignment in [CONSIGNA.md](CONSIGNA.md)), this version has been cleaned up, bug-fixed, and brought up to a real testing bar for the portfolio.

---

*Developed by [Martin Lantieri](https://github.com/Lantieridev) - 2026*
