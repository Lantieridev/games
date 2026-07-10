package ar.edu.utn.frc.tup.piii;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PokerMatch {

    private Scanner scanner = new Scanner(System.in);

    private List<Player> players;

    private Player winner;

    private Integer currentDealerIndex = 0;

    public PokerMatch(List<Player> players) {
        this.players = players;
        this.winner = null;
    }

    public Boolean isFinish() {
        int alivePlayers = 0;
        for (Player player : players) {
            if (player.getChips() > 0) {
                alivePlayers++;
            }
        }
        return alivePlayers <= 1;
    }

    public List<RoundPlayer> generateRoundPlayers() {
        List<RoundPlayer> roundPlayers = new ArrayList<>();
        for (int i = 0; i < players.size(); i++) {
            RoundPlayer roundPlayer;
            if (players.get(i).getBot()) {
                roundPlayer = new BotRoundPlayer();
            } else {
                roundPlayer = new HumanRoundPlayer();
            }
            roundPlayer.setPlayer(players.get(i));
            roundPlayer.setDealer(i == currentDealerIndex);
            roundPlayer.setAllIn(false);
            roundPlayer.setFolded(false);
            roundPlayer.setBetChips(0);
            roundPlayer.setHandCards(new ArrayList<>());
            roundPlayers.add(roundPlayer);
        }
        return roundPlayers;
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }
}
