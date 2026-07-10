package ar.edu.utn.frc.tup.piii;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MesaPokerPrinterTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    public void setup() {
        LetterByLetterPrinter.DISABLE_COLORS = true;
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
    }

    private RoundPlayer buildPlayer(String name, boolean isBot, int chips, int betChips, boolean dealer) {
        Player player = new Player(name, chips, isBot);
        RoundPlayer roundPlayer = new BotRoundPlayer(player, dealer);
        roundPlayer.setBetChips(betChips);
        return roundPlayer;
    }

    @Test
    public void testPrintMesaPoker_SixPlayers_DoesNotThrow() {
        List<RoundPlayer> players = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            players.add(buildPlayer("Player" + i, i != 0, 1000, 0, i == 0));
        }
        MesaPokerPrinter.printMesaPoker(players, List.of(), 100);

        String output = outContent.toString();
        assertTrue(output.contains("COMMUNITY CARDS"));
        assertTrue(output.contains("TOTAL POT"));
    }

    @Test
    public void testPrintMesaPoker_FewerThanSixPlayers_DoesNotThrow() {
        List<RoundPlayer> players = new ArrayList<>();
        players.add(buildPlayer("You", false, 1000, 20, true));
        players.add(buildPlayer("Bot1", true, 500, 20, false));

        // Should not throw IndexOutOfBoundsException now that the table
        // no longer assumes exactly 6 seats (busted players get removed).
        MesaPokerPrinter.printMesaPoker(players, List.of(), 40);

        assertTrue(outContent.toString().contains("You are matched"));
    }

    @Test
    public void testPrintMesaPoker_HumanOwesChips_ShowsToCall() {
        List<RoundPlayer> players = new ArrayList<>();
        players.add(buildPlayer("You", false, 1000, 10, true));
        players.add(buildPlayer("Bot1", true, 500, 30, false));

        MesaPokerPrinter.printMesaPoker(players, List.of(), 40);

        String output = outContent.toString();
        assertTrue(output.contains("To call: "));
        assertTrue(output.contains("20 chips"));
    }
}
