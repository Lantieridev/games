package ar.edu.utn.frc.tup.piii;

import ar.edu.utn.frc.tup.piii.model.Card;
import ar.edu.utn.frc.tup.piii.model.Combination;
import java.util.List;

public class ConsoleUI {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE_BOLD = "\033[1;37m";

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printBanner() {
        System.out.println(CYAN + "██████╗  ██████╗ ███╗   ███╗██╗███╗   ██╗██╗ ██████╗ " + RESET);
        System.out.println(CYAN + "██╔══██╗██╔═══██╗████╗ ████║██║████╗  ██║██║██╔═══██╗" + RESET);
        System.out.println(CYAN + "██║  ██║██║   ██║██╔████╔██║██║██╔██╗ ██║██║██║   ██║" + RESET);
        System.out.println(CYAN + "██║  ██║██║   ██║██║╚██╔╝██║██║██║╚██╗██║██║██║   ██║" + RESET);
        System.out.println(CYAN + "██████╔╝╚██████╔╝██║ ╚═╝ ██║██║██║ ╚████║██║╚██████╔╝" + RESET);
        System.out.println(CYAN + "╚═════╝  ╚═════╝ ╚═╝     ╚═╝╚═╝╚═╝  ╚═══╝╚═╝ ╚═════╝ " + RESET);
        System.out.println(YELLOW + "               E L E M E N T A L                     " + RESET);
        System.out.println("=====================================================");
    }

    public static void renderHand(List<Card> hand) {
        if (hand == null || hand.isEmpty()) return;
        
        String[] lines = {"", "", "", ""};
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            String color = getColorForElement(card.element().name());
            String symbol = getSymbolForElement(card.element().name());
            String name = card.element().name().substring(0, Math.min(card.element().name().length(), 5));
            
            lines[0] += color + " ┌─────────┐ " + RESET;
            lines[1] += color + " │ " + symbol + " " + String.format("%-5s", name) + " │ " + RESET;
            lines[2] += color + " │   [" + card.value() + "]   │ " + RESET;
            lines[3] += color + " └─────────┘ " + RESET;
        }
        
        for (String line : lines) {
            System.out.println(line);
        }

        // Print indices below
        for (int i = 0; i < hand.size(); i++) {
            System.out.print("      (" + i + ")     ");
        }
        System.out.println();
    }

    public static void renderTable(List<Combination> table) {
        System.out.println(WHITE_BOLD + "\n--- MESA COMPARTIDA ---" + RESET);
        if (table.isEmpty()) {
            System.out.println("  [ Mesa Vacia ]");
        } else {
            for (int i = 0; i < table.size(); i++) {
                System.out.print(WHITE_BOLD + i + ". " + table.get(i).getName() + ": " + RESET);
                for (Card c : table.get(i).getCards()) {
                    System.out.print(getColorForElement(c.element().name()) + "[" + c.element().name().charAt(0) + c.value() + "] " + RESET);
                }
                System.out.println(YELLOW + " (" + table.get(i).calculateScore() + " pts)" + RESET);
            }
        }
        System.out.println("-----------------------");
    }

    private static String getColorForElement(String element) {
        return switch (element) {
            case "FUEGO" -> RED;
            case "AGUA" -> BLUE;
            case "TIERRA" -> GREEN;
            case "AIRE" -> CYAN;
            default -> RESET;
        };
    }

    private static String getSymbolForElement(String element) {
        return switch (element) {
            case "FUEGO" -> "🔥";
            case "AGUA" -> "💧";
            case "TIERRA" -> "🌱";
            case "AIRE" -> "💨";
            default -> " ";
        };
    }

    public static void printVictory(String name, int score) {
        System.out.println(YELLOW + "****************************************" + RESET);
        System.out.println(YELLOW + "          ¡VICTORIA MAGISTRAL!          " + RESET);
        System.out.println(YELLOW + "****************************************" + RESET);
        System.out.println(CYAN + "   " + name + " ha dominado los elementos" + RESET);
        System.out.println(WHITE_BOLD + "   Puntaje Final: " + score + " pts" + RESET);
        System.out.println(YELLOW + "          🏆  ¡ERES EL MAESTRO!         " + RESET);
        System.out.println(YELLOW + "****************************************" + RESET);
    }

    public static void printDefeat(String name, int score) {
        System.out.println(RED + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" + RESET);
        System.out.println(RED + "           DERROTA ELEMENTAL            " + RESET);
        System.out.println(RED + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" + RESET);
        System.out.println(WHITE_BOLD + "   " + name + " ha sido superado por la IA" + RESET);
        System.out.println(WHITE_BOLD + "   Puntaje Final: " + score + " pts" + RESET);
        System.out.println(RED + "           💀  Sigue practicando...     " + RESET);
        System.out.println(RED + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx" + RESET);
    }
}
