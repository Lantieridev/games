package ar.edu.utn.frc.tup.lciii;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
class EscobaGameTest {

    private Scanner scanner;
    private EscobaGame escobaGame;

    private final PrintStream systemOut = System.out;
    private ByteArrayOutputStream testOut;

    @BeforeEach
    public void setUpOutput() {
        LetterByLetterPrinter.DISABLE_COLORS = true;
        scanner = Mockito.mock(Scanner.class);
        escobaGame = Mockito.spy(EscobaGame.class);
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
        escobaGame.setScanner(scanner);
    }

    @AfterEach
    public void restoreSystemInputOutput() {
        System.setOut(systemOut);
    }

    @Test
    void welcomeMessage() {
        String expected = "=".repeat(80) + System.lineSeparator() +
                "  ███████╗███████╗ ██████╗ ██████╗ ██████╗  █████╗     ██████╗ ███████╗    ██╗███████╗" + System.lineSeparator() +
                "  ██╔════╝██╔════╝██╔════╝██╔═══██╗██╔══██╗██╔══██╗    ██╔══██╗██╔════╝   ███║██╔════╝" + System.lineSeparator() +
                "  █████╗  ███████╗██║     ██║   ██║██████╔╝███████║    ██║  ██║█████╗     ╚██║███████╗" + System.lineSeparator() +
                "  ██╔══╝  ╚════██║██║     ██║   ██║██╔══██╗██╔══██║    ██║  ██║██╔══╝      ██║╚════██║" + System.lineSeparator() +
                "  ███████╗███████║╚██████╗╚██████╔╝██████╔╝██║  ██║    ██████╔╝███████╗    ██║███████║" + System.lineSeparator() +
                "  ╚══════╝╚══════╝ ╚═════╝ ╚═════╝ ╚═════╝ ╚═╝  ╚═╝    ╚═════╝ ╚══════╝    ╚═╝╚══════╝" + System.lineSeparator() +
                "=".repeat(80) + System.lineSeparator() +
                System.lineSeparator() +
                "Welcome to the 'Escoba de 15' game!" + System.lineSeparator();
        escobaGame.welcomeMessage();
        assertEquals(expected.trim().replace("\r\n", "\n"), getOutput().trim().replace("\r\n", "\n"));
    }

    @Test
    void createHumanUser() {
        when(scanner.nextLine()).thenReturn("Hernán");
        User user = escobaGame.createHumanUser();
        assertEquals("Hernán", user.getName());
    }

    @Test
    void createAppUser() {
        User user = escobaGame.createAppUser();
        assertEquals("APP", user.getName());
    }

    @Test
    void wantPlayAgain_TrueAnswer() {
        when(scanner.nextLine()).thenReturn("y");
        Boolean result = escobaGame.wantPlayAgain();
        assertTrue(result);
    }

    @Test
    void wantPlayAgain_FalseAnswer() {
        when(scanner.nextLine()).thenReturn("n");
        Boolean result = escobaGame.wantPlayAgain();
        assertFalse(result);
    }

    @Test
    void getYesNoAnswerTest_YesAnswer() {
        assertTrue(escobaGame.getYesNoAnswer("y"));
        assertTrue(escobaGame.getYesNoAnswer("Y"));
    }

    @Test
    void getYesNoAnswerTest_NoAnswer() {
        assertFalse(escobaGame.getYesNoAnswer("n"));
        assertFalse(escobaGame.getYesNoAnswer("N"));
    }

    @Test
    void getYesNoAnswerTest_NullAnswer() {
        assertNull(escobaGame.getYesNoAnswer("invalid"));
        assertNull(escobaGame.getYesNoAnswer(null));
    }

    private String getOutput() {
        return testOut.toString();
    }
}