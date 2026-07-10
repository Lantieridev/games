package com.utn.buscaminas;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {

    @Test
    public void testBoardInitialization() {
        Board board = new Board(Difficulty.EASY);
        assertEquals(10, board.getSize());
        assertEquals(10, board.getMinesCount());
        
        int mines = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board.getCell(i, j).isMined()) mines++;
            }
        }
        assertEquals(10, mines);
    }

    @Test
    public void testInvalidCoordinates() {
        Board board = new Board(Difficulty.EASY);
        assertNull(board.getCell(-1, 0));
        assertNull(board.getCell(10, 0));
        assertNull(board.getCell(0, -1));
        assertNull(board.getCell(0, 10));
    }

    @Test
    public void testAllSafeCellsRevealed() {
        Board board = new Board(Difficulty.EASY);
        // Initially false
        assertFalse(board.allSafeCellsRevealed());
        
        // Manually reveal all non-mined cells
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Cell cell = board.getCell(i, j);
                if (!cell.isMined()) {
                    cell.reveal();
                }
            }
        }
        assertTrue(board.allSafeCellsRevealed());
    }
}
