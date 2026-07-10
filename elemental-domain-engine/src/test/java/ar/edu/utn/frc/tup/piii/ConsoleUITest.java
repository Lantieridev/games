package ar.edu.utn.frc.tup.piii;

import ar.edu.utn.frc.tup.piii.model.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ConsoleUITest {

    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream captured;

    @BeforeEach
    public void captureOutput() {
        captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    public void restoreOutput() {
        System.setOut(originalOut);
    }

    private String output() {
        return captured.toString(StandardCharsets.UTF_8);
    }

    @Test
    public void testPrintBannerAndClearScreenProduceOutput() {
        ConsoleUI.printBanner();
        ConsoleUI.clearScreen();
        assertTrue(output().contains("E L E M E N T A L"));
    }

    @Test
    public void testRenderHandWithNullOrEmptyHandPrintsNothing() {
        ConsoleUI.renderHand(null);
        ConsoleUI.renderHand(new ArrayList<>());
        assertEquals("", output());
    }

    @Test
    public void testRenderHandPrintsEveryElementSymbolAndValue() {
        List<Card> hand = List.of(
            new Card(Element.FUEGO, 1),
            new Card(Element.AIRE, 2),
            new Card(Element.TIERRA, 3),
            new Card(Element.AGUA, 4)
        );
        ConsoleUI.renderHand(hand);
        String out = output();
        assertTrue(out.contains("[1]"));
        assertTrue(out.contains("[2]"));
        assertTrue(out.contains("[3]"));
        assertTrue(out.contains("[4]"));
        assertTrue(out.contains("(0)"));
        assertTrue(out.contains("(3)"));
    }

    @Test
    public void testRenderTableWithEmptyTable() {
        ConsoleUI.renderTable(new ArrayList<>());
        assertTrue(output().contains("Mesa Vacia"));
    }

    @Test
    public void testRenderTableWithCombinations() {
        List<Combination> table = new ArrayList<>();
        table.add(new Triad(new ArrayList<>(List.of(
            new Card(Element.FUEGO, 5), new Card(Element.AIRE, 5), new Card(Element.AGUA, 5)
        ))));
        ConsoleUI.renderTable(table);
        String out = output();
        assertTrue(out.contains("Tríada"));
        assertTrue(out.contains("15 pts"));
    }

    @Test
    public void testPrintVictoryAndDefeat() {
        ConsoleUI.printVictory("Ana", 42);
        ConsoleUI.printDefeat("Ana", 10);
        String out = output();
        assertTrue(out.contains("VICTORIA MAGISTRAL"));
        assertTrue(out.contains("42 pts"));
        assertTrue(out.contains("DERROTA ELEMENTAL"));
        assertTrue(out.contains("10 pts"));
    }
}
