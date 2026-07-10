package ar.edu.utn.frc.tup.piii;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MesaPokerPrinter {

    public static void printMesaPoker(List<RoundPlayer> players, List<Card> tableCards, Integer pot) {
        String gold = LetterByLetterPrinter.YELLOW + LetterByLetterPrinter.BOLD;
        String cyan = LetterByLetterPrinter.CYAN + LetterByLetterPrinter.BOLD;
        String reset = LetterByLetterPrinter.RESET;
        String white = LetterByLetterPrinter.WHITE + LetterByLetterPrinter.BOLD;
        String gray = LetterByLetterPrinter.WHITE;

        System.out.println("\n" + LetterByLetterPrinter.CYAN + "=".repeat(100) + reset);
        System.out.println(gold + "                                     🃏 TEXAS HOLD'EM POKER 🃏" + reset);
        System.out.println(LetterByLetterPrinter.CYAN + "=".repeat(100) + reset + "\n");

        // Players sit in two rows of up to 3; the table shrinks as players bust out
        printPlayerRow(players, 0, gold, reset);

        System.out.println("\n    " + white + "┌" + "─".repeat(90) + "┐" + reset);
        System.out.println("    " + white + "│" + " ".repeat(90) + "│" + reset);

        String cardsStr = tableCards.stream().map(Card::toString).collect(Collectors.joining(" "));
        System.out.printf("    " + white + "│" + " ".repeat(25) + "COMMUNITY CARDS: %-48s " + white + "│%n" + reset,
                cyan + (cardsStr.isEmpty() ? "[ Waiting... ]" : cardsStr) + reset);

        System.out.println("    " + white + "│" + " ".repeat(90) + "│" + reset);
        System.out.printf("    " + white + "│" + " ".repeat(35) + gold + "💰 TOTAL POT: %-37s " + white + "│%n" + reset,
                gold + pot + " 🪙" + reset);

        System.out.println("    " + white + "│" + " ".repeat(90) + "│" + reset);
        System.out.println("    " + white + "└" + "─".repeat(90) + "┘" + reset + "\n");

        printPlayerRow(players, 3, gold, reset);

        System.out.println("\n" + LetterByLetterPrinter.CYAN + "─".repeat(100) + reset);
        RoundPlayer human = players.stream().filter(p -> !p.getPlayer().getBot()).findFirst().orElse(null);
        if (human != null) {
            String hand = human.getHandCards().stream().map(Card::toString).collect(Collectors.joining(" "));
            System.out.println("   " + white + "YOUR HAND: " + cyan + hand + reset);

            int maxBet = players.stream().mapToInt(RoundPlayer::getBetChips).max().orElse(0);
            int toCall = maxBet - human.getBetChips();
            if (toCall > 0) {
                System.out.println("   " + gray + "To call: " + gold + toCall + " chips" + reset);
            } else {
                System.out.println("   " + gray + "You are matched — check or raise." + reset);
            }
        }
        System.out.println(LetterByLetterPrinter.CYAN + "─".repeat(100) + reset + "\n");
    }

    /**
     * Prints up to 3 players starting at {@code fromIndex}. Table used to assume
     * exactly 6 seats; now it degrades gracefully as players get eliminated.
     */
    private static void printPlayerRow(List<RoundPlayer> players, int fromIndex, String gold, String reset) {
        List<RoundPlayer> row = players.stream()
                .skip(fromIndex)
                .limit(3)
                .collect(Collectors.toList());
        if (row.isEmpty()) {
            return;
        }

        StringBuilder names = new StringBuilder();
        StringBuilder chips = new StringBuilder();
        StringBuilder dealer = new StringBuilder();
        for (RoundPlayer p : row) {
            names.append(String.format("        [%-18s]              ", formatPlayerInfo(p)));
            chips.append(String.format("     Chips: %-18s         ", gold + p.printDinero() + reset + (p.getFolded() ? " (folded)" : "")));
            dealer.append(String.format("        %-18s                 ", p.getDealer() ? gold + "⭕ DEALER" + reset : " "));
        }
        System.out.println(names);
        System.out.println(chips);
        System.out.println(dealer);
    }

    private static String formatPlayerInfo(RoundPlayer p) {
        String color = p.getPlayer().getBot() ? LetterByLetterPrinter.WHITE : LetterByLetterPrinter.GREEN + LetterByLetterPrinter.BOLD;
        return color + p.getPlayer().getName() + (p.getPlayer().getBot() ? " (Bot)" : " (You)") + LetterByLetterPrinter.RESET;
    }

}
