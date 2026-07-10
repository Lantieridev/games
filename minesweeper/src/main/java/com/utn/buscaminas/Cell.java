package com.utn.buscaminas;

public class Cell {
    private boolean isMined;
    private boolean isRevealed;
    private int adjacentMines;

    public Cell() {
        this.isMined = false;
        this.isRevealed = false;
        this.adjacentMines = 0;
    }

    public boolean isMined() {
        return isMined;
    }

    public void setMined(boolean mined) {
        isMined = mined;
    }

    public boolean isRevealed() {
        return isRevealed;
    }

    public void reveal() {
        isRevealed = true;
    }

    public int getAdjacentMines() {
        return adjacentMines;
    }

    public void setAdjacentMines(int adjacentMines) {
        this.adjacentMines = adjacentMines;
    }

    /**
     * Plain-text representation, with no ANSI color codes - safe for tests,
     * logs, or any non-terminal context. Use toColoredString() for the
     * actual console UI.
     */
    @Override
    public String toString() {
        if (!isRevealed) {
            return "[?]";
        }
        if (isMined) {
            return "[*]";
        }
        return "[" + adjacentMines + "]";
    }

    /**
     * Console rendering with ANSI colors - the number's color hints at how
     * dangerous the cell is (blue=1 through red=3, yellow=4+).
     */
    public String toColoredString() {
        String RESET = "[0m";
        String RED = "[31m";
        String GREEN = "[32m";
        String YELLOW = "[33m";
        String BLUE = "[34m";
        String CYAN = "[36m";

        if (!isRevealed) {
            return CYAN + "[?]" + RESET;
        }
        if (isMined) {
            return RED + "[*]" + RESET;
        }

        String color = switch (adjacentMines) {
            case 1 -> BLUE;
            case 2 -> GREEN;
            case 3 -> RED;
            case 4 -> YELLOW;
            default -> RESET;
        };

        return color + "[" + adjacentMines + "]" + RESET;
    }
}
