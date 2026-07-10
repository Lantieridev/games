package ar.edu.utn.frc.tup.piii.model;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class MachinePlayerTest {

    @BeforeAll
    public static void disableThinkingDelay() {
        MachinePlayer.setThinkingDelayEnabled(false);
    }

    @Test
    public void testPlayTurnFormsATriadFromHand() {
        MachinePlayer ai = new MachinePlayer("IA");
        ai.addCard(new Card(Element.FUEGO, 5));
        ai.addCard(new Card(Element.AIRE, 5));
        ai.addCard(new Card(Element.AGUA, 5));
        List<Combination> table = new ArrayList<>();

        ai.playTurn(new Deck(1L), table);

        assertEquals(1, table.size());
        assertEquals("Tríada", table.get(0).getName());
        assertEquals(15, ai.getScore());
    }

    @Test
    public void testPlayTurnFormsASequenceFromHand() {
        MachinePlayer ai = new MachinePlayer("IA");
        ai.addCard(new Card(Element.FUEGO, 3));
        ai.addCard(new Card(Element.FUEGO, 4));
        ai.addCard(new Card(Element.FUEGO, 5));
        List<Combination> table = new ArrayList<>();

        ai.playTurn(new Deck(2L), table);

        assertEquals(1, table.size());
        assertEquals("Secuencia", table.get(0).getName());
        assertEquals(12, ai.getScore());
    }

    @Test
    public void testPlayTurnPrefersReactiveChainOverTriad() {
        MachinePlayer ai = new MachinePlayer("IA");
        // A reactive chain (one of each element) AND a same-value triad candidate overlap.
        ai.addCard(new Card(Element.FUEGO, 3));
        ai.addCard(new Card(Element.AIRE, 3));
        ai.addCard(new Card(Element.TIERRA, 3));
        ai.addCard(new Card(Element.AGUA, 3));
        List<Combination> table = new ArrayList<>();

        ai.playTurn(new Deck(3L), table);

        assertEquals("Cadena Reactiva", table.get(0).getName());
        assertEquals(24, ai.getScore()); // (3+3+3+3) * 2
    }

    @Test
    public void testThinkingDelayActuallySleepsWhenEnabled() {
        MachinePlayer.setThinkingDelayEnabled(true);
        try {
            MachinePlayer ai = new MachinePlayer("IA");
            long start = System.currentTimeMillis();
            ai.playTurn(new Deck(30L), new ArrayList<>());
            assertTrue(System.currentTimeMillis() - start >= 900);
        } finally {
            MachinePlayer.setThinkingDelayEnabled(false);
        }
    }

    @Test
    public void testFindSequenceDiscardsATooShortRunBeforeAGap() {
        MachinePlayer ai = new MachinePlayer("IA");
        // 1,2 (a 2-run, discarded on the gap) then 5,6,7 (the qualifying run).
        ai.addCard(new Card(Element.FUEGO, 1));
        ai.addCard(new Card(Element.FUEGO, 2));
        ai.addCard(new Card(Element.FUEGO, 5));
        ai.addCard(new Card(Element.FUEGO, 6));
        ai.addCard(new Card(Element.FUEGO, 7));
        List<Combination> table = new ArrayList<>();

        ai.playTurn(new Deck(31L), table);

        assertEquals(1, table.size());
        assertEquals(List.of(5, 6, 7), table.get(0).getCards().stream().map(Card::value).sorted().toList());
    }

    @Test
    public void testFindSequenceSkipsOverAGapInValues() {
        MachinePlayer ai = new MachinePlayer("IA");
        // 1,2,3 then a gap, then 7,8,9 — two separate 3-runs, not one run of 6.
        ai.addCard(new Card(Element.FUEGO, 1));
        ai.addCard(new Card(Element.FUEGO, 2));
        ai.addCard(new Card(Element.FUEGO, 3));
        ai.addCard(new Card(Element.FUEGO, 7));
        ai.addCard(new Card(Element.FUEGO, 8));
        ai.addCard(new Card(Element.FUEGO, 9));
        List<Combination> table = new ArrayList<>();

        ai.playTurn(new Deck(4L), table);

        // Both 3-runs qualify and get played within the same turn — findAndPlayCombinations
        // loops until no more combos are found.
        assertTrue(table.stream().allMatch(c -> c.getName().equals("Secuencia")));
        assertEquals(2, table.size());
    }

    @Test
    public void testExtendingASequenceAwardsTheCardValue() {
        MachinePlayer ai = new MachinePlayer("IA");
        List<Combination> table = new ArrayList<>();
        table.add(new Sequence(new ArrayList<>(List.of(
            new Card(Element.FUEGO, 3), new Card(Element.FUEGO, 4), new Card(Element.FUEGO, 5)
        ))));
        ai.addCard(new Card(Element.FUEGO, 6)); // extends the sequence to 3-4-5-6

        ai.playTurn(new Deck(5L), table);

        assertEquals(1, table.size());
        assertEquals(4, table.get(0).getCards().size());
        assertEquals(6, ai.getScore()); // delta == the extending card's own value
    }

    @Test
    public void testExtendingATriadAwardsNoBonusPoints() {
        MachinePlayer ai = new MachinePlayer("IA");
        List<Combination> table = new ArrayList<>();
        table.add(new Triad(new ArrayList<>(List.of(
            new Card(Element.FUEGO, 5), new Card(Element.AIRE, 5)
        ))));
        ai.addCard(new Card(Element.AGUA, 5)); // extends the triad to 3 elements

        ai.playTurn(new Deck(6L), table);

        assertEquals(1, table.size());
        assertEquals(3, table.get(0).getCards().size());
        // Triad.calculateScore() is value*3 regardless of card count — extending
        // it doesn't legitimately earn extra points, unlike a Sequence.
        assertEquals(0, ai.getScore());
    }

    @Test
    public void testDiscardReturnsNullWhenHandIsSmall() {
        MachinePlayer ai = new MachinePlayer("IA");
        ai.addCard(new Card(Element.FUEGO, 1));
        assertNull(ai.discard());
    }

    @Test
    public void testDiscardRemovesLowestValueCardWhenHandExceedsSeven() {
        MachinePlayer ai = new MachinePlayer("IA");
        for (int v = 1; v <= 8; v++) ai.addCard(new Card(Element.FUEGO, v));

        Card discarded = ai.discard();

        assertEquals(new Card(Element.FUEGO, 1), discarded);
        assertEquals(7, ai.getHand().size());
        assertFalse(ai.getHand().contains(discarded));
    }
}
