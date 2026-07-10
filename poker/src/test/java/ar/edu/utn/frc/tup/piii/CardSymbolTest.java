package ar.edu.utn.frc.tup.piii;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CardSymbolTest {

    @Test
    public void testAceValueAndSymbol() {
        assertEquals("A", CardSymbol.ACE.getSymbol());
        assertEquals(14, CardSymbol.ACE.getValue());
    }

    @Test
    public void testTenValueAndSymbol() {
        assertEquals("10", CardSymbol.TEN.getSymbol());
        assertEquals(10, CardSymbol.TEN.getValue());
    }

    @Test
    public void testKingValueAndSymbol() {
        assertEquals("K", CardSymbol.KING.getSymbol());
        assertEquals(13, CardSymbol.KING.getValue());
    }
}
