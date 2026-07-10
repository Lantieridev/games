package ar.edu.utn.frc.tup.piii;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PokerRoundTest {

    private PokerRound pokerRound;
    private List<RoundPlayer> players;
    private Deck mockDeck;

    @BeforeEach
    public void setup() {
        LetterByLetterPrinter.DISABLE_COLORS = true;
        players = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            Player player = new Player("Player" + i, 1000, i == 0);
            RoundPlayer roundPlayer = new BotRoundPlayer(player, false);
            if (i == 0) {
                roundPlayer.setDealer(true);
            }
            players.add(roundPlayer);
        }

        pokerRound = new PokerRound(players);

        mockDeck = Mockito.mock(Deck.class);
        pokerRound.setDeck(mockDeck);
    }

    @Test
    public void testPlayFullRound_PlaysAllStagesWhenNobodyFolds() {
        PokerRound spyRound = Mockito.spy(pokerRound);
        doNothing().when(spyRound).startRound();
        doNothing().when(spyRound).preFlop();
        doNothing().when(spyRound).flop();
        doNothing().when(spyRound).turn();
        doNothing().when(spyRound).river();

        spyRound.playFullRound();

        verify(spyRound, times(1)).preFlop();
        verify(spyRound, times(1)).flop();
        verify(spyRound, times(1)).turn();
        verify(spyRound, times(1)).river();
    }

    @Test
    public void testPlayFullRound_StopsAfterPreFlopWhenOnlyOnePlayerRemains() {
        PokerRound spyRound = Mockito.spy(pokerRound);
        doNothing().when(spyRound).startRound();
        doAnswer(invocation -> {
            players.get(1).setFolded(true);
            players.get(2).setFolded(true);
            players.get(3).setFolded(true);
            return null;
        }).when(spyRound).preFlop();
        doNothing().when(spyRound).flop();
        doNothing().when(spyRound).turn();
        doNothing().when(spyRound).river();

        spyRound.playFullRound();

        verify(spyRound, times(1)).preFlop();
        verify(spyRound, never()).flop();
        verify(spyRound, never()).turn();
        verify(spyRound, never()).river();
    }

    @Test
    public void testPlayFullRound_StopsAfterFlopWhenOnlyOnePlayerRemains() {
        PokerRound spyRound = Mockito.spy(pokerRound);
        doNothing().when(spyRound).startRound();
        doNothing().when(spyRound).preFlop();
        doAnswer(invocation -> {
            players.get(1).setFolded(true);
            players.get(2).setFolded(true);
            players.get(3).setFolded(true);
            return null;
        }).when(spyRound).flop();
        doNothing().when(spyRound).turn();
        doNothing().when(spyRound).river();

        spyRound.playFullRound();

        verify(spyRound, times(1)).flop();
        verify(spyRound, never()).turn();
        verify(spyRound, never()).river();
    }

    @Test
    public void testIsRoundDecided_MultipleActivePlayers_ReturnsFalse() throws Exception {
        Method isRoundDecided = PokerRound.class.getDeclaredMethod("isRoundDecided");
        isRoundDecided.setAccessible(true);
        assertFalse((boolean) isRoundDecided.invoke(pokerRound));
    }

    @Test
    public void testIsRoundDecided_OnlyOneActivePlayer_ReturnsTrue() throws Exception {
        players.get(1).setFolded(true);
        players.get(2).setFolded(true);
        players.get(3).setFolded(true);
        Method isRoundDecided = PokerRound.class.getDeclaredMethod("isRoundDecided");
        isRoundDecided.setAccessible(true);
        assertTrue((boolean) isRoundDecided.invoke(pokerRound));
    }

    @Test
    public void testDealCards_DistributesCorrectly() {
        Card card1 = new Card(CardSuit.CLUBS, CardSymbol.ACE);
        Card card2 = new Card(CardSuit.DIAMONDS, CardSymbol.KING);
        Card card3 = new Card(CardSuit.HEARTS, CardSymbol.QUEEN);
        Card card4 = new Card(CardSuit.SPADES, CardSymbol.JACK);
        Card card5 = new Card(CardSuit.CLUBS, CardSymbol.TEN);
        Card card6 = new Card(CardSuit.DIAMONDS, CardSymbol.NINE);
        Card card7 = new Card(CardSuit.HEARTS, CardSymbol.EIGHT);
        Card card8 = new Card(CardSuit.SPADES, CardSymbol.SEVEN);

        when(mockDeck.takeCard())
                .thenReturn(card1, card2, card3, card4, card5, card6, card7, card8);

        pokerRound.dealCards();

        for (RoundPlayer player : players) {
            assertEquals(2, player.getHandCards().size());
        }

        verify(mockDeck, times(8)).takeCard();

        assertEquals(2, players.get(0).getHandCards().size());
        assertEquals(2, players.get(1).getHandCards().size());
        assertEquals(2, players.get(2).getHandCards().size());
        assertEquals(2, players.get(3).getHandCards().size());

        Set<Card> cardSet = new HashSet<>();
        cardSet.addAll(players.get(0).getHandCards());
        cardSet.addAll(players.get(1).getHandCards());
        cardSet.addAll(players.get(2).getHandCards());
        cardSet.addAll(players.get(3).getHandCards());

        assertEquals(8, cardSet.size());
    }

    @Test
    public void testSetChipsToWinner_OnlyOneNotFolded() throws Exception {
        pokerRound.setBucketChips(500);
        players.get(1).setFolded(true);
        players.get(2).setFolded(true);
        players.get(3).setFolded(true);

        Method calculateRoundWinner = PokerRound.class.getDeclaredMethod("calculateRoundWinner");
        calculateRoundWinner.setAccessible(true);
        calculateRoundWinner.invoke(pokerRound);

        assertEquals(1500, players.get(0).getPlayer().getChips());
        assertEquals(0, pokerRound.getBucketChips());
    }

    @Test
    public void testIsStageFinish_AllEqualBets() throws Exception {
        Method isStageFinish = PokerRound.class.getDeclaredMethod("isStageFinish");
        isStageFinish.setAccessible(true);
        for (RoundPlayer player : players) {
            player.setBetChips(20);
        }
        assertTrue((boolean) isStageFinish.invoke(pokerRound));
    }

    @Test
    public void testIsStageFinish_PlayerNotMatchingHighestBet() throws Exception {
        Method isStageFinish = PokerRound.class.getDeclaredMethod("isStageFinish");
        isStageFinish.setAccessible(true);
        for (RoundPlayer player : players) {
            player.setBetChips(20);
        }
        players.get(1).setBetChips(10);
        assertFalse((boolean) isStageFinish.invoke(pokerRound));
    }

    @Test
    public void testIsStageFinish_PlayerFoldedWithLowerBet() throws Exception {
        Method isStageFinish = PokerRound.class.getDeclaredMethod("isStageFinish");
        isStageFinish.setAccessible(true);
        for (RoundPlayer player : players) {
            player.setBetChips(20);
        }
        players.get(1).setBetChips(10);
        players.get(1).setFolded(true);
        assertTrue((boolean) isStageFinish.invoke(pokerRound));
    }

    @Test
    public void testIsStageFinish_PlayerAllInWithLowerBet() throws Exception {
        Method isStageFinish = PokerRound.class.getDeclaredMethod("isStageFinish");
        isStageFinish.setAccessible(true);
        for (RoundPlayer player : players) {
            player.setBetChips(20);
        }
        players.get(1).setBetChips(10);
        players.get(1).setAllIn(true);
        assertTrue((boolean) isStageFinish.invoke(pokerRound));
    }

    @Test
    public void testIsStageFinish_OnePlayerZeroBet() throws Exception {
        Method isStageFinish = PokerRound.class.getDeclaredMethod("isStageFinish");
        isStageFinish.setAccessible(true);
        for (RoundPlayer player : players) {
            player.setBetChips(20);
            player.setAllIn(false);
            player.setFolded(false);
        }
        players.get(1).setBetChips(0);
        assertFalse((boolean) isStageFinish.invoke(pokerRound));
    }

    @Test
    public void testIsStageFinish_AllBetsZero() throws Exception {
        Method isStageFinish = PokerRound.class.getDeclaredMethod("isStageFinish");
        isStageFinish.setAccessible(true);
        for (RoundPlayer player : players) {
            player.setBetChips(0);
            player.setAllIn(false);
            player.setFolded(false);
        }
        assertTrue((boolean) isStageFinish.invoke(pokerRound));
    }

    @Test
    public void testCollectChipsToBucket() throws Exception {
        pokerRound.setBucketChips(100);
        players.get(0).setBetChips(50);
        players.get(1).setBetChips(30);
        players.get(2).setBetChips(20);
        players.get(3).setBetChips(0);

        Method collectChipsToBucket = PokerRound.class.getDeclaredMethod("collectChipsToBucket");
        collectChipsToBucket.setAccessible(true);
        collectChipsToBucket.invoke(pokerRound);

        assertEquals(200, pokerRound.getBucketChips());
        for (RoundPlayer player : players) {
            assertEquals(0, player.getBetChips());
        }
    }

    @Test
    public void testResetRound() {
        pokerRound.setBucketChips(200);

        for (int i = 0; i < players.size(); i++) {
            RoundPlayer player = players.get(i);
            List<Card> handCards = new ArrayList<>();
            handCards.add(new Card(CardSuit.CLUBS, CardSymbol.SEVEN));
            handCards.add(new Card(CardSuit.DIAMONDS, CardSymbol.SEVEN));
            player.setHandCards(handCards);
            player.setBetChips(20 * (i + 1));
            if (i == 1) player.setAllIn(true);
            if (i == 2) player.setFolded(true);
        }

        pokerRound.setTableCards(List.of(
                new Card(CardSuit.HEARTS, CardSymbol.ACE),
                new Card(CardSuit.SPADES, CardSymbol.KING),
                new Card(CardSuit.CLUBS, CardSymbol.QUEEN)
        ));
        pokerRound.setRoundStage(RoundStage.FLOP);
        pokerRound.setTurnPlayerIndex(2);

        int currentDealerIndex = -1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getDealer()) {
                currentDealerIndex = i;
                break;
            }
        }

        pokerRound.resetRound();

        assertNotNull(pokerRound.getDeck());
        assertNotSame(mockDeck, pokerRound.getDeck());
        assertTrue(pokerRound.getTableCards().isEmpty());
        assertEquals(0, pokerRound.getBucketChips());
        assertNull(pokerRound.getTurnPlayerIndex());
        assertNull(pokerRound.getRoundStage());

        for (RoundPlayer player : players) {
            assertFalse(player.getAllIn());
            assertFalse(player.getFolded());
            assertEquals(0, player.getBetChips());
            assertTrue(player.getHandCards().isEmpty());
        }

        int newDealerIndex = -1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getDealer()) {
                newDealerIndex = i;
                break;
            }
        }

        assertEquals((currentDealerIndex + 1) % players.size(), newDealerIndex);
    }

    @Test
    public void testMoveDealer_AdvanceOne() throws Exception {
        players.get(0).setDealer(true);

        Method moveDealer = PokerRound.class.getDeclaredMethod("moveDealer");
        moveDealer.setAccessible(true);
        moveDealer.invoke(pokerRound);

        assertFalse(players.get(0).getDealer());
        assertTrue(players.get(1).getDealer());
    }

    @Test
    public void testMoveDealer_WrapAround() throws Exception {
        for (int i = 0; i < players.size(); i++) {
            players.get(i).setDealer(i == players.size() - 1);
        }

        Method moveDealer = PokerRound.class.getDeclaredMethod("moveDealer");
        moveDealer.setAccessible(true);
        moveDealer.invoke(pokerRound);

        assertTrue(players.get(0).getDealer());
    }


    @Test
    void testSetNextTurnPlayerIndex_TurnIndexNull() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        pokerRound.setTurnPlayerIndex(null);
        players.get(0).setDealer(true);

        Method setNextTurnPlayerIndex = PokerRound.class.getDeclaredMethod("setNextTurnPlayerIndex");
        setNextTurnPlayerIndex.setAccessible(true);
        setNextTurnPlayerIndex.invoke(pokerRound);

        assertEquals(1, pokerRound.getTurnPlayerIndex());
    }

    @Test
    void testSetNextTurnPlayerIndex_TurnIndexNull_LastIndex() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        players.get(0).setDealer(false);
        pokerRound.setTurnPlayerIndex(null);
        players.get(players.size() - 1).setDealer(true);

        Method setNextTurnPlayerIndex = PokerRound.class.getDeclaredMethod("setNextTurnPlayerIndex");
        setNextTurnPlayerIndex.setAccessible(true);
        setNextTurnPlayerIndex.invoke(pokerRound);

        assertEquals(0, pokerRound.getTurnPlayerIndex());
    }

    @Test
    void testSetNextTurnPlayerIndex_TurnIndexNotNull() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        pokerRound.setTurnPlayerIndex(1);

        Method setNextTurnPlayerIndex = PokerRound.class.getDeclaredMethod("setNextTurnPlayerIndex");
        setNextTurnPlayerIndex.setAccessible(true);
        setNextTurnPlayerIndex.invoke(pokerRound);

        assertEquals(2, pokerRound.getTurnPlayerIndex());
    }

    @Test
    void testSetNextTurnPlayerIndex_TurnIndexNotNull_LastIndex() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        pokerRound.setTurnPlayerIndex(players.size() - 1);

        Method setNextTurnPlayerIndex = PokerRound.class.getDeclaredMethod("setNextTurnPlayerIndex");
        setNextTurnPlayerIndex.setAccessible(true);
        setNextTurnPlayerIndex.invoke(pokerRound);

        assertEquals(0, pokerRound.getTurnPlayerIndex());
    }

    @Test
    void testSetNextTurnPlayerIndex_TurnIndexNotNull_AnyFolded() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        pokerRound.setTurnPlayerIndex(0);
        players.get(1).setFolded(true);

        Method setNextTurnPlayerIndex = PokerRound.class.getDeclaredMethod("setNextTurnPlayerIndex");
        setNextTurnPlayerIndex.setAccessible(true);
        setNextTurnPlayerIndex.invoke(pokerRound);

        assertEquals(2, pokerRound.getTurnPlayerIndex());
    }

    @Test
    void testSetNextTurnPlayerIndex_TurnIndexNotNull_AnyAllin() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        pokerRound.setTurnPlayerIndex(0);
        players.get(1).setAllIn(true);

        Method setNextTurnPlayerIndex = PokerRound.class.getDeclaredMethod("setNextTurnPlayerIndex");
        setNextTurnPlayerIndex.setAccessible(true);
        setNextTurnPlayerIndex.invoke(pokerRound);

        assertEquals(2, pokerRound.getTurnPlayerIndex());
    }

    @Test
    void testSetNextTurnPlayerIndex_TurnIndexNotNull_AnyAllin_AnyFolded() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        pokerRound.setTurnPlayerIndex(players.size() - 3);
        players.get(players.size() - 2).setFolded(true);
        players.get(players.size() - 1).setAllIn(true);

        Method setNextTurnPlayerIndex = PokerRound.class.getDeclaredMethod("setNextTurnPlayerIndex");
        setNextTurnPlayerIndex.setAccessible(true);
        setNextTurnPlayerIndex.invoke(pokerRound);

        assertEquals(0, pokerRound.getTurnPlayerIndex());
    }
}
