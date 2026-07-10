package com.utn.buscaminas;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConsoleUITest {

    // Seed 7 on EASY (10x10, 10 mines) is fixed and known: (0,0) has zero
    // adjacent mines (safe, triggers a flood-fill reveal), and (0,2) is a
    // real mine - both used below to drive genuinely deterministic E2E play
    // instead of guessing at what a real shuffle would do.
    private static final long KNOWN_SEED = 7L;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    @BeforeEach
    public void setup() {
        System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    private void provideInput(String data) {
        System.setIn(new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8)));
    }

    private ConsoleUI seededUi() {
        ConsoleUI ui = new ConsoleUI();
        ui.setSeed(KNOWN_SEED);
        return ui;
    }

    @Test
    public void testFullGame_RevealSafeCellThenHitKnownMine_LosesTheGame() {
        // Name, difficulty EASY, reveal (0,0) (safe, floods open), then hit
        // the known mine at (0,2), decline a rematch.
        provideInput("TestPlayer\n1\n0 0\n0 2\nN\n");

        seededUi().start();

        String output = outContent.toString();
        assertTrue(output.contains("Has tocado una mina"), "expected the game-over banner to print on hitting a mine");
    }

    @Test
    public void testFullGame_HardDifficultySelected_UsesA20x20Board() {
        provideInput("TestPlayer\n2\nQ\nN\n");

        seededUi().start();

        String output = outContent.toString();
        // The board header prints column indices 0..19 for a 20-wide board;
        // "19" only ever appears if the HARD board was actually built.
        assertTrue(output.contains("19"));
    }

    @Test
    public void testFullGame_InvalidDifficultyInput_FallsBackToEasy() {
        provideInput("TestPlayer\nabc\nQ\nN\n");

        seededUi().start();

        String output = outContent.toString();
        assertTrue(output.contains("Opcion no valida, usando Facil por defecto."));
    }

    @Test
    public void testFullGame_InvalidCoordinateFormat_ReportsErrorAndContinues() {
        provideInput("TestPlayer\n1\nnot-a-coordinate\nQ\nN\n");

        seededUi().start();

        String output = outContent.toString();
        assertTrue(output.contains("Entrada invalida"));
    }

    @Test
    public void testFullGame_RevealingAlreadyRevealedCell_ReportsError() {
        provideInput("TestPlayer\n1\n0 0\n0 0\nQ\nN\n");

        seededUi().start();

        String output = outContent.toString();
        assertTrue(output.contains("Coordenada invalida o ya revelada."));
    }

    @Test
    public void testFullGame_PlayAgain_StartsASecondRoundWithFreshBoard() {
        // Quit the first round, say yes to a rematch, quit the second round too.
        provideInput("TestPlayer\n1\nQ\nS\n1\nQ\nN\n");

        seededUi().start();

        long roundCount = outContent.toString().split("Seleccione dificultad").length - 1;
        assertTrue(roundCount == 2, "expected exactly 2 rounds to have been played, got " + roundCount);
    }

    @Test
    public void testFullGame_NoSeedSet_UsesRealRandomBoardAndStillWorks() {
        // Covers the unseeded branch (production path) - quitting immediately
        // means the actual random mine layout never matters for this test.
        provideInput("TestPlayer\n1\nQ\nN\n");

        new ConsoleUI().start();

        assertTrue(outContent.toString().contains("Partida abandonada."));
    }

    @Test
    public void testFullGame_NoWinsRecorded_ShowsEmptyStats() {
        provideInput("TestPlayer\n1\nQ\nN\n");

        seededUi().start();

        assertTrue(outContent.toString().contains("No hay victorias registradas."));
    }

    @Test
    public void testFullGame_WinningRound_RecordsAWinInFinalStats() {
        // Exact remaining safe cells after revealing (0,0) on seed 7, computed
        // directly from Game/Board (not guessed) so this reliably reaches a
        // real win instead of risking the input stream running dry.
        String remainingSafeCells = "0 3\n0 5\n0 6\n0 7\n0 8\n0 9\n"
                + "1 4\n1 5\n1 6\n1 7\n1 9\n"
                + "2 5\n2 6\n2 7\n2 8\n2 9\n"
                + "3 4\n3 5\n3 6\n3 7\n3 8\n3 9\n"
                + "4 4\n4 5\n4 6\n4 7\n4 8\n4 9\n"
                + "5 3\n5 5\n5 6\n5 7\n5 8\n5 9\n"
                + "6 2\n6 3\n6 5\n6 6\n6 7\n6 8\n6 9\n"
                + "7 2\n7 3\n7 4\n7 5\n7 6\n7 7\n7 8\n"
                + "8 0\n8 2\n8 3\n8 4\n8 5\n8 6\n8 7\n8 8\n"
                + "9 0\n9 1\n9 2\n9 3\n9 4\n9 5\n9 6\n9 7\n9 8\n9 9\n";
        provideInput("TestPlayer\n1\n0 0\n" + remainingSafeCells + "N\n");

        seededUi().start();

        String output = outContent.toString();
        assertFalse(output.contains("No hay victorias registradas."));
        assertTrue(output.contains("Victorias: 1") || output.contains("CAMPO DESPEJADO"));
    }
}
