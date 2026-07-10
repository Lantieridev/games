package ar.edu.utn.frc.tup.lciii;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeckTest {

    @Test
    void createDeckTest() {
        Deck deck = new Deck();
        assertEquals(40, deck.getCards().size());
        for (Card card : deck.getCards()) {
            assertNotEquals(8, card.getNumber());
            assertNotEquals(9, card.getNumber());
        }
    }

    @Test
    void takeCardTest() {
        Deck deck = new Deck();
        int initialSize = deck.getCards().size();
        Card topCard = deck.getCards().peek();
        Card takenCard = deck.takeCard();
        assertEquals(initialSize - 1, deck.getCards().size());
        assertEquals(topCard, takenCard);
    }

    @Test
    void isEmptyTest() {
        Deck deck = new Deck();
        assertFalse(deck.isEmpty());
        while (!deck.isEmpty()) {
            deck.takeCard();
        }
        assertTrue(deck.isEmpty());
    }

    @Test
    void shuffleDeckTest() {
        Deck deck = new Deck();
        assertEquals(40, deck.getCards().size());
    }
}