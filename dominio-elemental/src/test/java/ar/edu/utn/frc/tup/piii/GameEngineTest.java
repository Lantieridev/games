package ar.edu.utn.frc.tup.piii;

import ar.edu.utn.frc.tup.piii.model.MachinePlayer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

public class GameEngineTest {

    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;

    @BeforeAll
    public static void disableThinkingDelay() {
        MachinePlayer.setThinkingDelayEnabled(false);
    }

    @AfterEach
    public void restoreStreams() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    private ByteArrayOutputStream runGame(String script) {
        System.setIn(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));

        GameEngine engine = new GameEngine();
        engine.setSeed(100L);
        engine.setPacingEnabled(false);
        engine.init();
        engine.play();
        return captured;
    }

    /**
     * A human who always passes their turn immediately, played to a full,
     * genuine game-over: real deck (seeded for determinism), real AI decisions,
     * real discard rule enforcement, real winner determination. This is the
     * actual entry point App.main() drives — no gap left undocumented.
     */
    private String buildPassEveryTurnScript(String playerName) {
        StringBuilder script = new StringBuilder(playerName).append('\n');
        // 36 cards - 10 dealt in init() = 26 left; the human draws first each
        // round, so 13 full human/machine rounds occur before the deck empties.
        for (int turn = 1; turn <= 13; turn++) {
            script.append("0\n"); // terminate turn immediately after drawing
            if (turn >= 3) {
                script.append("0\n"); // hand exceeds 7 from turn 3 onward — discard index 0
            }
        }
        return script.toString();
    }

    @Test
    public void testInitDealsFiveCardsEachAndReadsThePlayerName() {
        System.setIn(new ByteArrayInputStream("Martin\n".getBytes(StandardCharsets.UTF_8)));
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));

        GameEngine engine = new GameEngine();
        engine.setSeed(1L);
        engine.init();

        assertEquals("Martin", engine.getHuman().getName());
        assertEquals(5, engine.getHuman().getHand().size());
        assertEquals(5, engine.getMachine().getHand().size());
        assertEquals(26, engine.getDeck().size());
        assertTrue(engine.getTable().isEmpty());
    }

    @Test
    public void testFullGamePlaysToCompletionAndDeclaresAWinner() {
        ByteArrayOutputStream out = runGame(buildPassEveryTurnScript("Tester"));
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("FIN DEL JUEGO"));
    }

    @Test
    public void testHumanNeverActingMeansTheAIAccumulatesAllTheScore() {
        System.setIn(new ByteArrayInputStream(buildPassEveryTurnScript("Tester").getBytes(StandardCharsets.UTF_8)));
        System.setOut(new PrintStream(new ByteArrayOutputStream(), true, StandardCharsets.UTF_8));

        GameEngine engine = new GameEngine();
        engine.setSeed(100L);
        engine.setPacingEnabled(false);
        engine.init();
        engine.play();

        assertEquals(0, engine.getHuman().getScore());
        assertTrue(engine.getDeck().isEmpty());
        // The AI plays autonomously the whole game; with a real 26-card draw
        // across 13 turns it always finds at least one combination.
        assertTrue(engine.getMachine().getScore() > 0);
    }

    @Test
    public void testEndGameDeclaresDefeatWhenTheMachineOutscoresTheHuman() {
        ByteArrayOutputStream out = runGame(buildPassEveryTurnScript("Tester"));
        String text = out.toString(StandardCharsets.UTF_8);
        assertTrue(text.contains("DERROTA ELEMENTAL"));
    }

    @Test
    public void testInvalidMenuInputDuringARealGameDoesNotCrashTheEngine() {
        StringBuilder script = new StringBuilder("Tester\n");
        script.append("nonsense\n0\n"); // first turn: garbage then a valid pass
        for (int turn = 2; turn <= 13; turn++) {
            script.append("0\n");
            if (turn >= 3) script.append("0\n");
        }

        ByteArrayOutputStream out = assertDoesNotThrow(() -> runGame(script.toString()));
        assertTrue(out.toString(StandardCharsets.UTF_8).contains("Opcion invalida"));
    }
}
