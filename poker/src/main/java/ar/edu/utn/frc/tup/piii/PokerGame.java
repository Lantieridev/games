package ar.edu.utn.frc.tup.piii;

import java.util.Scanner;
import java.util.regex.Pattern;

public class PokerGame {

    public static final String YES_NO_REGEX = "[yYnN]";

    private Scanner scanner = new Scanner(System.in);

    public Scanner getScanner() {
        return scanner;
    }

    public void setScanner(Scanner scanner) {
        this.scanner = scanner;
    }

    public void welcomeMessage() {
        String gold = LetterByLetterPrinter.YELLOW + LetterByLetterPrinter.BOLD;
        String reset = LetterByLetterPrinter.RESET;
        
        System.out.println(gold + "    __________  __ __ __________ " + reset);
        System.out.println(gold + "   / ____/ __ \\/ //_// ____/ __ \\" + reset);
        System.out.println(gold + "  / /   / / / / ,<  / __/ / /_/ /" + reset);
        System.out.println(gold + " / /___/ /_/ / /| |/ /___/ _, _/ " + reset);
        System.out.println(gold + " \\____/\\____/_/ |_/_____/_/ |_|  " + reset);
        System.out.println();
        
        LetterByLetterPrinter.println("Welcome to the " + gold + "Texas Hold'em Poker" + reset + " Engine!", LetterByLetterPrinter.CYAN);
        LetterByLetterPrinter.println("Strategic calculation and nerves of steel are required.", LetterByLetterPrinter.WHITE);
        System.out.println();
    }

    public Player createHumanUser() {
        Player player = new Player();
        LetterByLetterPrinter.println("Enter your name to start playing");
        player.setName(scanner.nextLine());
        player.setChips(1000);
        player.setBot(false);
        return player;
    }

    public Player createAppPlayers(String name) {
        return new Player(name, 1000, true);
    }

    public Boolean wantPlayAgain() {
        Boolean answer = null;
        do {
            LetterByLetterPrinter.println("Do you want to play again? (y/n)");
            String input = scanner.nextLine();
            answer = getYesNoAnswer(input);
        } while (answer == null);
        return answer;
    }

    public static Boolean getYesNoAnswer(String input) {
        Pattern pattern = Pattern.compile(YES_NO_REGEX);
        Boolean answer = null;
        if (pattern.matcher(input).matches()) {
            answer = input.equalsIgnoreCase("y");
        } else {
            LetterByLetterPrinter.println("Invalid answer. Please enter 'y' or 'n'");
        }
        return answer;
    }
}
