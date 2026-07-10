package ar.edu.utn.frc.tup.piii;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class PokerRound {

    public static final Integer SMALL_BLIND = 10;
    public static final Integer BIG_BLIND = 20;

    private Deck deck;
    private List<Card> tableCards;
    private List<RoundPlayer> players;
    private Integer bucketChips;
    private Integer turnPlayerIndex;
    private RoundStage roundStage;

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public List<Card> getTableCards() {
        return tableCards;
    }

    public void setTableCards(List<Card> tableCards) {
        this.tableCards = tableCards;
    }

    public List<RoundPlayer> getPlayers() {
        return players;
    }

    public void setPlayers(List<RoundPlayer> players) {
        this.players = players;
    }

    public Integer getBucketChips() {
        return bucketChips;
    }

    public void setBucketChips(Integer bucketChips) {
        this.bucketChips = bucketChips;
    }

    public Integer getTurnPlayerIndex() {
        return turnPlayerIndex;
    }

    public void setTurnPlayerIndex(Integer turnPlayerIndex) {
        this.turnPlayerIndex = turnPlayerIndex;
    }

    public RoundStage getRoundStage() {
        return roundStage;
    }

    public void setRoundStage(RoundStage roundStage) {
        this.roundStage = roundStage;
    }

    public PokerRound(List<RoundPlayer> players) {
        this.deck = new Deck();
        this.players = players;
        this.tableCards = new ArrayList<>();
        this.bucketChips = 0;
        this.turnPlayerIndex = null;
        this.roundStage = null;
    }

    public void startRound() {
        setNextTurnPlayerIndex();
        smallBlind();
        bigBlind();
        dealCards();
        this.roundStage = RoundStage.PRE_FLOP;
    }

    /**
     * This method deals 2 cards to each player
     */
    public void dealCards() {
        LetterByLetterPrinter.println("\n" + LetterByLetterPrinter.WHITE + "┌" + "─".repeat(42) + "┐");
        LetterByLetterPrinter.println("│ " + LetterByLetterPrinter.CYAN + "DEALING CARDS" + " ".repeat(28) + LetterByLetterPrinter.WHITE + "│");
        LetterByLetterPrinter.println("└" + "─".repeat(42) + "┘" + LetterByLetterPrinter.RESET);
        for (RoundPlayer player : players) {
            player.getHandCards().add(deck.takeCard());
            player.getHandCards().add(deck.takeCard());
        }
    }

    public void preFlop() {
        this.playRoundStage("PRE-FLOP STAGE", 0);
    }

    public void flop() {
        this.playRoundStage("FLOP STAGE", 3);
    }

    public void turn() {
        this.playRoundStage("TURN STAGE", 1);
    }

    public void river() {
        this.playRoundStage("RIVER STAGE", 1);
        this.calculateRoundWinner();
    }

    /**
     * Runs the full round (blinds, deal, betting stages) and stops as soon as only
     * one player is left, instead of dealing out remaining community cards and
     * asking a lone winner to keep acting on a hand that is already decided.
     */
    public void playFullRound() {
        startRound();
        preFlop();
        if (isRoundDecided()) {
            calculateRoundWinner();
            return;
        }
        flop();
        if (isRoundDecided()) {
            calculateRoundWinner();
            return;
        }
        turn();
        if (isRoundDecided()) {
            calculateRoundWinner();
            return;
        }
        river();
    }

    private boolean isRoundDecided() {
        return players.stream().filter(player -> !player.getFolded()).count() <= 1;
    }

    private void playRoundStage(String stageName, int cardsQuantity) {
        String gold = LetterByLetterPrinter.YELLOW + LetterByLetterPrinter.BOLD;
        String white = LetterByLetterPrinter.WHITE + LetterByLetterPrinter.BOLD;
        
        LetterByLetterPrinter.println("\n" + white + ">>> " + gold + stageName + white + " <<<" + LetterByLetterPrinter.RESET);
        
        for (int i = 0; i < cardsQuantity; i++) {
            tableCards.add(deck.takeCard());
        }
        do {
            RoundPlayer player = players.get(turnPlayerIndex);
            if(!player.getPlayer().getBot()) {
                MesaPokerPrinter.printMesaPoker(players, tableCards, bucketChips);
            }
            player.playStage(tableCards, roundStage, players, bucketChips);
            setNextTurnPlayerIndex();
        } while(!isStageFinish());
        this.collectChipsToBucket();
        resetIndex();
        this.roundStage = RoundStage.getNextStage(roundStage);
    }

    private void resetIndex() {
        turnPlayerIndex = (players.stream()
                .filter(RoundPlayer::getDealer)
                .findFirst()
                .map(players::indexOf)
                .orElse(0) + 1) % players.size();
    }

    private void calculateRoundWinner() {
        LetterByLetterPrinter.println("\n" + LetterByLetterPrinter.CYAN + "Evaluating final hands..." + LetterByLetterPrinter.RESET);

        List<RoundPlayer> activePlayers = players.stream()
                .filter(player -> !player.getFolded())
                .collect(Collectors.toList());

        if (activePlayers.isEmpty()) {
            return;
        }

        if (activePlayers.size() == 1) {
            this.setChipsToWinner(activePlayers.get(0), List.of());
            return;
        }

        Map<RoundPlayer, Map.Entry<List<Card>, PokerHandRank>> playerBestHands = new HashMap<>();
        for (RoundPlayer player : activePlayers) {
            Map.Entry<List<Card>, PokerHandRank> bestHand = player.getBestHand(tableCards);
            playerBestHands.put(player, bestHand);
        }

        RoundPlayer winner = null;
        Map.Entry<List<Card>, PokerHandRank> bestHandSoFar = null;

        for (Map.Entry<RoundPlayer, Map.Entry<List<Card>, PokerHandRank>> entry : playerBestHands.entrySet()) {
            RoundPlayer currentPlayer = entry.getKey();
            Map.Entry<List<Card>, PokerHandRank> currentHand = entry.getValue();

            if (winner == null || HandEvaluator.isBetterHand(currentHand, bestHandSoFar)) {
                winner = currentPlayer;
                bestHandSoFar = currentHand;
            }
        }

        this.setChipsToWinner(winner, bestHandSoFar.getKey());
    }

    private void setChipsToWinner(RoundPlayer winnerPlayer, List<Card> bestHand) {
        String gold = LetterByLetterPrinter.YELLOW + LetterByLetterPrinter.BOLD;
        String green = LetterByLetterPrinter.GREEN + LetterByLetterPrinter.BOLD;
        String reset = LetterByLetterPrinter.RESET;

        LetterByLetterPrinter.println("\n" + green + "⭐ WINNER: " + winnerPlayer.getPlayer().getName() + " ⭐" + reset);
        if (!bestHand.isEmpty()) {
            String cards = bestHand.stream().map(Card::toString).collect(Collectors.joining(" "));
            LetterByLetterPrinter.println("Best Hand: " + LetterByLetterPrinter.CYAN + cards + reset);
        }

        LetterByLetterPrinter.println("Pot Claimed: " + gold + bucketChips + " chips" + reset + "\n");
        winnerPlayer.getPlayer().setChips(winnerPlayer.getPlayer().getChips() + bucketChips);
        this.bucketChips = 0;
    }

    /**
     * This method checks if all players have made their bets or fold to check if the stage is finished.
     *
     * @return true if all players have made their bets or folded, false otherwise
     */
    private boolean isStageFinish() {
        int maxBet = players.stream()
                .mapToInt(RoundPlayer::getBetChips)
                .max()
                .orElse(0);

        return players.stream()
                .filter(player -> !player.getFolded())
                .allMatch(player -> player.getAllIn() || player.getBetChips() == maxBet);
    }

    /**
     * This method collects the chips from all players and adds them to the bucket.
     */
    private void collectChipsToBucket() {
        LetterByLetterPrinter.println("Collecting chips to bucket...");
        for (RoundPlayer player : players) {
            bucketChips += player.getBetChips();
            player.setBetChips(0);
        }
        LetterByLetterPrinter.println("Total chips in bucket: " + bucketChips);
    }

    /**
     * This method sets the next player to play.
     */
    private void setNextTurnPlayerIndex() {
        if (turnPlayerIndex == null) {
            int dealerIndex = 0;
            for (int i = 0; i < players.size(); i++) {
                if (players.get(i).getDealer()) {
                    dealerIndex = i;
                    break;
                }
            }
            turnPlayerIndex = (dealerIndex + 1) % players.size();
        } else {
            int nextIndex = (turnPlayerIndex + 1) % players.size();
            int count = 0;
            while (count < players.size()) {
                RoundPlayer nextPlayer = players.get(nextIndex);
                if (!nextPlayer.getFolded() && !nextPlayer.getAllIn()) {
                    turnPlayerIndex = nextIndex;
                    return;
                }
                nextIndex = (nextIndex + 1) % players.size();
                count++;
            }
        }
    }

    private void smallBlind() {
        RoundPlayer player = players.get(turnPlayerIndex);
        player.bet(SMALL_BLIND);
        LetterByLetterPrinter.println(player.getPlayer().getName() + " posts small blind (" + SMALL_BLIND + ")");
        setNextTurnPlayerIndex();
    }

    private void bigBlind() {
        RoundPlayer player = players.get(turnPlayerIndex);
        player.bet(BIG_BLIND);
        LetterByLetterPrinter.println(player.getPlayer().getName() + " posts big blind (" + BIG_BLIND + ")");
        setNextTurnPlayerIndex();
    }

    /**
     * This method resets the round to start a new one.
     */
    public void resetRound() {
        this.deck = new Deck();
        this.tableCards = new ArrayList<>();
        this.bucketChips = 0;
        this.turnPlayerIndex = null;
        this.roundStage = null;
        for (RoundPlayer player : players) {
            player.setAllIn(false);
            player.setFolded(false);
            player.setBetChips(0);
            player.getHandCards().clear();
        }
        moveDealer();
    }

    /**
     * This method moves the dealer to the next player.
     */
    private void moveDealer() {
        int dealerIndex = -1;
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getDealer()) {
                dealerIndex = i;
                players.get(i).setDealer(false);
                break;
            }
        }
        if (dealerIndex != -1) {
            int nextDealerIndex = (dealerIndex + 1) % players.size();
            players.get(nextDealerIndex).setDealer(true);
        } else {
            players.get(0).setDealer(true);
        }
    }
}
