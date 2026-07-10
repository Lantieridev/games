package ar.edu.utn.frc.tup.p3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ShipTest {

    @Test
    void testEquals_SamePosition_ReturnsTrue() {
        // Was previously a stub that always returned true regardless of position -
        // wasn't actively hit by the current game logic (which compares Positions
        // directly, not Ships), but was still broken public API.
        Ship a = new Ship(new Position(1, 1), ShipStatus.AFLOAT);
        Ship b = new Ship(new Position(1, 1), ShipStatus.SUNKEN);
        assertEquals(a, b);
    }

    @Test
    void testEquals_DifferentPosition_ReturnsFalse() {
        Ship a = new Ship(new Position(1, 1), ShipStatus.AFLOAT);
        Ship b = new Ship(new Position(2, 2), ShipStatus.AFLOAT);
        assertNotEquals(a, b);
    }

    @Test
    void testEquals_Null_ReturnsFalse() {
        Ship a = new Ship(new Position(1, 1), ShipStatus.AFLOAT);
        assertNotEquals(null, a);
    }

    @Test
    void testEquals_DifferentClass_ReturnsFalse() {
        Ship a = new Ship(new Position(1, 1), ShipStatus.AFLOAT);
        assertNotEquals("not a ship", a);
    }

    @Test
    void testHashCode_EqualShips_HaveEqualHashCodes() {
        Ship a = new Ship(new Position(1, 1), ShipStatus.AFLOAT);
        Ship b = new Ship(new Position(1, 1), ShipStatus.SUNKEN);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void testSinkShip_SetsStatusToSunken() {
        Ship ship = new Ship(new Position(1, 1), ShipStatus.AFLOAT);
        ship.sinkShip();
        assertEquals(ShipStatus.SUNKEN, ship.getShipStatus());
    }

    @Test
    void testGettersAndSetters() {
        Ship ship = new Ship();
        Position position = new Position(4, 4);
        ship.setPosition(position);
        ship.setShipStatus(ShipStatus.AFLOAT);
        assertEquals(position, ship.getPosition());
        assertEquals(ShipStatus.AFLOAT, ship.getShipStatus());
    }
}
