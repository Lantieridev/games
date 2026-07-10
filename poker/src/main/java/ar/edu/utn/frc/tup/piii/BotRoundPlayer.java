package ar.edu.utn.frc.tup.piii;

import java.util.*;

// TODO: Bonus track -> ¿Que tipo de cambios le harias a esta clase para que tenga mayor
//  integligencia y no solo apueste siempre??? Te animas? -> Solo si ya terminaste los puntos de evaluación.
public class BotRoundPlayer extends RoundPlayer {

    public BotRoundPlayer(Player player, Boolean dealer) {
        super(player, dealer);
    }

    public BotRoundPlayer() {
        super();
    }

    @Override
    protected void playPreFlop(List<Card> tableCards, RoundStage roundStage, List<RoundPlayer> players, Integer bucketChip) {
        doCall(players);
    }

    @Override
    protected void playFlop(List<Card> tableCards, RoundStage roundStage, List<RoundPlayer> players, Integer bucketChip) {
        doCall(players);
    }

    @Override
    protected void playTurn(List<Card> tableCards, RoundStage roundStage, List<RoundPlayer> players, Integer bucketChip) {
        doCall(players);
    }

    @Override
    protected void playRiver(List<Card> tableCards, RoundStage roundStage, List<RoundPlayer> players, Integer bucketChip) {
        doCall(players);
    }

    private void doCall(List<RoundPlayer> players) {
        int betChips = this.getBetChips();
        int currentMaxBet = getCurrentMaxBet(players);
        if(currentMaxBet == 0 && super.getPlayer().getChips() > 0) {
            bet(10);
            return;
        }
        Integer chipsToCall = currentMaxBet - betChips;
        if (chipsToCall < super.getPlayer().getChips()) {
            bet(chipsToCall);
        } else {
            doAllIn();
        }
    }

    private void doAllIn() {
        super.setAllIn(true);
        bet(this.getPlayer().getChips());
    }
}
