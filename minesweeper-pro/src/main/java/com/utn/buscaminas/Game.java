package com.utn.buscaminas;

public class Game {
    private final String playerName;
    private final Board board;
    private boolean isGameOver;
    private boolean isWon;

    public Game(String playerName, Difficulty difficulty) {
        this.playerName = playerName;
        this.board = new Board(difficulty);
        this.isGameOver = false;
        this.isWon = false;
    }

    /**
     * Seeded constructor for deterministic tests - see Board(Difficulty, long).
     */
    public Game(String playerName, Difficulty difficulty, long seed) {
        this.playerName = playerName;
        this.board = new Board(difficulty, seed);
        this.isGameOver = false;
        this.isWon = false;
    }

    public boolean revealCell(int r, int c) {
        if (isGameOver) return false;

        Cell cell = board.getCell(r, c);
        if (cell == null || cell.isRevealed()) {
            return false;
        }

        cell.reveal();

        if (cell.isMined()) {
            isGameOver = true;
            isWon = false;
            return true;
        }

        // Auto-reveal neighbors if 0 adjacent mines
        if (cell.getAdjacentMines() == 0) {
            revealEmptyNeighbors(r, c);
        }

        if (board.allSafeCellsRevealed()) {
            isGameOver = true;
            isWon = true;
        }

        return true;
    }

    private void revealEmptyNeighbors(int r, int c) {
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int ni = r + i;
                int nj = c + j;
                Cell neighbor = board.getCell(ni, nj);
                if (neighbor != null && !neighbor.isRevealed() && !neighbor.isMined()) {
                    neighbor.reveal();
                    if (neighbor.getAdjacentMines() == 0) {
                        revealEmptyNeighbors(ni, nj);
                    }
                }
            }
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    public Board getBoard() {
        return board;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public boolean isWon() {
        return isWon;
    }
}
