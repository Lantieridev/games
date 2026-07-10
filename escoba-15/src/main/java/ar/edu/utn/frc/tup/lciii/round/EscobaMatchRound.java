package ar.edu.utn.frc.tup.lciii.round;

import ar.edu.utn.frc.tup.lciii.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa cada una de las rondas que se juegan durante una parida.
 * En una partida se jugarán tantas rondas como sean necesarias hasta
 * que haya un ganador de la partida.
 *
 */
public class EscobaMatchRound {

    /**
     * Mazo de cartas de usado en la ronda.
     */
    private Deck deck;

    /**
     * Jugador de la ronda que representa al usuario.
     * RoundPlayer es una clase distinta de player, que no representa al usuario en sí,
     * sino al usuario en una ronda puntual
     */
    private RoundPlayer roundPlayerHuman;

    /**
     * Jugador de la ronda que representa a la app.
     * RoundPlayer es una clase distinta de player, que no representa al usuario en sí,
     * sino al usuario en una ronda puntual
     */
    private RoundPlayer roundPlayerApp;

    /**
     * Lista de cartas en la mesa durante la ronda.
     */
    private List<Card> tableCards;

    /**
     * El último jugador que se llevó cartas de la mesa. Cuando la ronda termina
     * (se acaba el mazo y ambas manos), las cartas que quedan en la mesa sin
     * capturar se le entregan a este jugador — no quedan sin dueño.
     */
    private RoundPlayer lastCapturer;

    public Deck getDeck() {
        return deck;
    }

    public void setDeck(Deck deck) {
        this.deck = deck;
    }

    public RoundPlayer getRoundPlayerHuman() {
        return roundPlayerHuman;
    }

    public void setRoundPlayerHuman(RoundPlayer roundPlayerHuman) {
        this.roundPlayerHuman = roundPlayerHuman;
    }

    public RoundPlayer getRoundPlayerApp() {
        return roundPlayerApp;
    }

    public void setRoundPlayerApp(RoundPlayer roundPlayerApp) {
        this.roundPlayerApp = roundPlayerApp;
    }

    public List<Card> getTableCards() {
        return tableCards;
    }

    public void setTableCards(List<Card> tableCards) {
        this.tableCards = tableCards;
    }

    public EscobaMatchRound(User player, User app) {
        this.deck = new Deck();
        this.roundPlayerHuman = new HumanRoundPlayer(player);
        this.roundPlayerApp = new AppRoundPlayer(app);
        this.tableCards = new ArrayList<>();
    }

    /**
     * Este método inicia la ronda repartiendo 3 cartas a cada jugador y 4 cartas a la mesa.
     * Las cartas se reparten de manera intercalada entre el jugador y la app. Empezando a repartir
     * por el jugador que NO es dealer.
     *
     * @param isPlayerHumanDealer true si el jugador humano es el repartidor, false si la app es el repartidor
     *
     * @see this#dealCards(boolean)
     * @see Deck#takeCard()
     */
    public void startRound(Boolean isPlayerHumanDealer) {
        dealCards(isPlayerHumanDealer);
        for(int i = 0; i < 4; i++) {
            tableCards.add(deck.takeCard());
        }
        checkEscobaEnMesa(isPlayerHumanDealer);
    }

    /**
     * Este método verifica si la suma de los números de las cartas en la mesa es igual a 15.
     * Si la suma es 15, el jugador que repartió las cartas se lleva las cartas de la mesa y suma una escoba.
     *
     * @param isPlayerHumanDealer true si el jugador humano es el repartidor, false si la app es el repartidor
     */
    private void checkEscobaEnMesa(Boolean isPlayerHumanDealer) {
        Integer sum = 0;
        for(Card card : tableCards) {
            sum += card.getNumber();
        }
        if(sum == 15) {
            LetterByLetterPrinter.println(System.lineSeparator() + "Escoba en la mesa! The player who deals the cards takes them.");
            if(isPlayerHumanDealer) {
                roundPlayerHuman.setEscobasQuantity(roundPlayerHuman.getEscobasQuantity() + 1);
                roundPlayerHuman.getPersonalDeck().addAll(tableCards);
            } else {
                roundPlayerApp.setEscobasQuantity(roundPlayerApp.getEscobasQuantity() + 1);
                roundPlayerApp.getPersonalDeck().addAll(tableCards);
            }
            tableCards.clear();
        }
    }

    /**
     * Este método reparte 3 cartas a cada jugador.
     * Las cartas se reparten de manera intercalada entre el jugador y la app. Empezando a repartir
     * por el jugador que NO es dealer.
     *
     * @param isPlayerHumanDealer true si el jugador humano es el repartidor, false si la app es el repartidor
     *
     * @see RoundPlayer#getHandCards()
     * @see Deck#takeCard()
     */
    public void dealCards(boolean isPlayerHumanDealer) {
        LetterByLetterPrinter.println(System.lineSeparator() + "============================================");
        LetterByLetterPrinter.println("Dealing cards...");
        for (int i = 0; i < 3; i++) {
            if (isPlayerHumanDealer) {
                roundPlayerApp.getHandCards().add(deck.takeCard());
                roundPlayerHuman.getHandCards().add(deck.takeCard());
            } else {
                roundPlayerHuman.getHandCards().add(deck.takeCard());
                roundPlayerApp.getHandCards().add(deck.takeCard());
            }
        }
        LetterByLetterPrinter.println("============================================" + System.lineSeparator());
    }

    /**
     *
     *
     * @param isPlayerDealer
     */
    public void playRound(boolean isPlayerDealer) {
        boolean playerTurn = !isPlayerDealer;
        do {
            do {
                RoundPlayer currentPlayer = playerTurn ? roundPlayerHuman : roundPlayerApp;
                int cardsOnTableBefore = tableCards.size();
                currentPlayer.playTurn(tableCards);
                if (tableCards.size() < cardsOnTableBefore + 1) {
                    // The player's card plus at least one table card were captured
                    // (a discard always leaves exactly one more card than before).
                    lastCapturer = currentPlayer;
                }
                playerTurn = !playerTurn;
            } while (!isHandFinish());
            if (!isRoundFinish()) {
                dealCards(isPlayerDealer);
            }
        } while (!isRoundFinish());
        sweepRemainingTableCardsToLastCapturer();
        calculateRoundScore();
    }

    /**
     * Le entrega las cartas que quedaron sin reclamar en la mesa al último
     * jugador que capturó cartas, siguiendo la regla tradicional de la Escoba
     * de 15. No cuenta como escoba (el jugador no jugó una carta para lograrlo).
     */
    private void sweepRemainingTableCardsToLastCapturer() {
        if (tableCards.isEmpty() || lastCapturer == null) {
            return;
        }
        LetterByLetterPrinter.println(System.lineSeparator() + "🧹 " + lastCapturer.getPlayer().getName()
                + " takes the remaining table cards (last to capture).", LetterByLetterPrinter.CYAN);
        lastCapturer.getPersonalDeck().addAll(tableCards);
        tableCards.clear();
    }

    /**
     * Método para calcular los puntos obtenidos por cada jugador en la ronda
     * Suma en los puntos de cada jugador sus escobas y reparte los puntos por
     * cantidad de cartas, siete de oro, cantidad de siete y cantidad de oros.
     *
     * @see EscobaMatchRound#solveSevenWinner()
     * @see EscobaMatchRound#solveOrosWinner()
     * @see EscobaMatchRound#solveSevenOroWinner()
     * @see EscobaMatchRound#solveQuantityCardsWinner()
     */
    private void calculateRoundScore() {
        LetterByLetterPrinter.println("\n" + "★".repeat(20) + " ROUND SUMMARY " + "★".repeat(20), LetterByLetterPrinter.YELLOW + LetterByLetterPrinter.BOLD);
        roundPlayerHuman.setRoundPoints(roundPlayerHuman.getRoundPoints() + roundPlayerHuman.getEscobasQuantity());
        roundPlayerApp.setRoundPoints(roundPlayerApp.getRoundPoints() + roundPlayerApp.getEscobasQuantity());
        solveSevenWinner();
        solveSevenOroWinner();
        solveOrosWinner();
        solveQuantityCardsWinner();
        
        LetterByLetterPrinter.println("\n┌────────────── ROUND SCORE ──────────────┐", LetterByLetterPrinter.WHITE + LetterByLetterPrinter.BOLD);
        LetterByLetterPrinter.println(String.format("│ %-20s : %-15d │", roundPlayerHuman.getPlayer().getName(), roundPlayerHuman.getRoundPoints()), LetterByLetterPrinter.CYAN);
        LetterByLetterPrinter.println(String.format("│ %-20s : %-15d │", "APP", roundPlayerApp.getRoundPoints()), LetterByLetterPrinter.PURPLE);
        LetterByLetterPrinter.println("└─────────────────────────────────────────┘", LetterByLetterPrinter.WHITE + LetterByLetterPrinter.BOLD);
    }

    /**
     * Resuelve cúal de los 2 jugadores se lleva el punto por tener más siete.
     *
     * @see EscobaMatchRound#sevenQuantity(List)
     */
    private void solveSevenWinner() {
        Integer humanSevens = sevenQuantity(roundPlayerHuman.getPersonalDeck());
        Integer appSevens = sevenQuantity(roundPlayerApp.getPersonalDeck());

        if (humanSevens > appSevens) {
            LetterByLetterPrinter.println("✅ The human player wins 1 point for most sevens.", LetterByLetterPrinter.GREEN);
            roundPlayerHuman.setRoundPoints(roundPlayerHuman.getRoundPoints() + 1);
            if (humanSevens == 4) {
                LetterByLetterPrinter.println("🌟 The human player wins an extra point for having all sevens!", LetterByLetterPrinter.YELLOW);
                roundPlayerHuman.setRoundPoints(roundPlayerHuman.getRoundPoints() + 1);
            }
        } else if (appSevens > humanSevens) {
            LetterByLetterPrinter.println("🤖 The app wins 1 point for most sevens.", LetterByLetterPrinter.PURPLE);
            roundPlayerApp.setRoundPoints(roundPlayerApp.getRoundPoints() + 1);
            if (appSevens == 4) {
                LetterByLetterPrinter.println("🌟 The app wins an extra point for having all sevens!", LetterByLetterPrinter.YELLOW);
                roundPlayerApp.setRoundPoints(roundPlayerApp.getRoundPoints() + 1);
            }
        } else {
            LetterByLetterPrinter.println("⚖️ No points for sevens. Both players have the same quantity.", LetterByLetterPrinter.WHITE);
        }
    }

    /**
     * Resuelve cúal de los 2 jugadores se lleva el punto por tener el siete de oro.
     *
     * @see EscobaMatchRound#sevenOroCheck(List)
     */
    private void solveSevenOroWinner() {
        if (sevenOroCheck(roundPlayerHuman.getPersonalDeck()) && !sevenOroCheck(roundPlayerApp.getPersonalDeck())) {
            LetterByLetterPrinter.println("🟡 The player wins 1 point for the Seven of Oros!", LetterByLetterPrinter.YELLOW);
            roundPlayerHuman.setRoundPoints(roundPlayerHuman.getRoundPoints() + 1);
        } else if (!sevenOroCheck(roundPlayerHuman.getPersonalDeck()) && sevenOroCheck(roundPlayerApp.getPersonalDeck())) {
            LetterByLetterPrinter.println("🟡 The app wins 1 point for the Seven of Oros!", LetterByLetterPrinter.YELLOW);
            roundPlayerApp.setRoundPoints(roundPlayerApp.getRoundPoints() + 1);
        }
    }

    /**
     * Resuelve cúal de los 2 jugadores se lleva el punto por tener más cartas de oro.
     *
     * @see EscobaMatchRound#orosQuantity(List)
     */
    private void solveOrosWinner() {
        if (orosQuantity(roundPlayerHuman.getPersonalDeck()) > orosQuantity(roundPlayerApp.getPersonalDeck())) {
            LetterByLetterPrinter.println("💰 The player wins 1 point for most Oros.", LetterByLetterPrinter.YELLOW);
            roundPlayerHuman.setRoundPoints(roundPlayerHuman.getRoundPoints() + 1);
            if(orosQuantity(roundPlayerHuman.getPersonalDeck()) == 10) {
                LetterByLetterPrinter.println("👑 The player wins an extra point for having ALL OROS!", LetterByLetterPrinter.YELLOW + LetterByLetterPrinter.BOLD);
                roundPlayerHuman.setRoundPoints(roundPlayerHuman.getRoundPoints() + 1);
            }
        } else if (orosQuantity(roundPlayerHuman.getPersonalDeck()) < orosQuantity(roundPlayerApp.getPersonalDeck())) {
            LetterByLetterPrinter.println("💰 The app wins 1 point for most Oros.", LetterByLetterPrinter.PURPLE);
            roundPlayerApp.setRoundPoints(roundPlayerApp.getRoundPoints() + 1);
            if(orosQuantity(roundPlayerApp.getPersonalDeck()) == 10) {
                LetterByLetterPrinter.println("👑 The app wins an extra point for having ALL OROS!", LetterByLetterPrinter.PURPLE + LetterByLetterPrinter.BOLD);
                roundPlayerApp.setRoundPoints(roundPlayerApp.getRoundPoints() + 1);
            }
        } else {
            LetterByLetterPrinter.println("⚖️ No points for Oros. Both players have the same quantity.", LetterByLetterPrinter.WHITE);
        }
    }

    /**
     * Resuelve cúal de los 2 jugadores se lleva el punto por tener mayor cantidad de cartas.
     *
     * @see EscobaMatchRound#quantityCards(List)
     */
    private void solveQuantityCardsWinner() {
        if (roundPlayerHuman.getPersonalDeck().size() > roundPlayerApp.getPersonalDeck().size()) {
            LetterByLetterPrinter.println("📦 The player wins 1 point for most cards (" + roundPlayerHuman.getPersonalDeck().size() + ").", LetterByLetterPrinter.GREEN);
            roundPlayerHuman.setRoundPoints(roundPlayerHuman.getRoundPoints() + 1);
        } else if (roundPlayerHuman.getPersonalDeck().size() < roundPlayerApp.getPersonalDeck().size()) {
            LetterByLetterPrinter.println("📦 The app wins 1 point for most cards (" + roundPlayerApp.getPersonalDeck().size() + ").", LetterByLetterPrinter.PURPLE);
            roundPlayerApp.setRoundPoints(roundPlayerApp.getRoundPoints() + 1);
        } else {
            LetterByLetterPrinter.println("⚖️ No points for quantity of cards. Both players have the same amount.", LetterByLetterPrinter.WHITE);
        }
    }

    /**
     * Evalúa si la mano ha terminado.
     * Una mano es cada una de las veces que se vuelven a repartir cartas durante una ronda.
     *
     * @return true si ambos jugadores ya jugarón todas sus cartas en la mano, si no retorna false.
     *
     * @see RoundPlayer#getHandCards()
     */
    private boolean isHandFinish() {
        return roundPlayerHuman.getHandCards().isEmpty() && roundPlayerApp.getHandCards().isEmpty();
    }

    /**
     * Evalúa si la ronda ha terminado.
     *
     * @return true si ambos jugadores ya jugarón todas sus cartas en la mano
     * y no quedan cartas en el deck (mazo) para jugar otra mano, si no retorna false.
     *
     * @see RoundPlayer#getHandCards()
     * @see Deck#isEmpty()
     */
    private boolean isRoundFinish() {
        return roundPlayerHuman.getHandCards().isEmpty() && roundPlayerApp.getHandCards().isEmpty() && deck.isEmpty();
    }

    /**
     * Verifica en la lista de cartas, cuantas de ellas son del palo oro.
     *
     * @param cards lista de cartas a verificar
     *
     * @return la cantidad de cartas de oro que tiene la lista que recibe como parámetro
     */
    private Integer orosQuantity(List<Card> cards) {
        Integer quantity = 0;
        for (Card card : cards) {
            if (card.getCardSuit().equals(CardSuit.OROS)) {
                quantity++;
            }
        }
        return quantity;
    }

    /**
     * Verifica en la lista de cartas, cuantas de ellas son siete.
     *
     * @param cards lista de cartas a verificar
     *
     * @return la cantidad de siete que tiene la lista que recibe como parámetro
     */
    private Integer sevenQuantity(List<Card> cards) {
        Integer quantity = 0;
        for (Card card : cards) {
            if (card.getNumber().equals(7)) {
                quantity++;
            }
        }
        return quantity;
    }

    /**
     * Este método verifica si existe el siete de oro en la lista de cartas que recibe como parámetro.
     *
     * @param cards lista de cartas a verificar
     *
     * @return true si en la lista existe el siete de oro, false si no existe
     */
    private Boolean sevenOroCheck(List<Card> cards) {
        for (Card card : cards) {
            if (card.getNumber().equals(7) && card.getCardSuit().equals(CardSuit.OROS)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Este cuenta la cantidad de cartas que hay en la lista que recibe como parámetro.
     *
     * @param cards lista de cartas a verificar
     *
     * @return la cantidad de cartas que tiene la lista que recibe como parámetro
     */
    private Integer quantityCards(List<Card> cards) {
        return cards.size();
    }

}
