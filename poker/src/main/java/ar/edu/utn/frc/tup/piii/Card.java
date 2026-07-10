package ar.edu.utn.frc.tup.piii;

import java.util.Objects;

public class Card {

    private CardSuit cardSuit;

    private CardSymbol cardSymbol;

    public CardSuit getCardSuit() {
        return cardSuit;
    }

    public void setCardSuit(CardSuit cardSuit) {
        this.cardSuit = cardSuit;
    }

    public CardSymbol getCardSymbol() {
        return cardSymbol;
    }

    public void setCardSymbol(CardSymbol cardSymbol) {
        this.cardSymbol = cardSymbol;
    }

    public Card(CardSuit cardSuit, CardSymbol cardSymbol) {
        this.cardSuit = cardSuit;
        this.cardSymbol = cardSymbol;
    }

    @Override
    public String toString() {
        return "[" + cardSymbol.getSymbol() + cardSuit.getSuitSymbol() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Card other = (Card) obj;
        if (this.cardSuit != other.cardSuit) {
            return false;
        }
        if (!Objects.equals(this.cardSymbol, other.cardSymbol)) {
            return false;
        }
        return true;
    }
}
