package ar.edu.utn.frc.tup.lciii;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * Clase que representa un mazo de cartas españolas.
 */
public class Deck {

    /**
     * Lista de cartas del mazo
     */
    private final Stack<Card> cards;

    public Stack<Card> getCards() {
        return cards;
    }

    /**
     * Constructor de la clase Deck
     * El constructor crea la lista de cartas y las mezcla
     * Como el juego se juega con 40 cartas, el constructor retorna un mazo sin los 8 y 9 y sin los Jokers
     *
     * @see Deck#cards
     * @see Card
     * @see Stack
     * @see Deck#createDeck()
     */
    public Deck() {
        this.cards = new Stack<Card>();
        createDeck();
        shuffleDeck(new Random());
    }

    /**
     * Constructor con semilla fija, pensado para tests deterministas — evita
     * que un test dependa de una mezcla verdaderamente aleatoria y falle
     * al azar cuando el resultado no es el esperado (por ejemplo, una
     * escoba en la mesa inicial que no todas las semillas producen).
     *
     * @param seed semilla para el generador aleatorio de la mezcla
     */
    public Deck(long seed) {
        this.cards = new Stack<Card>();
        createDeck();
        shuffleDeck(new Random(seed));
    }

    /**
     * Crea las cartas del mazo
     * El mazo se crea con 40 cartas, sin los 8 y 9 y sin los Jokers
     *
     * @see Deck#cards
     * @see Card
     */
    private void createDeck() {
        for (CardSuit suit : CardSuit.values()) {
            for (int i = 1; i <= 12; i++) {
                if (i == 8 || i == 9) {
                    continue;
                }
                int value = i;
                if (i == 10) {
                    value = 8;
                } else if (i == 11) {
                    value = 9;
                } else if (i == 12) {
                    value = 10;
                }
                cards.push(new Card(suit, i, value));
            }
        }
    }

    /**
     * Toma una carta del mazo y la quita del mismo
     *
     * @return la carta tomada
     *
     * @see Deck#cards
     * @see Card
     * @see Stack
     */
    public Card takeCard() {
        return cards.pop();
    }

    /**
     * Verifica si el mazo está vacío
     *
     * @return true si el mazo está vacío, false en caso contrario
     *
     * @see Deck#cards
     * @see Stack#isEmpty()
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Mezcla la lista de cartas que recibe como parámetro
     *
     * @see Deck#cards
     * @see Collections#shuffle(List)
     */
    private void shuffleDeck(Random random) {
        Collections.shuffle(cards, random);
    }

}
