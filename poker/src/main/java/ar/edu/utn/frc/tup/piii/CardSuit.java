package ar.edu.utn.frc.tup.piii;

/**
 * Enum with all possible suits of a deck of french cards.
 */
public enum CardSuit {

    DIAMONDS("\u001B[31m♦\u001B[0m"),
    HEARTS("\u001B[31m♥\u001B[0m"),
    CLUBS("♣"),
    SPADES("♠");

    private final String suitSymbol;

    CardSuit(String suitSymbol) {
        this.suitSymbol = suitSymbol;
    }
    public String getSuitSymbol() {
        return suitSymbol;
    }
}
