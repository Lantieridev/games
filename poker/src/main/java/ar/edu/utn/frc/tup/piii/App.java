package ar.edu.utn.frc.tup.piii;

import java.util.ArrayList;
import java.util.List;

/**
 * Hello to Exam - POKER Texas Hold'em
 *
 */
public class App 
{

    private static final PokerGame pokerGame = new PokerGame();

    private static final String[] appNames = {"AceBot","BluffMaster","ChipCrusher","RoyalFlushAI","DealerDroid"};

    /*
     * This is the main program
     * IMPORTANTE: Este método no necesita ser modificado
     */
    public static void main( String[] args ) {
        pokerGame.welcomeMessage();
        Player player = pokerGame.createHumanUser();
        Boolean playAgain = true;
        // Bucle principal
        do {
            List<Player> players = new ArrayList<>();
            players.add(player);
            for (int i = 0; i < 5; i++) {
                players.add(pokerGame.createAppPlayers(appNames[i]));
            }
            PokerMatch pokerMatch = new PokerMatch(players);
            LetterByLetterPrinter.println(System.lineSeparator() + "We are ready to play!!!");
            List<RoundPlayer> roundPlayers = pokerMatch.generateRoundPlayers();
            // Bucle de la partida
            do {
                removeBustedPlayers(roundPlayers);
                if (roundPlayers.size() < 2) {
                    break;
                }
                PokerRound pokerRound = new PokerRound(roundPlayers);
                pokerRound.playFullRound();
                pokerRound.resetRound();
            } while (!pokerMatch.isFinish());
            playAgain = pokerGame.wantPlayAgain();
        } while (playAgain);
    }

    /**
     * Removes players with no chips left from the table and announces them as
     * eliminated. Without this, a busted player kept getting dealt into every
     * following round instead of being knocked out of the match.
     */
    private static void removeBustedPlayers(List<RoundPlayer> roundPlayers) {
        List<RoundPlayer> busted = roundPlayers.stream()
                .filter(rp -> rp.getPlayer().getChips() <= 0)
                .toList();
        for (RoundPlayer roundPlayer : busted) {
            LetterByLetterPrinter.println(roundPlayer.getPlayer().getName() + " is out of chips and eliminated!");
            roundPlayers.remove(roundPlayer);
        }
    }
}
