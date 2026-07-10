package ar.edu.utn.frc.tup.piii.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CardTest {

    @Test
    public void testAccessors() {
        Card card = new Card(Element.FUEGO, 7);
        assertEquals(Element.FUEGO, card.element());
        assertEquals(7, card.value());
    }

    @Test
    public void testToString() {
        Card card = new Card(Element.AGUA, 3);
        assertEquals("[AGUA 3]", card.toString());
    }

    @Test
    public void testRecordEquality() {
        assertEquals(new Card(Element.TIERRA, 5), new Card(Element.TIERRA, 5));
        assertNotEquals(new Card(Element.TIERRA, 5), new Card(Element.TIERRA, 6));
        assertNotEquals(new Card(Element.TIERRA, 5), new Card(Element.AIRE, 5));
    }
}
