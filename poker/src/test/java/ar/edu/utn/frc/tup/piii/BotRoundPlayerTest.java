package ar.edu.utn.frc.tup.piii;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class BotRoundPlayerTest {

    private BotRoundPlayer bot;
    private List<RoundPlayer> players;

    @BeforeEach
    public void setup() {
        LetterByLetterPrinter.DISABLE_COLORS = true;
        Player player = new Player("Bot1", 1000, true);
        bot = new BotRoundPlayer(player, false);
        players = new ArrayList<>();
        players.add(bot);
    }

    @Test
    public void testPlayPreFlop_NoCurrentBet_BetsOpeningAmount() {
        bot.playPreFlop(List.of(), RoundStage.PRE_FLOP, players, 0);
        assertEquals(10, bot.getBetChips());
    }

    @Test
    public void testPlayFlop_MatchesExistingBet() {
        Player rival = new Player("Bot2", 1000, true);
        RoundPlayer rivalPlayer = new BotRoundPlayer(rival, false);
        rivalPlayer.setBetChips(50);
        players.add(rivalPlayer);

        bot.playFlop(List.of(), RoundStage.FLOP, players, 50);

        assertEquals(50, bot.getBetChips());
        assertFalse(bot.getAllIn());
    }

    @Test
    public void testPlayTurn_CallAmountExceedsChips_GoesAllIn() {
        Player shortStack = new Player("Bot1", 30, true);
        BotRoundPlayer shortBot = new BotRoundPlayer(shortStack, false);
        List<RoundPlayer> localPlayers = new ArrayList<>();
        localPlayers.add(shortBot);

        Player rival = new Player("Bot2", 1000, true);
        RoundPlayer rivalPlayer = new BotRoundPlayer(rival, false);
        rivalPlayer.setBetChips(500);
        localPlayers.add(rivalPlayer);

        shortBot.playTurn(List.of(), RoundStage.TURN, localPlayers, 500);

        assertTrue(shortBot.getAllIn());
        assertEquals(30, shortBot.getBetChips());
        assertEquals(0, shortStack.getChips());
    }

    @Test
    public void testPlayRiver_DelegatesToDoCall() {
        bot.playRiver(List.of(), RoundStage.RIVER, players, 0);
        assertEquals(10, bot.getBetChips());
    }
}
