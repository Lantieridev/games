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

public class GameSimulatorTest {

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

    @Test
    public void testMainRunsAnIaVsIaSimulationToCompletion() {
        // Every "PAUSA PARA CAPTURA" prompt just needs an Enter — 30 blank
        // lines comfortably cover the worst case (a pause after every round).
        String script = "\n".repeat(30);
        System.setIn(new ByteArrayInputStream(script.getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));

        assertDoesNotThrow(() -> GameSimulator.main(new String[0]));

        assertTrue(captured.toString(StandardCharsets.UTF_8).contains("RESULTADO DE LA SIMULACION"));
    }
}
