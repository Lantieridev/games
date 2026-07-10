package com.utn.buscaminas;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MainTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    @BeforeEach
    public void setup() {
        System.setOut(new PrintStream(outContent, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    public void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    @Test
    public void testMain_WiresUpAndStartsConsoleUI() {
        System.setIn(new ByteArrayInputStream("TestPlayer\n1\nQ\nN\n".getBytes(StandardCharsets.UTF_8)));

        Main.main(new String[] {});

        assertTrue(outContent.toString().contains("Partida abandonada."));
    }

    @Test
    public void testConstructor_IsPrivate_ThisIsAnEntryPointClass() throws Exception {
        Constructor<Main> constructor = Main.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}
