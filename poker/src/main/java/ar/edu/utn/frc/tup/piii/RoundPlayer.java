package ar.edu.utn.frc.tup.piii;

import java.util.*;

public abstract class RoundPlayer {

    protected Scanner scanner = new Scanner(System.in);

    private Player player;
    private List<Card> handCards;
    private Integer betChips;
    private boolean dealer;
    private boolean isAllIn;
    private boolean isFolded;

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public List<Card> getHandCards() {
        return handCards;
    }

    public void setHandCards(List<Card> handCards) {
        this.handCards = handCards;
    }

    public Integer getBetChips() {
        return betChips;
    }

    public void setBetChips(Integer betChips) {
        this.betChips = betChips;
    }

    public boolean getDealer() {
        return dealer;
    }

    public void setDealer(boolean dealer) {
        this.dealer = dealer;
    }

    public boolean getAllIn() {
        return isAllIn;
    }

    public void setAllIn(boolean allIn) {
        this.isAllIn = allIn;
    }

    public boolean getFolded() {
        return isFolded;
    }

    public void setFolded(boolean folded) {
        this.isFolded = folded;
    }

    public RoundPlayer(Player player, boolean dealer) {
        this.player = player;
        this.handCards = new ArrayList<>();
        this.betChips = 0;
        this.dealer = dealer;
        this.isAllIn = false;
        this.isFolded = false;
    }

    public RoundPlayer() {
    }

    public Map.Entry<List<Card>, PokerHandRank> getBestHand(List<Card> tableCards) {
        List<Card> allCards = new ArrayList<>();
        allCards.addAll(tableCards);
        allCards.addAll(handCards);

        List<List<Card>> combinations = new ArrayList<>();
        generateCombinations(allCards, 0, new ArrayList<>(), combinations, 5);

        Map<List<Card>, PokerHandRank> rankedHands = evaluateHand(combinations);

        Map.Entry<List<Card>, PokerHandRank> bestHand = null;

        for (Map.Entry<List<Card>, PokerHandRank> entry : rankedHands.entrySet()) {
            if (bestHand == null || HandEvaluator.isBetterHand(entry, bestHand)) {
                bestHand = entry;
            }
        }

        return bestHand;
    }

    public void playStage(List<Card> tableCards, RoundStage roundStage,
                                   List<RoundPlayer> players, Integer bucketChip) {
        //LetterByLetterPrinter.println("Is " + this.player.getName() + " turn.");
        if(roundStage == RoundStage.PRE_FLOP) {
            playPreFlop(tableCards, roundStage, players, bucketChip);
        } else if (roundStage == RoundStage.FLOP) {
            playFlop(tableCards, roundStage, players, bucketChip);
        } else if (roundStage == RoundStage.TURN) {
            playTurn(tableCards, roundStage, players, bucketChip);
        } else if (roundStage == RoundStage.RIVER) {
            playRiver(tableCards, roundStage, players, bucketChip);
        }
    }

    protected abstract void playPreFlop(List<Card> tableCards, RoundStage roundStage,
                                        List<RoundPlayer> players, Integer bucketChip);

    protected abstract void playFlop(List<Card> tableCards, RoundStage roundStage,
                                        List<RoundPlayer> players, Integer bucketChip);

    protected abstract void playTurn(List<Card> tableCards, RoundStage roundStage,
                                        List<RoundPlayer> players, Integer bucketChip);

    protected abstract void playRiver(List<Card> tableCards, RoundStage roundStage,
                                        List<RoundPlayer> players, Integer bucketChip);

    protected void bet(Integer chips, Integer minToCall) {
        String cyan = LetterByLetterPrinter.CYAN;
        String reset = LetterByLetterPrinter.RESET;
        
        if(Objects.equals(chips, minToCall)) {
            LetterByLetterPrinter.println(">>> " + player.getName() + " calls the bet.", cyan);
        }
        if(chips > minToCall) {
            LetterByLetterPrinter.println(">>> " + player.getName() + " raises the bet!", LetterByLetterPrinter.YELLOW);
        }
        bet(chips);
    }

    protected void bet(Integer chips) {
        String gold = LetterByLetterPrinter.YELLOW + LetterByLetterPrinter.BOLD;
        String reset = LetterByLetterPrinter.RESET;
        
        LetterByLetterPrinter.println(">>> " + player.getName() + " bets " + gold + chips + reset + " chips.", LetterByLetterPrinter.WHITE);
        this.betChips += chips;
        this.player.setChips(this.player.getChips() - chips);
        if(this.player.getChips() == 0) {
            LetterByLetterPrinter.println("🔥 " + player.getName() + " IS ALL-IN!! 🔥", LetterByLetterPrinter.RED + LetterByLetterPrinter.BOLD);
            this.isAllIn = true;
        }
    }

    protected boolean isAllInBet(int bet) {
        return bet == this.getPlayer().getChips();
    }

    protected Integer getCurrentMaxBet(List<RoundPlayer> players) {
        return players.stream()
                .mapToInt(RoundPlayer::getBetChips)
                .max()
                .orElse(0);
    }

    private Map<List<Card>, PokerHandRank> evaluateHand(List<List<Card>> hands) {
        Map<List<Card>, PokerHandRank> pokerHandRankMap = new HashMap<>();
        for (List<Card> cards : hands) {
            if (HandEvaluator.isRoyalFlush(cards)) {
                pokerHandRankMap.put(cards, PokerHandRank.ROYAL_FLUSH);
            } else if (HandEvaluator.isStraightFlush(cards)) {
                pokerHandRankMap.put(cards, PokerHandRank.STRAIGHT_FLUSH);
            } else if (HandEvaluator.isPoker(cards)) {
                pokerHandRankMap.put(cards, PokerHandRank.POKER);
            } else {
                int triosCount = HandEvaluator.countTrios(cards);
                int pairsCount = HandEvaluator.countPairs(cards);

                if (triosCount >= 1 && pairsCount >= 1) {
                    pokerHandRankMap.put(cards, PokerHandRank.FULL_HOUSE);
                } else if (HandEvaluator.isFlush(cards)) {
                    pokerHandRankMap.put(cards, PokerHandRank.FLUSH);
                } else if (HandEvaluator.isStraight(cards)) {
                    pokerHandRankMap.put(cards, PokerHandRank.STRAIGHT);
                } else if (triosCount == 1) {
                    pokerHandRankMap.put(cards, PokerHandRank.THREE_OF_A_KING);
                } else if (pairsCount >= 1) {
                    pokerHandRankMap.put(cards, pairsCount >= 2 ? PokerHandRank.TWO_PAIR : PokerHandRank.ONE_PAIR);
                } else {
                    pokerHandRankMap.put(cards, PokerHandRank.HIGH_CARD);
                }
            }
        }
        return pokerHandRankMap;
    }


    private void generateCombinations(List<Card> cards, int start, List<Card> current,
                                      List<List<Card>> result, int k) {
        // Si hemos seleccionado exactamente k cartas, agregamos esta combinación al resultado
        if (current.size() == k) {
            result.add(new ArrayList<>(current));
            return;
        }

        // Si no hay suficientes cartas restantes para completar una combinación de 5, retornamos
        if (start >= cards.size() || current.size() + (cards.size() - start) < k) {
            return;
        }

        // Incluir la carta actual en la combinación
        current.add(cards.get(start));
        generateCombinations(cards, start + 1, current, result, k);

        // No incluir la carta actual en la combinación
        current.remove(current.size() - 1);
        generateCombinations(cards, start + 1, current, result, k);
    }

    public String printInfo() {
        return this.player.getName();
    }

    public String printDinero() {
        return String.format("💰%d | 💸%d", player.getChips(), betChips);
    }

    public String printCards() {
        return String.format("[%s][%s]", this.handCards.get(0).toString(), this.handCards.get(1).toString());
    }
}
