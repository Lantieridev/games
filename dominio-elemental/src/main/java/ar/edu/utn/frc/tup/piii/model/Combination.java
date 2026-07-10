package ar.edu.utn.frc.tup.piii.model;

import java.util.List;
import java.util.stream.Collectors;

public abstract class Combination {
    protected List<Card> cards;

    public Combination(List<Card> cards) {
        this.cards = cards;
    }

    public abstract int calculateScore();
    public abstract String getName();
    public abstract boolean canAdd(Card card);
    
    public void addCard(Card card) {
        cards.add(card);
    }

    public List<Card> getCards() {
        return cards;
    }

    @Override
    public String toString() {
        return getName() + ": " + cards.stream().map(Card::toString).collect(Collectors.joining(", ")) + 
               " (Score: " + calculateScore() + ")";
    }
}
