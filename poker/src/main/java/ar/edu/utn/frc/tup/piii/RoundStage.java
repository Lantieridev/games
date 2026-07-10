package ar.edu.utn.frc.tup.piii;

public enum RoundStage {

    PRE_FLOP, FLOP, TURN, RIVER, SHOWDOWN;

    public static RoundStage getNextStage(RoundStage stage) {
        switch (stage) {
            case PRE_FLOP:
                return FLOP;
            case FLOP:
                return TURN;
            case TURN:
                return RIVER;
            case RIVER:
                return SHOWDOWN;
            default:
                return null;
        }
    }
}
