package ar.edu.utn.frc.tup.piii;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CardTest {

    @Test
    public void testToString_FormatsSymbolAndSuit() {
        Card card = new Card(CardSuit.SPADES, CardSymbol.ACE);
        assertEquals("[A♠]", card.toString());
    }

    @Test
    public void testEquals_SameSuitAndSymbol_ReturnsTrue() {
        Card card1 = new Card(CardSuit.HEARTS, CardSymbol.KING);
        Card card2 = new Card(CardSuit.HEARTS, CardSymbol.KING);
        assertEquals(card1, card2);
    }

    @Test
    public void testEquals_DifferentSuit_ReturnsFalse() {
        Card card1 = new Card(CardSuit.HEARTS, CardSymbol.KING);
        Card card2 = new Card(CardSuit.SPADES, CardSymbol.KING);
        assertNotEquals(card1, card2);
    }

    @Test
    public void testEquals_DifferentSymbol_ReturnsFalse() {
        Card card1 = new Card(CardSuit.HEARTS, CardSymbol.KING);
        Card card2 = new Card(CardSuit.HEARTS, CardSymbol.QUEEN);
        assertNotEquals(card1, card2);
    }

    @Test
    public void testEquals_Null_ReturnsFalse() {
        Card card = new Card(CardSuit.HEARTS, CardSymbol.KING);
        assertNotEquals(null, card);
    }

    @Test
    public void testEquals_DifferentClass_ReturnsFalse() {
        Card card = new Card(CardSuit.HEARTS, CardSymbol.KING);
        assertNotEquals("not a card", card);
    }

    @Test
    public void testEquals_SameInstance_ReturnsTrue() {
        Card card = new Card(CardSuit.HEARTS, CardSymbol.KING);
        assertEquals(card, card);
    }

    @Test
    public void testSettersAndGetters() {
        Card card = new Card(CardSuit.CLUBS, CardSymbol.TWO);
        card.setCardSuit(CardSuit.DIAMONDS);
        card.setCardSymbol(CardSymbol.THREE);
        assertEquals(CardSuit.DIAMONDS, card.getCardSuit());
        assertEquals(CardSymbol.THREE, card.getCardSymbol());
    }
}
