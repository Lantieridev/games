package ar.edu.utn.frc.tup.piii.model;

import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class CombinationTest {

    // ── Triad ────────────────────────────────────────────────────────

    @Test
    public void testTriadScore() {
        List<Card> cards = Arrays.asList(
            new Card(Element.FUEGO, 5),
            new Card(Element.AIRE, 5),
            new Card(Element.AGUA, 5)
        );
        Triad triad = new Triad(cards);
        assertEquals(15, triad.calculateScore()); // 5 * 3
    }

    @Test
    public void testTriadGetName() {
        assertEquals("Tríada", new Triad(new ArrayList<>(Arrays.asList(
            new Card(Element.FUEGO, 5), new Card(Element.AIRE, 5), new Card(Element.AGUA, 5)
        ))).getName());
    }

    @Test
    public void testTriadCanAddMatchingValueDistinctElement() {
        Triad triad = new Triad(new ArrayList<>(Arrays.asList(
            new Card(Element.FUEGO, 5), new Card(Element.AIRE, 5)
        )));
        assertTrue(triad.canAdd(new Card(Element.AGUA, 5)));
    }

    @Test
    public void testTriadCanAddRejectsWrongValue() {
        Triad triad = new Triad(new ArrayList<>(Arrays.asList(
            new Card(Element.FUEGO, 5), new Card(Element.AIRE, 5)
        )));
        assertFalse(triad.canAdd(new Card(Element.AGUA, 6)));
    }

    @Test
    public void testTriadCanAddRejectsDuplicateElement() {
        Triad triad = new Triad(new ArrayList<>(Arrays.asList(
            new Card(Element.FUEGO, 5), new Card(Element.AIRE, 5)
        )));
        assertFalse(triad.canAdd(new Card(Element.FUEGO, 5)));
    }

    @Test
    public void testTriadCanAddRejectsWhenAlreadyFull() {
        Triad triad = new Triad(new ArrayList<>(Arrays.asList(
            new Card(Element.FUEGO, 5), new Card(Element.AIRE, 5),
            new Card(Element.AGUA, 5), new Card(Element.TIERRA, 5)
        )));
        assertFalse(triad.canAdd(new Card(Element.FUEGO, 5))); // size already 4
    }

    @Test
    public void testTriadScoreIsFlatRegardlessOfCardCount() {
        // Extending a Triad from 3 to 4 cards doesn't change its score —
        // this is the invariant the extend-combination scoring bug violated.
        Triad triad = new Triad(new ArrayList<>(Arrays.asList(
            new Card(Element.FUEGO, 5), new Card(Element.AIRE, 5), new Card(Element.AGUA, 5)
        )));
        int before = triad.calculateScore();
        triad.addCard(new Card(Element.TIERRA, 5));
        assertEquals(before, triad.calculateScore());
    }

    // ── Sequence ─────────────────────────────────────────────────────

    @Test
    public void testSequenceScore() {
        List<Card> cards = Arrays.asList(
            new Card(Element.FUEGO, 3),
            new Card(Element.FUEGO, 4),
            new Card(Element.FUEGO, 5)
        );
        Sequence seq = new Sequence(cards);
        assertEquals(12, seq.calculateScore()); // 3 + 4 + 5
    }

    @Test
    public void testSequenceGetName() {
        assertEquals("Secuencia", new Sequence(new ArrayList<>(Arrays.asList(
            new Card(Element.FUEGO, 3), new Card(Element.FUEGO, 4), new Card(Element.FUEGO, 5)
        ))).getName());
    }

    @Test
    public void testSequenceConstructorSortsCards() {
        List<Card> cards = new ArrayList<>(Arrays.asList(
            new Card(Element.FUEGO, 5), new Card(Element.FUEGO, 3), new Card(Element.FUEGO, 4)
        ));
        Sequence seq = new Sequence(cards);
        assertEquals(List.of(3, 4, 5), seq.getCards().stream().map(Card::value).toList());
    }

    @Test
    public void testSequenceCanAddExtendsAboveMax() {
        Sequence seq = new Sequence(new ArrayList<>(Arrays.asList(
            new Card(Element.FUEGO, 3), new Card(Element.FUEGO, 4), new Card(Element.FUEGO, 5)
        )));
        assertTrue(seq.canAdd(new Card(Element.FUEGO, 6)));
    }

    @Test
    public void testSequenceCanAddExtendsBelowMin() {
        Sequence seq = new Sequence(new ArrayList<>(Arrays.asList(
            new Card(Element.FUEGO, 3), new Card(Element.FUEGO, 4), new Card(Element.FUEGO, 5)
        )));
        assertTrue(seq.canAdd(new Card(Element.FUEGO, 2)));
    }

    @Test
    public void testSequenceCanAddRejectsWrongElement() {
        Sequence seq = new Sequence(new ArrayList<>(Arrays.asList(
            new Card(Element.FUEGO, 3), new Card(Element.FUEGO, 4), new Card(Element.FUEGO, 5)
        )));
        assertFalse(seq.canAdd(new Card(Element.AIRE, 6)));
    }

    @Test
    public void testSequenceCanAddRejectsNonAdjacentValue() {
        Sequence seq = new Sequence(new ArrayList<>(Arrays.asList(
            new Card(Element.FUEGO, 3), new Card(Element.FUEGO, 4), new Card(Element.FUEGO, 5)
        )));
        assertFalse(seq.canAdd(new Card(Element.FUEGO, 8)));
    }

    @Test
    public void testSequenceAddCardKeepsSortedOrder() {
        Sequence seq = new Sequence(new ArrayList<>(Arrays.asList(
            new Card(Element.FUEGO, 3), new Card(Element.FUEGO, 4), new Card(Element.FUEGO, 5)
        )));
        seq.addCard(new Card(Element.FUEGO, 2));
        assertEquals(List.of(2, 3, 4, 5), seq.getCards().stream().map(Card::value).toList());
        assertEquals(14, seq.calculateScore());
    }

    // ── ReactiveChain ────────────────────────────────────────────────

    @Test
    public void testReactiveChainScore() {
        List<Card> cards = Arrays.asList(
            new Card(Element.FUEGO, 1),
            new Card(Element.AIRE, 2),
            new Card(Element.TIERRA, 3),
            new Card(Element.AGUA, 4)
        );
        ReactiveChain chain = new ReactiveChain(cards);
        assertEquals(20, chain.calculateScore()); // (1+2+3+4) * 2
    }

    @Test
    public void testReactiveChainGetName() {
        assertEquals("Cadena Reactiva", new ReactiveChain(new ArrayList<>(Arrays.asList(
            new Card(Element.FUEGO, 1), new Card(Element.AIRE, 2), new Card(Element.TIERRA, 3), new Card(Element.AGUA, 4)
        ))).getName());
    }

    @Test
    public void testReactiveChainCannotBeExtended() {
        ReactiveChain chain = new ReactiveChain(new ArrayList<>(Arrays.asList(
            new Card(Element.FUEGO, 1), new Card(Element.AIRE, 2), new Card(Element.TIERRA, 3), new Card(Element.AGUA, 4)
        )));
        assertFalse(chain.canAdd(new Card(Element.FUEGO, 9)));
    }

    // ── Combination (base class behavior, exercised via a subclass) ────

    @Test
    public void testGetCardsReturnsBackingList() {
        List<Card> cards = new ArrayList<>(Arrays.asList(
            new Card(Element.FUEGO, 5), new Card(Element.AIRE, 5), new Card(Element.AGUA, 5)
        ));
        Triad triad = new Triad(cards);
        assertEquals(3, triad.getCards().size());
    }

    @Test
    public void testToStringIncludesNameCardsAndScore() {
        Triad triad = new Triad(new ArrayList<>(Arrays.asList(
            new Card(Element.FUEGO, 5), new Card(Element.AIRE, 5), new Card(Element.AGUA, 5)
        )));
        String str = triad.toString();
        assertTrue(str.startsWith("Tríada: "));
        assertTrue(str.contains("[FUEGO 5]"));
        assertTrue(str.contains("(Score: 15)"));
    }
}
