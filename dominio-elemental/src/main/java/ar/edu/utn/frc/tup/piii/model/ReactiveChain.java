package ar.edu.utn.frc.tup.piii.model;

import java.util.List;

public class ReactiveChain extends Combination {
    public ReactiveChain(List<Card> cards) {
        super(cards);
    }

    @Override
    public int calculateScore() {
        return cards.stream().mapToInt(Card::value).sum() * 2;
    }

    @Override
    public String getName() {
        return "Cadena Reactiva";
    }

    @Override
    public boolean canAdd(Card card) {
        return false; // Una cadena reactiva ya tiene los 4 elementos obligatorios
    }
}
