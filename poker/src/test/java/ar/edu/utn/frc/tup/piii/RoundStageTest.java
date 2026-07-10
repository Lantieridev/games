package ar.edu.utn.frc.tup.piii;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RoundStageTest {

    @Test
    public void testGetNextStage_PreFlopToFlop() {
        assertEquals(RoundStage.FLOP, RoundStage.getNextStage(RoundStage.PRE_FLOP));
    }

    @Test
    public void testGetNextStage_FlopToTurn() {
        assertEquals(RoundStage.TURN, RoundStage.getNextStage(RoundStage.FLOP));
    }

    @Test
    public void testGetNextStage_TurnToRiver() {
        assertEquals(RoundStage.RIVER, RoundStage.getNextStage(RoundStage.TURN));
    }

    @Test
    public void testGetNextStage_RiverToShowdown() {
        assertEquals(RoundStage.SHOWDOWN, RoundStage.getNextStage(RoundStage.RIVER));
    }

    @Test
    public void testGetNextStage_Showdown_ReturnsNull() {
        assertNull(RoundStage.getNextStage(RoundStage.SHOWDOWN));
    }
}
