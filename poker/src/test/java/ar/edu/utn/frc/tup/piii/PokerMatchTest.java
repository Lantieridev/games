package ar.edu.utn.frc.tup.piii;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PokerMatchTest {

    @BeforeEach
    public void setup() {
        LetterByLetterPrinter.DISABLE_COLORS = true;
    }

    @Test
    public void testIsFinish_WithOnlyOnePlayerWithChips_ReturnsTrue() {
        
        List<Player> players = new ArrayList<>();
        Player player1 = new Player();
        player1.setChips(100);
        Player player2 = new Player();
        player2.setChips(0);
        Player player3 = new Player();
        player3.setChips(0);

        players.add(player1);
        players.add(player2);
        players.add(player3);

        PokerMatch pokerMatch = new PokerMatch(players);

        
        boolean result = pokerMatch.isFinish();

        
        assertTrue(result);
    }

    @Test
    public void testIsFinish_WithMultiplePlayersWithChips_ReturnsFalse() {
        
        List<Player> players = new ArrayList<>();
        Player player1 = new Player();
        player1.setChips(100);
        Player player2 = new Player();
        player2.setChips(50);
        Player player3 = new Player();
        player3.setChips(0);

        players.add(player1);
        players.add(player2);
        players.add(player3);

        PokerMatch pokerMatch = new PokerMatch(players);

        
        boolean result = pokerMatch.isFinish();

        
        assertFalse(result);
    }

    @Test
    public void testIsFinish_WithAllPlayersHavingChips_ReturnsFalse() {
        
        List<Player> players = new ArrayList<>();
        Player player1 = new Player();
        player1.setChips(100);
        Player player2 = new Player();
        player2.setChips(150);
        Player player3 = new Player();
        player3.setChips(200);

        players.add(player1);
        players.add(player2);
        players.add(player3);

        PokerMatch pokerMatch = new PokerMatch(players);

        
        boolean result = pokerMatch.isFinish();

        
        assertFalse(result);
    }

    @Test
    public void testIsFinish_WithNoPlayersHavingChips_ReturnsTrue() {
        List<Player> players = new ArrayList<>();
        Player player1 = new Player();
        player1.setChips(0);
        Player player2 = new Player();
        player2.setChips(0);
        Player player3 = new Player();
        player3.setChips(0);

        players.add(player1);
        players.add(player2);
        players.add(player3);

        PokerMatch pokerMatch = new PokerMatch(players);

        boolean result = pokerMatch.isFinish();

        assertTrue(result);
    }

    @Test
    public void testIsFinish_WithSinglePlayerWithChips_ReturnsTrue() {
        
        List<Player> players = new ArrayList<>();
        Player player1 = new Player();
        player1.setChips(100);

        players.add(player1);

        PokerMatch pokerMatch = new PokerMatch(players);

        boolean result = pokerMatch.isFinish();

        assertTrue(result);
    }

    @Test
    public void testIsFinish_WithSinglePlayerWithoutChips_ReturnsTrue() {
        
        List<Player> players = new ArrayList<>();
        Player player1 = new Player();
        player1.setChips(0);

        players.add(player1);

        PokerMatch pokerMatch = new PokerMatch(players);

        
        boolean result = pokerMatch.isFinish();

        
        assertTrue(result);
    }

    @Test
    public void testIsFinish_WithNegativeChipValues_CorrectHandling() {
        
        List<Player> players = new ArrayList<>();
        Player player1 = new Player();
        player1.setChips(100);
        Player player2 = new Player();
        player2.setChips(-50);

        players.add(player1);
        players.add(player2);

        PokerMatch pokerMatch = new PokerMatch(players);

        boolean result = pokerMatch.isFinish();

        
        assertTrue(result);
    }

    @Test
    public void testIsFinish_AfterRemovingPlayer_CorrectResult() {
        
        List<Player> players = new ArrayList<>();
        Player player1 = new Player();
        player1.setChips(100);
        Player player2 = new Player();
        player2.setChips(150);

        players.add(player1);
        players.add(player2);

        PokerMatch pokerMatch = new PokerMatch(players);

        
        pokerMatch.removePlayer(player2);
        boolean result = pokerMatch.isFinish();

        
        assertTrue(result);
    }
}
