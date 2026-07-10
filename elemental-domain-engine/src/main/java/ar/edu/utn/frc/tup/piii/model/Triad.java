package ar.edu.utn.frc.tup.piii.model;

import java.util.List;

public class Triad extends Combination {
    public Triad(List<Card> cards) {
        super(cards);
    }

    @Override
    public int calculateScore() {
        return cards.get(0).value() * 3;
    }

    @Override
    public String getName() {
        return "Tríada";
    }

    @Override
    public boolean canAdd(Card card) {
        if (card.value() != cards.get(0).value()) return false;
        if (cards.size() >= 4) return false;
        return cards.stream().noneMatch(c -> c.element() == card.element());
    }
}
