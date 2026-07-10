package ar.edu.utn.frc.tup.piii;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PokerHandRankTest {

    @Test
    public void testOrdering_RoyalFlushIsHighestRank() {
        assertTrue(PokerHandRank.ROYAL_FLUSH.ordinal() > PokerHandRank.HIGH_CARD.ordinal());
        assertTrue(PokerHandRank.ROYAL_FLUSH.ordinal() > PokerHandRank.POKER.ordinal());
    }

    @Test
    public void testValueOf_ReturnsCorrectEnum() {
        assertEquals(PokerHandRank.FULL_HOUSE, PokerHandRank.valueOf("FULL_HOUSE"));
    }

    @Test
    public void testValues_HasTenRanks() {
        assertEquals(10, PokerHandRank.values().length);
    }
}
