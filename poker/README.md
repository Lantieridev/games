# 🃏 Texas Hold'em Poker | Engine & CLI

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-3.9-blue?style=for-the-badge&logo=apache-maven)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

> Part of the [games](https://github.com/Lantieridev/games) monorepo.

> [!IMPORTANT]
> **Technical Showcase:** A comprehensive implementation of Texas Hold'em Poker, featuring automated bot players, advanced hand evaluation logic, and a refined terminal-based user experience.

Welcome to **Poker Engine**, a strategic simulation of the world's most popular card game. This project demonstrates complex business logic implementation, multi-player round management, and a competitive AI system.

---

## 🚀 Project Overview

This engine provides a complete platform for playing Texas Hold'em. It manages the entire game lifecycle, from deck creation and shuffling to complex betting rounds (Pre-Flop, Flop, Turn, River) and final hand showdowns using a specialized evaluation algorithm.

### ✨ Key Features
*   **Complete Game Lifecycle:** Full implementation of Texas Hold'em rules.
*   **Advanced Hand Evaluator:** Precisely detects all poker combinations (from High Card to Royal Flush).
*   **Automated Bot Players:** Competitive bots that participate in betting rounds.
*   **Premium CLI UI:** A refined terminal interface with ANSI colors and interactive elements.
*   **Robust Testing:** Extensive test suite covering hand evaluation, round logic, and player actions.

---

## 🛠️ Tech Stack
*   **Java 21**: Leveraging modern language features for clean code.
*   **Maven**: Build automation and dependency management.
*   **JUnit 5 & Mockito**: Comprehensive unit and integration testing.
*   **Design Principles**: OOP-focused architecture with clear separation of concerns (Model-View-Controller pattern).

---

## 🃏 Poker Hand Ranking
The engine accurately evaluates and compares the following hands (ordered by strength):

1.  **Royal Flush**: A, K, Q, J, 10 of the same suit.
2.  **Straight Flush**: Five consecutive cards of the same suit.
3.  **Four of a Kind**: Four cards of the same value.
4.  **Full House**: Three of a kind plus a pair.
5.  **Flush**: Five cards of the same suit.
6.  **Straight**: Five consecutive cards of different suits.
7.  **Three of a Kind**: Three cards of the same value.
8.  **Two Pair**: Two different pairs.
9.  **One Pair**: Two cards of the same value.
10. **High Card**: The highest card in hand.

---

## ⚙️ How to Run

### 1. Requirements
*   Java JDK 21+
*   Maven 3.8+

### 2. Build and Test
```powershell
mvn clean install
```

### 3. Start the Game
```powershell
mvn exec:java "-Dexec.mainClass=ar.edu.utn.frc.tup.piii.App"
```

---

## 📜 Academic Context
Originally developed as a technical challenge for the Java Programming curriculum at UTN FRC, this version has been significantly enhanced to meet professional portfolio standards, including UI/UX improvements and documentation translation.

---

*Developed by [Martin Lantieri](https://github.com/Lantieridev) - 2026*