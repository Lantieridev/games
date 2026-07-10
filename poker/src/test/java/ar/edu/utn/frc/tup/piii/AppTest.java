package ar.edu.utn.frc.tup.piii;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest {

    private List<RoundPlayer> roundPlayers;

    @BeforeEach
    public void setup() {
        LetterByLetterPrinter.DISABLE_COLORS = true;
        roundPlayers = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            Player player = new Player("Player" + i, i == 1 ? 0 : 1000, false);
            roundPlayers.add(new BotRoundPlayer(player, i == 0));
        }
    }

    @Test
    public void testRemoveBustedPlayers_RemovesPlayersWithZeroChips() throws Exception {
        Method removeBustedPlayers = App.class.getDeclaredMethod("removeBustedPlayers", List.class);
        removeBustedPlayers.setAccessible(true);

        removeBustedPlayers.invoke(null, roundPlayers);

        assertEquals(3, roundPlayers.size());
        assertTrue(roundPlayers.stream().noneMatch(rp -> rp.getPlayer().getChips() <= 0));
    }

    @Test
    public void testRemoveBustedPlayers_NoOneBusted_LeavesTableUnchanged() throws Exception {
        for (RoundPlayer roundPlayer : roundPlayers) {
            roundPlayer.getPlayer().setChips(500);
        }
        Method removeBustedPlayers = App.class.getDeclaredMethod("removeBustedPlayers", List.class);
        removeBustedPlayers.setAccessible(true);

        removeBustedPlayers.invoke(null, roundPlayers);

        assertEquals(4, roundPlayers.size());
    }

    @Test
    public void testRemoveBustedPlayers_NegativeChips_AlsoRemoved() throws Exception {
        roundPlayers.get(2).getPlayer().setChips(-5);
        Method removeBustedPlayers = App.class.getDeclaredMethod("removeBustedPlayers", List.class);
        removeBustedPlayers.setAccessible(true);

        removeBustedPlayers.invoke(null, roundPlayers);

        assertEquals(2, roundPlayers.size());
    }
}
