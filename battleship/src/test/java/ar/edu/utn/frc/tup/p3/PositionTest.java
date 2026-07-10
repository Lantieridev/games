package ar.edu.utn.frc.tup.p3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PositionTest {

    @Test
    void testEquals_SameRowAndColumn_ReturnsTrue() {
        Position a = new Position(3, 5);
        Position b = new Position(3, 5);
        assertEquals(a, b);
    }

    @Test
    void testEquals_DifferentRow_ReturnsFalse() {
        Position a = new Position(3, 5);
        Position b = new Position(4, 5);
        assertNotEquals(a, b);
    }

    @Test
    void testEquals_DifferentColumn_ReturnsFalse() {
        Position a = new Position(3, 5);
        Position b = new Position(3, 6);
        assertNotEquals(a, b);
    }

    @Test
    void testEquals_Null_ReturnsFalse() {
        // This used to throw a NullPointerException instead of returning false -
        // equals() cast obj without checking it first.
        Position a = new Position(3, 5);
        assertNotEquals(null, a);
    }

    @Test
    void testEquals_DifferentClass_ReturnsFalse() {
        Position a = new Position(3, 5);
        assertNotEquals("not a position", a);
    }

    @Test
    void testEquals_SameInstance_ReturnsTrue() {
        Position a = new Position(3, 5);
        assertEquals(a, a);
    }

    @Test
    void testHashCode_EqualPositions_HaveEqualHashCodes() {
        // equals() was overridden without hashCode(), which breaks HashSet/HashMap
        // usage of Position - two "equal" positions could land in different buckets.
        Position a = new Position(3, 5);
        Position b = new Position(3, 5);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testGettersAndSetters() {
        Position position = new Position();
        position.setRow(2);
        position.setColumn(7);
        assertEquals(2, position.getRow());
        assertEquals(7, position.getColumn());
    }
}
