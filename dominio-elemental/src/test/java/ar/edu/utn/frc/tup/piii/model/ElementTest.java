package ar.edu.utn.frc.tup.piii.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ElementTest {

    @Test
    public void testNextCyclesThroughAllElements() {
        assertEquals(Element.AIRE, Element.next(Element.FUEGO));
        assertEquals(Element.TIERRA, Element.next(Element.AIRE));
        assertEquals(Element.AGUA, Element.next(Element.TIERRA));
    }

    @Test
    public void testNextReturnsNullAfterLastElement() {
        assertNull(Element.next(Element.AGUA));
    }

    @Test
    public void testAllFourElementsExist() {
        assertEquals(4, Element.values().length);
    }
}
