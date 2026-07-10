package ar.edu.utn.frc.tup.piii;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class HandEvaluatorTest {

    @BeforeEach
    public void setup() {
        LetterByLetterPrinter.DISABLE_COLORS = true;
    }
    
    @Test
    public void testHasConsecutiveSequence_WithConsecutiveSequence_ReturnsTrue() {
        
        List<Card> cards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.TEN),
                new Card(CardSuit.CLUBS, CardSymbol.JACK),
                new Card(CardSuit.DIAMONDS, CardSymbol.QUEEN),
                new Card(CardSuit.SPADES, CardSymbol.KING),
                new Card(CardSuit.HEARTS, CardSymbol.ACE)
        );

        
        assertTrue(HandEvaluator.isStraight(cards));
    }

    @Test
    public void testHasConsecutiveSequence_WithLowAceSequence_ReturnsTrue() {
        
        List<Card> cards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.ACE),
                new Card(CardSuit.CLUBS, CardSymbol.TWO),
                new Card(CardSuit.DIAMONDS, CardSymbol.THREE),
                new Card(CardSuit.SPADES, CardSymbol.FOUR),
                new Card(CardSuit.HEARTS, CardSymbol.FIVE)
        );

        
        assertTrue(HandEvaluator.isStraight(cards));
    }

    @Test
    public void testHasConsecutiveSequence_WithMiddleSequence_ReturnsTrue() {
        
        List<Card> cards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.FIVE),
                new Card(CardSuit.CLUBS, CardSymbol.SIX),
                new Card(CardSuit.DIAMONDS, CardSymbol.SEVEN),
                new Card(CardSuit.SPADES, CardSymbol.EIGHT),
                new Card(CardSuit.HEARTS, CardSymbol.NINE)
        );

        
        assertTrue(HandEvaluator.isStraight(cards));
    }

    @Test
    public void testHasConsecutiveSequence_WithNonConsecutiveSequence_ReturnsFalse() {
        
        List<Card> cards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.TWO),
                new Card(CardSuit.CLUBS, CardSymbol.FOUR),
                new Card(CardSuit.DIAMONDS, CardSymbol.SIX),
                new Card(CardSuit.SPADES, CardSymbol.EIGHT),
                new Card(CardSuit.HEARTS, CardSymbol.TEN)
        );

        
        assertFalse(HandEvaluator.isStraight(cards));
    }

    @Test
    public void testHasConsecutiveSequence_WithInvalidWraparoundSequence_ReturnsFalse() {
        
        List<Card> cards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.QUEEN),
                new Card(CardSuit.CLUBS, CardSymbol.KING),
                new Card(CardSuit.DIAMONDS, CardSymbol.ACE),
                new Card(CardSuit.SPADES, CardSymbol.TWO),
                new Card(CardSuit.HEARTS, CardSymbol.THREE)
        );

        
        assertFalse(HandEvaluator.isStraight(cards));
    }

    @Test
    public void testHasConsecutiveSequence_WithDuplicateValues_ReturnsFalse() {
        
        List<Card> cards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.SEVEN),
                new Card(CardSuit.CLUBS, CardSymbol.EIGHT),
                new Card(CardSuit.DIAMONDS, CardSymbol.NINE),
                new Card(CardSuit.SPADES, CardSymbol.TEN),
                new Card(CardSuit.HEARTS, CardSymbol.TEN)
        );

        
        assertFalse(HandEvaluator.isStraight(cards));
    }

    @Test
    public void testHasConsecutiveSequence_WithLessThanFiveCards_ReturnsFalse() {
        
        List<Card> cards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.SEVEN),
                new Card(CardSuit.CLUBS, CardSymbol.EIGHT),
                new Card(CardSuit.DIAMONDS, CardSymbol.NINE),
                new Card(CardSuit.SPADES, CardSymbol.TEN)
        );

        
        assertFalse(HandEvaluator.isStraight(cards));
    }

    @Test
    public void testCountGroupsOfSameSymbol_WithOnePair_ReturnsOne() {
        List<Card> cards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.SEVEN),
                new Card(CardSuit.CLUBS, CardSymbol.SEVEN),
                new Card(CardSuit.DIAMONDS, CardSymbol.TWO),
                new Card(CardSuit.SPADES, CardSymbol.FIVE),
                new Card(CardSuit.HEARTS, CardSymbol.KING)
        );

        
        int pairCount = HandEvaluator.countPairs(cards);

        
        assertEquals(1, pairCount);
    }

    @Test
    public void testCountGroupsOfSameSymbol_WithTwoPairs_ReturnsTwo() {
        
        List<Card> cards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.SEVEN),
                new Card(CardSuit.CLUBS, CardSymbol.SEVEN),
                new Card(CardSuit.DIAMONDS, CardSymbol.TWO),
                new Card(CardSuit.SPADES, CardSymbol.TWO),
                new Card(CardSuit.HEARTS, CardSymbol.KING)
        );

        
        int pairCount = HandEvaluator.countPairs(cards);

        
        assertEquals(2, pairCount);
    }

    @Test
    public void testCountGroupsOfSameSymbol_WithTrio_ReturnsZeroPairs() {
        
        List<Card> cards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.SEVEN),
                new Card(CardSuit.CLUBS, CardSymbol.SEVEN),
                new Card(CardSuit.DIAMONDS, CardSymbol.SEVEN),
                new Card(CardSuit.SPADES, CardSymbol.TWO),
                new Card(CardSuit.HEARTS, CardSymbol.KING)
        );

        
        int pairCount = HandEvaluator.countPairs(cards);

        
        assertEquals(0, pairCount);
    }

    @Test
    public void testCountGroupsOfSameSymbol_WithOneTrio_ReturnsOne() {
        
        List<Card> cards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.SEVEN),
                new Card(CardSuit.CLUBS, CardSymbol.SEVEN),
                new Card(CardSuit.DIAMONDS, CardSymbol.SEVEN),
                new Card(CardSuit.SPADES, CardSymbol.TWO),
                new Card(CardSuit.HEARTS, CardSymbol.KING)
        );

        
        int trioCount = HandEvaluator.countTrios(cards);

        
        assertEquals(1, trioCount);
    }

    @Test
    public void testCountGroupsOfSameSymbol_WithTwoTrios_ReturnsTwo() {
        List<Card> cards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.SEVEN),
                new Card(CardSuit.CLUBS, CardSymbol.SEVEN),
                new Card(CardSuit.DIAMONDS, CardSymbol.SEVEN),
                new Card(CardSuit.SPADES, CardSymbol.TWO),
                new Card(CardSuit.HEARTS, CardSymbol.TWO),
                new Card(CardSuit.CLUBS, CardSymbol.TWO)
        );

        
        int trioCount = HandEvaluator.countTrios(cards);

        
        assertEquals(2, trioCount);
    }

    @Test
    public void testCountGroupsOfSameSymbol_WithPoker_ReturnsZeroPairs() {
        
        List<Card> cards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.SEVEN),
                new Card(CardSuit.CLUBS, CardSymbol.SEVEN),
                new Card(CardSuit.DIAMONDS, CardSymbol.SEVEN),
                new Card(CardSuit.SPADES, CardSymbol.SEVEN),
                new Card(CardSuit.HEARTS, CardSymbol.KING)
        );

        
        int pairCount = HandEvaluator.countPairs(cards);

        
        assertEquals(0, pairCount);
    }

    @Test
    public void testCountGroupsOfSameSymbol_WithPoker_ReturnsZeroTrios() {
        
        List<Card> cards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.SEVEN),
                new Card(CardSuit.CLUBS, CardSymbol.SEVEN),
                new Card(CardSuit.DIAMONDS, CardSymbol.SEVEN),
                new Card(CardSuit.SPADES, CardSymbol.SEVEN),
                new Card(CardSuit.HEARTS, CardSymbol.KING)
        );

        
        int trioCount = HandEvaluator.countTrios(cards);

        
        assertEquals(0, trioCount);
    }

    @Test
    public void testCountGroupsOfSameSymbol_WithFullHouse_ReturnsOnePairAndOneTrio() {
        
        List<Card> cards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.SEVEN),
                new Card(CardSuit.CLUBS, CardSymbol.SEVEN),
                new Card(CardSuit.DIAMONDS, CardSymbol.SEVEN),
                new Card(CardSuit.SPADES, CardSymbol.TWO),
                new Card(CardSuit.HEARTS, CardSymbol.TWO)
        );

        
        int pairCount = HandEvaluator.countPairs(cards);
        int trioCount = HandEvaluator.countTrios(cards);

        
        assertEquals(1, pairCount);
        assertEquals(1, trioCount);
    }

    @Test
    public void testCountGroupsOfSameSymbol_WithEmptyHand_ReturnsZero() {
        
        List<Card> cards = new ArrayList<>();

        
        int pairCount = HandEvaluator.countPairs(cards);
        int trioCount = HandEvaluator.countTrios(cards);

        
        assertEquals(0, pairCount);
        assertEquals(0, trioCount);
    }

    @Test
    public void testIsFlush_WithAllSameSuit_ReturnsTrue() {
        
        List<Card> cards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.TWO),
                new Card(CardSuit.HEARTS, CardSymbol.FIVE),
                new Card(CardSuit.HEARTS, CardSymbol.SEVEN),
                new Card(CardSuit.HEARTS, CardSymbol.TEN),
                new Card(CardSuit.HEARTS, CardSymbol.KING)
        );

        
        assertTrue(HandEvaluator.isFlush(cards));
    }

    @Test
    public void testIsFlush_WithDifferentSuits_ReturnsFalse() {
        
        List<Card> cards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.TWO),
                new Card(CardSuit.CLUBS, CardSymbol.FIVE),
                new Card(CardSuit.HEARTS, CardSymbol.SEVEN),
                new Card(CardSuit.HEARTS, CardSymbol.TEN),
                new Card(CardSuit.HEARTS, CardSymbol.KING)
        );

        
        assertFalse(HandEvaluator.isFlush(cards));
    }

    @Test
    public void testIsFlush_WithSingleCard_ReturnsTrue() {
        List<Card> cards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.ACE)
        );

        assertTrue(HandEvaluator.isFlush(cards));
    }

    @Test
    public void testIsFlush_WithMoreThanFiveCards_ConsidersAllCards() {
        List<Card> cards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.TWO),
                new Card(CardSuit.HEARTS, CardSymbol.FOUR),
                new Card(CardSuit.HEARTS, CardSymbol.SIX),
                new Card(CardSuit.HEARTS, CardSymbol.EIGHT),
                new Card(CardSuit.HEARTS, CardSymbol.TEN),
                new Card(CardSuit.HEARTS, CardSymbol.QUEEN),
                new Card(CardSuit.HEARTS, CardSymbol.ACE)
        );

        assertTrue(HandEvaluator.isFlush(cards));

        List<Card> mixedCards = List.of(
                new Card(CardSuit.HEARTS, CardSymbol.TWO),
                new Card(CardSuit.HEARTS, CardSymbol.FOUR),
                new Card(CardSuit.HEARTS, CardSymbol.SIX),
                new Card(CardSuit.HEARTS, CardSymbol.EIGHT),
                new Card(CardSuit.HEARTS, CardSymbol.TEN),
                new Card(CardSuit.HEARTS, CardSymbol.QUEEN),
                new Card(CardSuit.CLUBS, CardSymbol.ACE)
        );

        assertFalse(HandEvaluator.isFlush(mixedCards));
    }
}
