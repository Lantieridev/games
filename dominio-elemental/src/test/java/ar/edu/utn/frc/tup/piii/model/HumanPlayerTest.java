package ar.edu.utn.frc.tup.piii.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import static org.junit.jupiter.api.Assertions.*;

public class HumanPlayerTest {

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private ByteArrayOutputStream capturedOut;

    private HumanPlayer playerWithInput(String script) {
        System.setIn(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)));
        capturedOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut, true, StandardCharsets.UTF_8));
        return new HumanPlayer("Tester", new Scanner(System.in));
    }

    @AfterEach
    public void restoreStreams() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    @Test
    public void testTerminatingTurnImmediatelyDrawsAndStops() {
        HumanPlayer human = playerWithInput("0\n");
        List<Combination> table = new ArrayList<>();
        Deck deck = new Deck(10L);
        int before = deck.size();

        human.playTurn(deck, table);

        assertEquals(1, human.getHand().size());
        assertEquals(before - 1, deck.size());
        assertTrue(table.isEmpty());
    }

    @Test
    public void testInvalidMenuChoiceDoesNotCrashAndReprompts() {
        HumanPlayer human = playerWithInput("abc\n0\n");
        List<Combination> table = new ArrayList<>();

        assertDoesNotThrow(() -> human.playTurn(new Deck(11L), table));
        assertTrue(capturedOut.toString(StandardCharsets.UTF_8).contains("Opcion invalida"));
    }

    @Test
    public void testCreateValidTriadFromHand() {
        HumanPlayer human = playerWithInput("1\n0,1,2\n0\n");
        human.addCard(new Card(Element.FUEGO, 5));
        human.addCard(new Card(Element.AIRE, 5));
        human.addCard(new Card(Element.AGUA, 5));
        List<Combination> table = new ArrayList<>();

        human.playTurn(new Deck(12L), table);

        assertEquals(1, table.size());
        assertEquals("Tríada", table.get(0).getName());
        assertEquals(15, human.getScore());
        // The 3 cards used for the triad are gone; only the drawn card remains.
        assertEquals(1, human.getHand().size());
    }

    @Test
    public void testCreateInvalidCombinationShowsMessageAndKeepsHand() {
        HumanPlayer human = playerWithInput("1\n0,1\n0\n"); // only 2 cards selected, < 3 minimum
        human.addCard(new Card(Element.FUEGO, 5));
        human.addCard(new Card(Element.AIRE, 6));
        List<Combination> table = new ArrayList<>();

        human.playTurn(new Deck(13L), table);

        assertTrue(table.isEmpty());
        assertEquals(0, human.getScore());
        assertTrue(capturedOut.toString(StandardCharsets.UTF_8).contains("Combinacion invalida"));
    }

    @Test
    public void testCreateCombinationWithMalformedIndicesShowsErrorInsteadOfCrashing() {
        HumanPlayer human = playerWithInput("1\nabc\n0\n");
        human.addCard(new Card(Element.FUEGO, 5));
        List<Combination> table = new ArrayList<>();

        assertDoesNotThrow(() -> human.playTurn(new Deck(14L), table));
        assertTrue(capturedOut.toString(StandardCharsets.UTF_8).contains("Error al seleccionar cartas"));
    }

    @Test
    public void testExtendSequenceAwardsCardValueAsScore() {
        HumanPlayer human = playerWithInput("2\n0\n0\n0\n");
        human.addCard(new Card(Element.FUEGO, 6));
        List<Combination> table = new ArrayList<>();
        table.add(new Sequence(new ArrayList<>(List.of(
            new Card(Element.FUEGO, 3), new Card(Element.FUEGO, 4), new Card(Element.FUEGO, 5)
        ))));

        human.playTurn(new Deck(15L), table);

        assertEquals(4, table.get(0).getCards().size());
        assertEquals(6, human.getScore());
    }

    @Test
    public void testExtendTriadAwardsNoBonusPoints() {
        HumanPlayer human = playerWithInput("2\n0\n0\n0\n");
        human.addCard(new Card(Element.AGUA, 5));
        List<Combination> table = new ArrayList<>();
        table.add(new Triad(new ArrayList<>(List.of(
            new Card(Element.FUEGO, 5), new Card(Element.AIRE, 5)
        ))));

        human.playTurn(new Deck(16L), table);

        assertEquals(3, table.get(0).getCards().size());
        assertEquals(0, human.getScore());
    }

    @Test
    public void testExtendWithNoTableEntriesShowsMessage() {
        HumanPlayer human = playerWithInput("2\n0\n");
        List<Combination> table = new ArrayList<>();

        human.playTurn(new Deck(17L), table);

        assertTrue(capturedOut.toString(StandardCharsets.UTF_8).contains("No hay combinaciones en la mesa"));
    }

    @Test
    public void testExtendWithOutOfRangeTableIndexDoesNotCrash() {
        HumanPlayer human = playerWithInput("2\n99\n0\n");
        human.addCard(new Card(Element.FUEGO, 6));
        List<Combination> table = new ArrayList<>();
        table.add(new Sequence(new ArrayList<>(List.of(
            new Card(Element.FUEGO, 3), new Card(Element.FUEGO, 4), new Card(Element.FUEGO, 5)
        ))));

        assertDoesNotThrow(() -> human.playTurn(new Deck(18L), table));
        assertTrue(capturedOut.toString(StandardCharsets.UTF_8).contains("Indice de combinacion invalido"));
        assertEquals(3, table.get(0).getCards().size()); // unchanged
    }

    @Test
    public void testExtendWithOutOfRangeHandIndexDoesNotCrash() {
        HumanPlayer human = playerWithInput("2\n0\n99\n0\n");
        human.addCard(new Card(Element.FUEGO, 6));
        List<Combination> table = new ArrayList<>();
        table.add(new Sequence(new ArrayList<>(List.of(
            new Card(Element.FUEGO, 3), new Card(Element.FUEGO, 4), new Card(Element.FUEGO, 5)
        ))));

        assertDoesNotThrow(() -> human.playTurn(new Deck(19L), table));
        assertTrue(capturedOut.toString(StandardCharsets.UTF_8).contains("Indice de carta invalido"));
    }

    @Test
    public void testExtendWithNonFittingCardShowsMessage() {
        HumanPlayer human = playerWithInput("2\n0\n0\n0\n");
        human.addCard(new Card(Element.FUEGO, 9)); // doesn't fit a 3-4-5 sequence
        List<Combination> table = new ArrayList<>();
        table.add(new Sequence(new ArrayList<>(List.of(
            new Card(Element.FUEGO, 3), new Card(Element.FUEGO, 4), new Card(Element.FUEGO, 5)
        ))));

        human.playTurn(new Deck(20L), table);

        assertTrue(capturedOut.toString(StandardCharsets.UTF_8).contains("no encaja"));
        assertEquals(3, table.get(0).getCards().size());
    }

    @Test
    public void testCreateValidReactiveChainFromHand() {
        HumanPlayer human = playerWithInput("1\n0,1,2,3\n0\n");
        human.addCard(new Card(Element.FUEGO, 1));
        human.addCard(new Card(Element.AIRE, 2));
        human.addCard(new Card(Element.TIERRA, 3));
        human.addCard(new Card(Element.AGUA, 4));
        List<Combination> table = new ArrayList<>();

        human.playTurn(new Deck(21L), table);

        assertEquals(1, table.size());
        assertEquals("Cadena Reactiva", table.get(0).getName());
        assertEquals(20, human.getScore()); // (1+2+3+4) * 2
    }

    @Test
    public void testCreateCombinationThatMatchesNoValidTypeIsRejected() {
        // 3 cards, different values, different elements, non-consecutive — not a
        // Triad (values differ), not a Sequence (elements differ), not a Chain (size != 4).
        HumanPlayer human = playerWithInput("1\n0,1,2\n0\n");
        human.addCard(new Card(Element.FUEGO, 1));
        human.addCard(new Card(Element.AIRE, 5));
        human.addCard(new Card(Element.TIERRA, 9));
        List<Combination> table = new ArrayList<>();

        human.playTurn(new Deck(22L), table);

        assertTrue(table.isEmpty());
        assertTrue(capturedOut.toString(StandardCharsets.UTF_8).contains("Combinacion invalida"));
    }

    @Test
    public void testDiscardReturnsNullWhenHandIsSmall() {
        HumanPlayer human = playerWithInput("");
        human.addCard(new Card(Element.FUEGO, 1));
        assertNull(human.discard());
    }

    @Test
    public void testDiscardRemovesTheChosenCard() {
        HumanPlayer human = playerWithInput("2\n");
        for (int v = 1; v <= 8; v++) human.addCard(new Card(Element.FUEGO, v));

        Card discarded = human.discard();

        assertEquals(new Card(Element.FUEGO, 3), discarded);
        assertEquals(7, human.getHand().size());
    }

    @Test
    public void testDiscardWithInvalidIndexDoesNotCrash() {
        HumanPlayer human = playerWithInput("99\n");
        for (int v = 1; v <= 8; v++) human.addCard(new Card(Element.FUEGO, v));

        Card discarded = assertDoesNotThrow(human::discard);

        assertNull(discarded);
        assertEquals(8, human.getHand().size()); // nothing removed
    }

    @Test
    public void testDiscardWithNonNumericIndexDoesNotCrash() {
        HumanPlayer human = playerWithInput("nope\n");
        for (int v = 1; v <= 8; v++) human.addCard(new Card(Element.FUEGO, v));

        assertDoesNotThrow(human::discard);
    }
}
