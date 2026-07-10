package ar.edu.utn.frc.tup.piii.model;

import java.util.Comparator;
import java.util.List;

public class Sequence extends Combination {
    public Sequence(List<Card> cards) {
        super(cards);
        this.cards.sort(Comparator.comparingInt(Card::value));
    }

    @Override
    public int calculateScore() {
        return cards.stream().mapToInt(Card::value).sum();
    }

    @Override
    public String getName() {
        return "Secuencia";
    }

    @Override
    public boolean canAdd(Card card) {
        if (card.element() != cards.get(0).element()) return false;
        int min = cards.stream().mapToInt(Card::value).min().orElse(0);
        int max = cards.stream().mapToInt(Card::value).max().orElse(0);
        return card.value() == min - 1 || card.value() == max + 1;
    }

    @Override
    public void addCard(Card card) {
        super.addCard(card);
        this.cards.sort(Comparator.comparingInt(Card::value));
    }
}
