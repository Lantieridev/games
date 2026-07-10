package ar.edu.utn.frc.tup.lciii.round;

import ar.edu.utn.frc.tup.lciii.Card;
import ar.edu.utn.frc.tup.lciii.CardSuit;
import ar.edu.utn.frc.tup.lciii.LetterByLetterPrinter;
import ar.edu.utn.frc.tup.lciii.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

class HumanRoundPlayerTest {

    private final PrintStream systemOut = System.out;

    private ByteArrayOutputStream testOut;

    @BeforeEach
    public void setUpOutput() {
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
        LetterByLetterPrinter.DISABLE_COLORS = true;
    }

    @AfterEach
    public void restoreSystemInputOutput() {
        System.setOut(systemOut);
    }

    @Test
    void playTurnTest() {
        HumanRoundPlayer roundPlayer = Mockito.spy(HumanRoundPlayer.class);
        Scanner scanner = Mockito.mock(Scanner.class);
        User user = Mockito.mock(User.class);
        roundPlayer.setScanner(scanner);
        roundPlayer.setPlayer(user);
        List<Card> handCards = new ArrayList<>();
        handCards.add(new Card(CardSuit.ESPADAS, 12, 10));
        handCards.add(new Card(CardSuit.OROS, 3, 3));
        handCards.add(new Card(CardSuit.BASTOS, 3, 3));
        roundPlayer.setHandCards(handCards);
        roundPlayer.setPersonalDeck(new ArrayList<>());

        List<Card> tableCards = new ArrayList<>();
        tableCards.add(new Card(CardSuit.ESPADAS, 10, 8));
        tableCards.add(new Card(CardSuit.COPAS, 4, 4));
        tableCards.add(new Card(CardSuit.ESPADAS, 4, 4));

        when(scanner.nextInt()).thenReturn(1);
        when(user.getName()).thenReturn("PLAYER");

        String expectedOutput = "=".repeat(44) + System.lineSeparator() +
                "➤ PLAYER TURN" + System.lineSeparator() +
                System.lineSeparator() +
                "┌───────── TABLE CARDS ──────────" + System.lineSeparator() +
                "│ 0 - 10 de ESPADAS" + System.lineSeparator() +
                "│ 1 - 4 de COPAS" + System.lineSeparator() +
                "│ 2 - 4 de ESPADAS" + System.lineSeparator() +
                "└────────────────────────────────" + System.lineSeparator() +
                "Your hand:" + System.lineSeparator() +
                "  0 - 12 de ESPADAS" + System.lineSeparator() +
                "  1 - 3 de OROS" + System.lineSeparator() +
                "  2 - 3 de BASTOS" + System.lineSeparator() +
                "Please, select a card to play: " + System.lineSeparator() +
                "Multiple combinations of 15 possible! Choose one:" + System.lineSeparator() +
                "  [0] 10 de ESPADAS 4 de COPAS 3 de OROS" + System.lineSeparator() +
                "  [1] 10 de ESPADAS 4 de ESPADAS 3 de OROS" + System.lineSeparator() +
                "✨ PLAYER takes cards! They were added to the personal deck." + System.lineSeparator() +
                "=".repeat(44) + System.lineSeparator() + System.lineSeparator();
        Optional<Method> optionalMethod = ReflectionUtils.findMethod(HumanRoundPlayer.class, "playTurn", List.class);
        if(optionalMethod.isPresent()) {
            ReflectionUtils.invokeMethod(optionalMethod.get(), roundPlayer, tableCards);
            String actual = getOutput().trim().replace("\r\n", "\n");
            String expected = expectedOutput.trim().replace("\r\n", "\n");
            assertEquals(expected, actual);
            assertEquals(3, roundPlayer.getPersonalDeck().size());
            assertEquals(2, roundPlayer.getHandCards().size());
            assertEquals(1, tableCards.size());
        } else {
            fail("Method playTurn not found");
        }
    }

    @Test
    void getSubconjuntoToPlayTest() {
        HumanRoundPlayer roundPlayer = Mockito.spy(HumanRoundPlayer.class);
        Scanner scanner = Mockito.mock(Scanner.class);
        roundPlayer.setScanner(scanner);
        List<Card> conjunto1 = List.of(
                new Card(CardSuit.ESPADAS, 10, 8),
                new Card(CardSuit.OROS, 3, 3),
                new Card(CardSuit.COPAS, 4, 4));
        List<Card> conjunto2 = List.of(
                new Card(CardSuit.ESPADAS, 10, 8),
                new Card(CardSuit.BASTOS, 3, 3),
                new Card(CardSuit.COPAS, 4, 4)
        );
        List<List<Card>> subconjuntos = new ArrayList<>();
        subconjuntos.add(conjunto1);
        subconjuntos.add(conjunto2);
        when(scanner.nextInt()).thenReturn(0);
        String expectedOutput = System.lineSeparator() + "Multiple combinations of 15 possible! Choose one:" + System.lineSeparator() +
                "  [0] 10 de ESPADAS 3 de OROS 4 de COPAS" + System.lineSeparator() +
                "  [1] 10 de ESPADAS 3 de BASTOS 4 de COPAS" + System.lineSeparator();
        Optional<Method> optionalMethod = ReflectionUtils.findMethod(HumanRoundPlayer.class, "getSubconjuntoToPlay", List.class);
        Integer result = null;
        if(optionalMethod.isPresent()) {
            result = (Integer) ReflectionUtils.invokeMethod(optionalMethod.get(), roundPlayer, subconjuntos);
            assertEquals(expectedOutput.trim().replace("\r\n", "\n"), getOutput().trim().replace("\r\n", "\n"));
            assertEquals(0, result);
        } else {
            fail("Method getSubconjuntoToPlay not found");
        }
    }

    @Test
    void getCardToPlayTest() {
        HumanRoundPlayer roundPlayer = Mockito.spy(HumanRoundPlayer.class);
        Scanner scanner = Mockito.mock(Scanner.class);
        roundPlayer.setScanner(scanner);
        List<Card> handCards = new ArrayList<>();
        handCards.add(new Card(CardSuit.ESPADAS, 10, 8));
        handCards.add(new Card(CardSuit.OROS, 3, 3));
        handCards.add(new Card(CardSuit.COPAS, 4, 4));
        roundPlayer.setHandCards(handCards);
        when(scanner.nextInt()).thenReturn(0);
        String expectedOutput = "Your hand:" + System.lineSeparator() +
                "  0 - 10 de ESPADAS" + System.lineSeparator() +
                "  1 - 3 de OROS" + System.lineSeparator() +
                "  2 - 4 de COPAS" + System.lineSeparator() +
                "Please, select a card to play: ";
        Optional<Method> optionalMethod = ReflectionUtils.findMethod(HumanRoundPlayer.class, "getCardToPlay");
        Integer result = null;
        if(optionalMethod.isPresent()) {
            result = (Integer) ReflectionUtils.invokeMethod(optionalMethod.get(), roundPlayer);
            assertEquals(expectedOutput.trim().replace("\r\n", "\n"), getOutput().trim().replace("\r\n", "\n"));
            assertEquals(0, result);
        } else {
            fail("Method getCardToPlay not found");
        }
    }

    private String getOutput() {
        return testOut.toString();
    }

}