package ar.edu.utn.frc.tup.p3;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardTest {

    private Board board;

    @BeforeEach
    void setup() {
        board = new Board();
    }

    @Test
    void testConstructor_CreatesTenByTenBoard() {
        assertEquals(10, board.getBoard().length);
        assertEquals(10, board.getBoard()[0].length);
    }

    @Test
    void testInitBoardFleet_FillsEveryCellWithWater() {
        board.initBoardFleet();
        for (String[] row : board.getBoard()) {
            for (String cell : row) {
                assertTrue(cell.contains("~"));
            }
        }
    }

    @Test
    void testInitBoardEnemyFleet_FillsEveryCellWithHiddenMarker() {
        board.initBoardEnemyFleet();
        for (String[] row : board.getBoard()) {
            for (String cell : row) {
                assertTrue(cell.contains("·"));
            }
        }
    }

    @Test
    void testSetShipPositions_PlacesEachShipOnItsCell() {
        board.initBoardFleet();
        Ship ship = new Ship(new Position(2, 3), ShipStatus.AFLOAT);
        board.setShipPositions(List.of(ship));
        assertTrue(board.getBoard()[2][3].contains("S"));
    }

    @Test
    void testSetWaterOnBoard_MarksThatCellAsWater() {
        Position position = new Position(4, 4);
        board.setWaterOnBoard(position);
        assertTrue(board.getBoard()[4][4].contains("~"));
    }

    @Test
    void testSetShipOnBoard_MarksThatCellAsShip() {
        Position position = new Position(5, 5);
        board.setShipOnBoard(position);
        assertTrue(board.getBoard()[5][5].contains("S"));
    }

    @Test
    void testDrawBoard_PrintsTheGrid() {
        board.initBoardFleet();
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));
        try {
            board.drawBoard();
        } finally {
            System.setOut(originalOut);
        }
        String output = outContent.toString();
        assertTrue(output.contains("0") && output.contains("9"));
    }

    @Test
    void testSetBoard_ReplacesTheGrid() {
        String[][] custom = new String[10][10];
        board.setBoard(custom);
        assertEquals(custom, board.getBoard());
    }
}
