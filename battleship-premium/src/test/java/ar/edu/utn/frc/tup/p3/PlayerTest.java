package ar.edu.utn.frc.tup.p3;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PlayerTest {

    @Test
    void testNoArgConstructor_StartsWithNullFields() {
        Player player = new Player();
        assertNull(player.getPlayerName());
        assertNull(player.getScore());
        assertNull(player.getGamesWon());
    }

    @Test
    void testParameterizedConstructor_SetsAllFields() {
        Player player = new Player("Human", 5, 2);
        assertEquals("Human", player.getPlayerName());
        assertEquals(5, player.getScore());
        assertEquals(2, player.getGamesWon());
    }

    @Test
    void testSettersAndGetters() {
        Player player = new Player();
        player.setPlayerName("APP");
        player.setScore(10);
        player.setGamesWon(3);
        assertEquals("APP", player.getPlayerName());
        assertEquals(10, player.getScore());
        assertEquals(3, player.getGamesWon());
    }
}
