package ar.edu.utn.frc.tup.lciii.round;

import ar.edu.utn.frc.tup.lciii.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AppRoundPlayer extends RoundPlayer {

    private static final Card SEVEN_ORO = new Card(CardSuit.OROS, 7, 7);

    public AppRoundPlayer(User player) {
        super(player);
    }

    @Override
    public void playTurn(List<Card> tableCards) {
        LetterByLetterPrinter.println("\n" + "=".repeat(44), LetterByLetterPrinter.WHITE);
        LetterByLetterPrinter.println("➤ APP TURN", LetterByLetterPrinter.PURPLE + LetterByLetterPrinter.BOLD);
        showCardsOnTheTable(tableCards);
        List<Card> selectedCards = selectCardsApp(tableCards);
        if (selectedCards != null) {
            takingCardsFromTable(tableCards, selectedCards);
        } else {
            Card cardToDiscard = getCardToDiscard();
            LetterByLetterPrinter.println("🤖 App discards: " + cardToDiscard.toFormattedString(), LetterByLetterPrinter.WHITE);
            tableCards.add(cardToDiscard);
            this.getHandCards().remove(cardToDiscard);
        }
        LetterByLetterPrinter.println("=".repeat(44), LetterByLetterPrinter.WHITE + "\n");
    }

    private Card getCardToDiscard() {
        return this.getHandCards().get(this.getHandCards().size() - 1);
    }

    /**
     * Este método selecciona las cartas que el jugador APP va a jugar.
     * Para esto sigue la siguiente lógica:
     * 1. Busca todas las combinaciones posibles de cartas entre las que están en la mesa y las que tiene en la mano.
     * Siempre que estas sumen 15.
     * 2. Ordena las combinaciones de mayor a menor por cantidad de cartas,
     * quedando arriba de la lista la que más cartas tiene.
     * 3. Si no encuentra ninguna combinación que sume 15, retorna null.
     * 4. Si encuentra solo una combinación que sume 15, selecciona dicha combinación.
     * 5. Si encuentra más de una combinación que sume 15, selecciona por el siguiente orden de prioridad:
     *   a. La combinación que contenga una escoba.
     *   b. La combinación que contenga el 7 de oro.
     *   c. La combinación que contenga más cartas de oro.
     *   d. La combinación que contenga más 7.
     *   e. La combinación que contenga más cartas. En este caso,
     *   la primera de la lista, ya la misma está ordenada por cantidad de cartas.
     *
     * @param tableCards cartas en la mesa.
     *
     * @return lista de cartas seleccionadas por el jugador APP.
     */
    private List<Card> selectCardsApp(List<Card> tableCards) {
        List<List<Card>> subconjuntos = new ArrayList<>();
        for (Card handCard : this.getHandCards()) {
            List<Card> conjunto = new ArrayList<>(tableCards);
            conjunto.add(handCard);
            List<List<Card>> subconjuntosAux = obtenerSubconjuntos(conjunto);
            subconjuntosAux.removeIf(c -> !c.contains(handCard));
            subconjuntosAux.removeIf(c -> c.stream().mapToInt(Card::getValue).sum() != 15);
            subconjuntos.addAll(subconjuntosAux);
        }

        if (subconjuntos.isEmpty()) {
            return null;
        }

        subconjuntos.sort((a, b) -> b.size() - a.size());

        List<Card> escoba = getCardsWithEscoba(subconjuntos, tableCards.size() + 1);
        if (escoba != null) {
            return escoba;
        }

        List<Card> sevenOro = getCardsWithSevenOro(subconjuntos);
        if (sevenOro != null) {
            return sevenOro;
        }

        List<Card> moreOros = getCardsWithMoreOros(subconjuntos);
        if (moreOros != null) {
            return moreOros;
        }

        List<Card> moreSevens = getCardsWithSeven(subconjuntos);
        if (moreSevens != null) {
            return moreSevens;
        }

        return subconjuntos.get(0);
    }

    /**
     * Método que obtiene el subconjunto que contiene más cartas del tipo 7 o null si no existe ninguno.
     *
     * @param subconjuntos lista de subconjuntos en los que buscar.
     * @return el subconjunto que contiene más 7 o null si no existe.
     */
    private List<Card> getCardsWithSeven(List<List<Card>> subconjuntos) {
        List<Card> best = null;
        int maxSevens = 0;
        for (List<Card> sub : subconjuntos) {
            int sevens = (int) sub.stream().filter(c -> c.getNumber() == 7).count();
            if (sevens > maxSevens) {
                maxSevens = sevens;
                best = sub;
            }
        }
        return best;
    }

    /**
     * Método que obtiene el subconjunto que contiene una escoba o null si no existe ninguno.
     * Este método valída que un subconjunto de cartas sea igual a la
     * cantidad pasada por parámetro (cartas de la mesa + 1).
     *
     * @param subconjuntos lista de subconjuntos en los que buscar.
     * @param cardsQuantityToEscoba cantidad de cartas necesarias para hacer escoba. Igual a cartas de la mesa + 1.
     * @return el subconjunto que contiene una escoba o null si no existe.
     */
    private List<Card> getCardsWithEscoba(List<List<Card>> subconjuntos, Integer cardsQuantityToEscoba) {
        for (List<Card> sub : subconjuntos) {
            if (sub.size() == cardsQuantityToEscoba) {
                return sub;
            }
        }
        return null;
    }

    /**
     * Método que obtiene el subconjunto que contiene el 7 de oro o null si no existe ninguno.
     *
     * @param subconjuntos lista de subconjuntos en los que buscar.
     * @return el subconjunto que contiene el 7 de oro o null si no existe.
     */
    private List<Card> getCardsWithSevenOro(List<List<Card>> subconjuntos) {
        for (List<Card> sub : subconjuntos) {
            if (sub.contains(SEVEN_ORO)) {
                return sub;
            }
        }
        return null;
    }

    /**
     * Método que obtiene el subconjunto que contiene más cartas de oro o null si no existe ninguno.
     *
     * @param subconjuntos lista de subconjuntos en los que buscar.
     * @return el subconjunto que contiene más oros o null si no existe.
     */
    private List<Card> getCardsWithMoreOros(List<List<Card>> subconjuntos) {
        List<Card> best = null;
        int maxOros = 0;
        for (List<Card> sub : subconjuntos) {
            int oros = (int) sub.stream().filter(c -> c.getCardSuit().equals(CardSuit.OROS)).count();
            if (oros > maxOros) {
                maxOros = oros;
                best = sub;
            }
        }
        return best;
    }

}
