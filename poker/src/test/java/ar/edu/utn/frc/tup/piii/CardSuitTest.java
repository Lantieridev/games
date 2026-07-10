package ar.edu.utn.frc.tup.piii;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CardSuitTest {

    @Test
    public void testAllSuitsHaveASymbol() {
        for (CardSuit suit : CardSuit.values()) {
            assertNotNull(suit.getSuitSymbol());
        }
    }

    @Test
    public void testValueOf_ReturnsCorrectEnum() {
        assertEquals(CardSuit.SPADES, CardSuit.valueOf("SPADES"));
    }
}
