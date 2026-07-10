package ar.edu.utn.frc.tup.piii;

import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Deck {

    private final Stack<Card> cards;

    public Stack<Card> getCards() {
        return cards;
    }

    public Deck() {
        this.cards = new Stack<Card>();
        createDeck();
        shuffleDeck();
    }

    private void createDeck() {
        // TODO: Implementar la creación de las cartas del mazo. Recuerde que son barajas francesas de 52 cartas
        //  ya que no se incluyen los comodines.
        //  Las cartas poseen un CardSuit y un CardSymbol
        //  Debe crear un mazo completo
    }

    public Card takeCard() {
        return cards.pop();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    private void shuffleDeck() {
        Collections.shuffle(cards);
    }

}
