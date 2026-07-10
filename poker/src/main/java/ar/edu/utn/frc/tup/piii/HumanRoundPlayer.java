package ar.edu.utn.frc.tup.piii;

import java.util.List;

public class HumanRoundPlayer extends RoundPlayer {

    public HumanRoundPlayer(Player player, Boolean dealer) {
        super(player, dealer);
    }

    public HumanRoundPlayer() {
        super();
    }

    @Override
    protected void playPreFlop(List<Card> tableCards, RoundStage roundStage,
                             List<RoundPlayer> players, Integer bucketChip) {
        String gold = LetterByLetterPrinter.YELLOW + LetterByLetterPrinter.BOLD;
        String white = LetterByLetterPrinter.WHITE + LetterByLetterPrinter.BOLD;
        String reset = LetterByLetterPrinter.RESET;

        int minToCall = getCurrentMaxBet(players);
        int answer = -1;
        do {
            System.out.println(white + "YOUR TURN (PRE-FLOP)" + reset);
            System.out.println(LetterByLetterPrinter.CYAN + "0." + reset + " Bet");
            System.out.println(LetterByLetterPrinter.CYAN + "1." + reset + " Fold");
            LetterByLetterPrinter.print("Choice: ", gold);
            
            try {
                answer = this.scanner.nextInt();
                if (answer < 0 || answer > 1) {
                    LetterByLetterPrinter.println("Invalid option.", LetterByLetterPrinter.RED);
                } else {
                    switch (answer) {
                        case 0:
                            doBet(minToCall);
                            break;
                        case 1:
                            doFold();
                            break;
                    }
                }
            }catch (Exception e) {
                LetterByLetterPrinter.println("Please enter a number.", LetterByLetterPrinter.RED);
                scanner.next();
            }
        } while (answer < 0 || answer > 1);
    }

    @Override
    protected void playFlop(List<Card> tableCards, RoundStage roundStage, List<RoundPlayer> players, Integer bucketChip) {
        playAfterPreFlop(players);
    }

    @Override
    protected void playTurn(List<Card> tableCards, RoundStage roundStage, List<RoundPlayer> players, Integer bucketChip) {
        playAfterPreFlop(players);
    }

    @Override
    protected void playRiver(List<Card> tableCards, RoundStage roundStage, List<RoundPlayer> players, Integer bucketChip) {
        playAfterPreFlop(players);
    }

    private void playAfterPreFlop(List<RoundPlayer> players) {
        String gold = LetterByLetterPrinter.YELLOW + LetterByLetterPrinter.BOLD;
        String white = LetterByLetterPrinter.WHITE + LetterByLetterPrinter.BOLD;
        String reset = LetterByLetterPrinter.RESET;
        
        int minToCall = getCurrentMaxBet(players);
        int answer = -1;
        do {
            System.out.println(white + "YOUR TURN" + reset);
            System.out.println(LetterByLetterPrinter.CYAN + "0." + reset + " Bet");
            System.out.println(LetterByLetterPrinter.CYAN + "1." + reset + " Fold");
            System.out.println(LetterByLetterPrinter.CYAN + "2." + reset + " Call");
            if(minToCall == 0) {
                System.out.println(LetterByLetterPrinter.CYAN + "3." + reset + " Check");
            }
            LetterByLetterPrinter.print("Choice: ", gold);
            
            try {
                answer = this.scanner.nextInt();
                if (answer < 0 || answer > 4) {
                    LetterByLetterPrinter.println("Invalid option.", LetterByLetterPrinter.RED);
                } else {
                    switch (answer) {
                        case 0:
                            doBet(minToCall);
                            break;
                        case 1:
                            doFold();
                            break;
                        case 2:
                            doCall(players);
                            break;
                        case 3:
                            doCheck();
                            break;
                    }
                }
            }catch (Exception e) {
                LetterByLetterPrinter.println("Please enter a number.", LetterByLetterPrinter.RED);
                scanner.next();
            }
        } while (answer < 0 || answer > 4);
    }

    private void doCall(List<RoundPlayer> players) {
        LetterByLetterPrinter.println(">>> You called the current bet.", LetterByLetterPrinter.CYAN);
        int betChips = this.getBetChips();
        int currentMaxBet = getCurrentMaxBet(players);
        Integer chipsToCall = currentMaxBet - betChips;
        if (chipsToCall < super.getPlayer().getChips()) {
            bet(chipsToCall);
        } else {
            LetterByLetterPrinter.println("Not enough chips to fully call. Going All-In!", LetterByLetterPrinter.YELLOW);
            doBet(chipsToCall);
        }
    }

    private void doFold() {
        LetterByLetterPrinter.println(">>> You folded your hand.", LetterByLetterPrinter.RED);
        this.setFolded(true);
    }

    private void doCheck() {
        LetterByLetterPrinter.println(">>> You checked.", LetterByLetterPrinter.CYAN);
        this.setBetChips(0);
    }

    private void doBet(Integer minToCall) {
        LetterByLetterPrinter.print("Amount to bet: ", LetterByLetterPrinter.YELLOW);
        try {
            int bet = this.scanner.nextInt();
            if (bet > this.getPlayer().getChips()) {
                LetterByLetterPrinter.println("Insufficient chips for this bet.", LetterByLetterPrinter.RED);
                doBet(minToCall);
            } else if (bet + this.getBetChips() < minToCall && !isAllInBet(bet)) {
                LetterByLetterPrinter.println("Bet must at least match the current call amount.", LetterByLetterPrinter.RED);
                doBet(minToCall);
            }  else if (bet + this.getBetChips() >= minToCall) {
                bet(bet, minToCall);
            } else {
                LetterByLetterPrinter.println("Invalid amount.", LetterByLetterPrinter.RED);
            }
        } catch (Exception e) {
            LetterByLetterPrinter.println("Please enter a valid numeric amount.", LetterByLetterPrinter.RED);
            scanner.next();
        }
    }

}
