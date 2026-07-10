package com.utn.buscaminas;

public enum Difficulty {
    EASY(10, 10),
    HARD(20, 20);

    private final int size;
    private final int mines;

    Difficulty(int size, int mines) {
        this.size = size;
        this.mines = mines;
    }

    public int getSize() {
        return size;
    }

    public int getMines() {
        return mines;
    }
}
