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

public class AppTest {

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
    public void testMainWiresUpAndRunsAFullGame() {
        StringBuilder script = new StringBuilder("Tester\n");
        for (int turn = 1; turn <= 13; turn++) {
            script.append("0\n");
            if (turn >= 3) script.append("0\n");
        }
        System.setIn(new ByteArrayInputStream(script.toString().getBytes(StandardCharsets.UTF_8)));
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured, true, StandardCharsets.UTF_8));

        assertDoesNotThrow(() -> App.main(new String[0]));

        assertTrue(captured.toString(StandardCharsets.UTF_8).contains("FIN DEL JUEGO"));
    }
}
