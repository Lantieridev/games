package ar.edu.utn.frc.tup.piii.model;

import java.util.Collections;
import java.util.Random;
import java.util.Stack;

public class Deck {
    private final Stack<Card> cards;

    public Deck() {
        this(new Random());
    }

    /** Deterministic shuffle, for reproducible tests/simulations. */
    public Deck(long seed) {
        this(new Random(seed));
    }

    private Deck(Random random) {
        cards = new Stack<>();
        for (Element e : Element.values()) {
            for (int i = 1; i <= 9; i++) {
                cards.push(new Card(e, i));
            }
        }
        Collections.shuffle(cards, random);
    }

    public Card draw() {
        return cards.isEmpty() ? null : cards.pop();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public int size() {
        return cards.size();
    }
}
