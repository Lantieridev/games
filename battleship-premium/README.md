# 🚢 Battleship Premium | Java Combat Engine

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk)
![Maven](https://img.shields.io/badge/Maven-3.9-blue?style=for-the-badge&logo=apache-maven)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

> Part of the [games](https://github.com/Lantieridev/games) monorepo.

> [!IMPORTANT]
> **Advanced Tactical Simulation:** A premium terminal-based implementation of the classic Naval Battle, featuring sophisticated AI combat logic, ANSI-colored radar systems, and complete fleet management.

Welcome to **Battleship Premium**, a technical showcase of object-oriented game architecture. This project simulates a tactical naval conflict where players compete against a strategic AI on a 10x10 grid.

---

## 🚀 Key Features

*   **🤖 Strategic AI:** Features a calculated combat engine that prioritizes hits and searches for adjacent ship segments.
*   **📡 Immersive Radar:** A refined terminal interface using Unicode box-drawing characters and ANSI colors for a high-tech military feel.
*   **📊 Fleet Management:** Full implementation of variable ship sizes, orientation logic, and damage tracking.
*   **🧪 100% Logic Stability:** Extensive unit tests ensuring coordinate validation, hit detection, and win/loss state accuracy.

---

## 🛠️ Tech Stack

*   **Java 21**: Leveraging modern language features for robust state management.
*   **Maven**: Build automation and dependency management.
*   **JUnit 5**: Comprehensive testing for game logic.
*   **GitHub Actions**: Automated CI/CD pipeline.

---

## 📖 How to Run

### 1. Build and Test
```powershell
mvn clean install
```

### 2. Start the Battle
```powershell
mvn exec:java "-Dexec.mainClass=ar.edu.utn.frc.tup.p3.App"
```

---

## 📜 Academic Context
Originally developed as a midterm challenge for the Programming III curriculum at UTN FRC, this version has been significantly enhanced to meet professional portfolio standards, including full English localization and UI/UX improvements.

---

*Developed by [Martin Lantieri](https://github.com/Lantieridev) - 2026*
