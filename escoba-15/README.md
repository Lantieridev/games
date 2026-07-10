# 🃏 Escoba de 15 | Java Card Engine

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-3.9-blue?style=for-the-badge&logo=apache-maven)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

> Part of the [games](https://github.com/Lantieridev/games) monorepo.

> [!IMPORTANT]
> **Strategic Engine:** A robust implementation of the traditional Spanish card game "Escoba de 15", featuring competitive AI, round-based logic, and a refined terminal experience.

Welcome to **Escoba Engine**, a technical deep-dive into object-oriented game design. This project simulates the classic Spanish "Escoba", where players compete to clear the table by summing cards to 15.

---

## 🚀 Key Features

*   **🤖 Competitive AI:** Implements strategic decision-making to prioritize high-value cards (7 of Oros) and board clears.
*   **📊 Dynamic Scoring:** Full implementation of traditional rules, including "Escobas", "Sietes", and "Velo".
*   **✨ Premium Terminal UI:** An immersive CLI experience featuring ASCII art banners and letter-by-letter printing for professional storytelling.
*   **🧪 Reliable Architecture:** Built with industry best practices, including Strategy patterns and comprehensive unit testing.

---

## 🛠️ Tech Stack

*   **Java 21**: Modern language constructs for robust game logic.
*   **Maven**: Build automation and dependency management.
*   **JUnit 5 & Mockito**: High-coverage testing for game state and AI behavior.
*   **GitHub Actions**: Continuous integration for every push.

---

## 📖 How to Run

### 1. Build and Test
```powershell
mvn clean install
```

### 2. Start the Game
```powershell
mvn exec:java "-Dexec.mainClass=ar.edu.utn.frc.tup.lciii.App"
```

---

## 📜 Academic Context
Originally developed as a technical challenge for the Java Programming curriculum at UTN FRC, this version has been significantly enhanced to meet professional portfolio standards, including UI/UX improvements and documentation translation.

---

*Developed by [Martin Lantieri](https://github.com/Lantieridev) - 2026*

