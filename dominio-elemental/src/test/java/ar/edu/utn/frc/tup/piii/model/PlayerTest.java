package ar.edu.utn.frc.tup.piii.model;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    /** Minimal concrete subclass to exercise Player's own base behavior in isolation. */
    private static class TestPlayer extends Player {
        TestPlayer(String name) { super(name); }
        @Override public void playTurn(Deck deck, List<Combination> table) {}
        @Override public Card discard() { return null; }
    }

    @Test
    public void testConstructorInitializesEmptyHandAndZeroScore() {
        Player p = new TestPlayer("Ana");
        assertEquals("Ana", p.getName());
        assertTrue(p.getHand().isEmpty());
        assertEquals(0, p.getScore());
    }

    @Test
    public void testAddCardAppendsToHand() {
        Player p = new TestPlayer("Ana");
        p.addCard(new Card(Element.FUEGO, 1));
        assertEquals(1, p.getHand().size());
    }

    @Test
    public void testAddCardIgnoresNull() {
        Player p = new TestPlayer("Ana");
        p.addCard(null);
        assertTrue(p.getHand().isEmpty());
    }

    @Test
    public void testRemoveCardsRemovesMatchingEntries() {
        Player p = new TestPlayer("Ana");
        Card a = new Card(Element.FUEGO, 1);
        Card b = new Card(Element.AIRE, 2);
        p.addCard(a);
        p.addCard(b);
        p.removeCards(List.of(a));
        assertEquals(List.of(b), p.getHand());
    }

    @Test
    public void testAddScoreAccumulates() {
        Player p = new TestPlayer("Ana");
        p.addScore(10);
        p.addScore(5);
        assertEquals(15, p.getScore());
    }
}
