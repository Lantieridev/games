package ar.edu.utn.frc.tup.piii.model;

import java.util.ArrayList;
import java.util.List;

public abstract class Player {
    protected String name;
    protected List<Card> hand;
    protected int score;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.score = 0;
    }

    public void addCard(Card card) {
        if (card != null) hand.add(card);
    }

    public void removeCards(List<Card> cardsToRemove) {
        hand.removeAll(cardsToRemove);
    }

    public void addScore(int points) {
        this.score += points;
    }

    public String getName() { return name; }
    public List<Card> getHand() { return hand; }
    public int getScore() { return score; }

    public abstract void playTurn(Deck deck, List<Combination> table);
    public abstract Card discard();
}
