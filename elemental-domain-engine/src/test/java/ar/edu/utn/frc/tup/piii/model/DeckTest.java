package ar.edu.utn.frc.tup.piii.model;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class DeckTest {

    @Test
    public void testDeckHas36UniqueCards() {
        Deck deck = new Deck(1L);
        Set<Card> seen = new HashSet<>();
        int count = 0;
        while (!deck.isEmpty()) {
            Card c = deck.draw();
            assertTrue(seen.add(c), "duplicate card drawn: " + c);
            count++;
        }
        assertEquals(36, count);
    }

    @Test
    public void testDeckContainsAllElementValueCombinations() {
        Deck deck = new Deck(2L);
        Set<Card> all = new HashSet<>();
        while (!deck.isEmpty()) all.add(deck.draw());

        for (Element e : Element.values()) {
            for (int v = 1; v <= 9; v++) {
                assertTrue(all.contains(new Card(e, v)), "missing " + e + " " + v);
            }
        }
    }

    @Test
    public void testDrawReturnsNullWhenEmpty() {
        Deck deck = new Deck(3L);
        while (!deck.isEmpty()) deck.draw();
        assertNull(deck.draw());
    }

    @Test
    public void testSizeDecreasesOnDraw() {
        Deck deck = new Deck(4L);
        assertEquals(36, deck.size());
        deck.draw();
        assertEquals(35, deck.size());
    }

    @Test
    public void testSameSeedProducesSameDrawOrder() {
        Deck a = new Deck(42L);
        Deck b = new Deck(42L);
        for (int i = 0; i < 36; i++) {
            assertEquals(a.draw(), b.draw());
        }
    }

    @Test
    public void testDifferentSeedsProduceDifferentOrder() {
        Deck a = new Deck(1L);
        Deck b = new Deck(2L);
        boolean anyDifferent = false;
        for (int i = 0; i < 36; i++) {
            if (!a.draw().equals(b.draw())) { anyDifferent = true; break; }
        }
        assertTrue(anyDifferent);
    }

    @Test
    public void testUnseededConstructorProducesAFullShuffledDeck() {
        Deck deck = new Deck();
        assertEquals(36, deck.size());
        assertFalse(deck.isEmpty());
    }
}
