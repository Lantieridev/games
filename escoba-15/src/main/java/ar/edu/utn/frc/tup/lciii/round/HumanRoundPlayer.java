package ar.edu.utn.frc.tup.lciii.round;

import ar.edu.utn.frc.tup.lciii.*;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;

public class HumanRoundPlayer extends RoundPlayer {

    public HumanRoundPlayer(User player) {
        super(player);
    }

    public HumanRoundPlayer() {
        super();
    }

    /**
     * Este método representa el turno de un jugador humano.
     * @param tableCards
     */
    @Override
    public void playTurn(List<Card> tableCards) {
        LetterByLetterPrinter.println(System.lineSeparator() + "=".repeat(44), LetterByLetterPrinter.WHITE);
        LetterByLetterPrinter.println("➤ " + getPlayer().getName() + " TURN", LetterByLetterPrinter.CYAN + LetterByLetterPrinter.BOLD);
        showCardsOnTheTable(tableCards);
        int rta = getCardToPlay();

        Card selectedCard = this.getHandCards().get(rta);
        List<Card> conjunto = new ArrayList<>(tableCards);
        conjunto.add(selectedCard);
        List<List<Card>> subconjuntosAux = obtenerSubconjuntos(conjunto);
        subconjuntosAux.removeIf(c -> !c.contains(selectedCard));
        subconjuntosAux.removeIf(c -> c.stream().mapToInt(Card::getValue).sum() != 15);
        List<List<Card>> subconjuntos = new ArrayList<>(subconjuntosAux);

        if(subconjuntos.isEmpty()) {
            LetterByLetterPrinter.println("You can't make 15 with this card. The card was added to the table.");
            tableCards.add(selectedCard);
            this.getHandCards().remove(selectedCard);
        } else {
            int rtaSub = 0;
            if (subconjuntos.size() > 1) {
                rtaSub = getSubconjuntoToPlay(subconjuntos);
            }
            takingCardsFromTable(tableCards, subconjuntos.get(rtaSub));
        }
        LetterByLetterPrinter.println("=".repeat(44) + System.lineSeparator(), LetterByLetterPrinter.WHITE);
    }

    /**
     * Este método captura la carta que el jugador humano quiere jugar.
     * @return el índice de la carta en la mano del jugador
     */
    protected int getCardToPlay() {
        LetterByLetterPrinter.println("Your hand:", LetterByLetterPrinter.PURPLE + LetterByLetterPrinter.BOLD);
        for (int i = 0; i < this.getHandCards().size(); i++) {
            LetterByLetterPrinter.println("  " + i + " - " + this.getHandCards().get(i).toFormattedString());
        }
        boolean isValidCard = false;
        int rta = -1;
        do {
            LetterByLetterPrinter.print("Please, select a card to play: ", LetterByLetterPrinter.WHITE);
            try {
                rta = getScanner().nextInt();
                if (rta >= 0 && rta < this.getHandCards().size()) {
                    isValidCard = true;
                } else {
                    LetterByLetterPrinter.println("Invalid index. Please select a valid card.");
                }
            } catch (InputMismatchException e) {
                LetterByLetterPrinter.println("Invalid input. Please enter a number.");
                getScanner().next();
            }
        } while (!isValidCard);
        return rta;
    }

    /**
     * Este método captura el subconjunto que el jugador humano quiere jugar en caso de que existan mas de uno.
     * @param subconjuntos lista de subconjuntos posibles
     * @return el índice del subconjunto seleccionado
     */
    private int getSubconjuntoToPlay(List<List<Card>> subconjuntos) {
        boolean isValidSubconjunto = false;
        int rtaSubconjunto = -1;
        LetterByLetterPrinter.println(System.lineSeparator() + "Multiple combinations of 15 possible! Choose one:", LetterByLetterPrinter.CYAN + LetterByLetterPrinter.BOLD);
        do {
            for (int i = 0; i < subconjuntos.size(); i++) {
                String listCards = subconjuntos.get(i).stream().map(Card::toFormattedString).reduce("", (a, b) -> a + " " + b);
                LetterByLetterPrinter.println("  [" + i + "]" + listCards);
            }
            try {
                rtaSubconjunto = getScanner().nextInt();
                if (rtaSubconjunto >= 0 && rtaSubconjunto < subconjuntos.size()) {
                    isValidSubconjunto = true;
                } else {
                    LetterByLetterPrinter.println("Invalid index. Please select a valid option.");
                }
            } catch (InputMismatchException e) {
                LetterByLetterPrinter.println("Invalid input. Please enter a number.");
                getScanner().next();
            }
        } while (!isValidSubconjunto);
        return rtaSubconjunto;
    }
}
