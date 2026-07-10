package com.utn.buscaminas;

import java.util.Scanner;
import java.util.HashMap;
import java.util.Map;

public class ConsoleUI {
    private final Scanner scanner;
    private final Map<String, Integer> playerWins;

    /**
     * Optional fixed seed for the board's mine layout. Null in normal play
     * (real randomness); set by tests to drive a deterministic, reproducible
     * game end-to-end through this same class instead of mocking it away.
     */
    private Long seed;

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    // ANSI Colors
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE_BOLD = "\033[1;37m";

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.playerWins = new HashMap<>();
    }

    public void start() {
        clearScreen();
        printBanner();

        System.out.print(WHITE_BOLD + "Ingrese su nombre para comenzar: " + RESET);
        String playerName = scanner.nextLine();

        boolean keepPlaying = true;
        while (keepPlaying) {
            playRound(playerName);
            System.out.print("\n¿Desea jugar otra partida? (S/N): ");
            String choice = scanner.nextLine().toUpperCase();
            keepPlaying = choice.equals("S");
        }

        showFinalStats();
        System.out.println("¡Gracias por jugar!");
    }

    private void playRound(String playerName) {
        System.out.println("\nSeleccione dificultad:");
        System.out.println("1. Facil (10x10, 10 minas)");
        System.out.println("2. Dificil (20x20, 20 minas)");
        System.out.print("Opcion: ");

        Difficulty difficulty = Difficulty.EASY;
        try {
            int opt = Integer.parseInt(scanner.nextLine());
            if (opt == 2) difficulty = Difficulty.HARD;
        } catch (NumberFormatException e) {
            System.out.println("Opcion no valida, usando Facil por defecto.");
        }

        Game game = (seed != null) ? new Game(playerName, difficulty, seed) : new Game(playerName, difficulty);

        while (!game.isGameOver()) {
            displayBoard(game.getBoard());
            System.out.print("\nIngrese coordenadas (fila columna) o 'Q' para salir: ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("Q")) {
                System.out.println("Partida abandonada.");
                return;
            }

            try {
                String[] parts = input.split("\\s+");
                if (parts.length != 2) throw new Exception();

                int r = Integer.parseInt(parts[0]);
                int c = Integer.parseInt(parts[1]);

                if (!game.revealCell(r, c)) {
                    System.out.println("Coordenada invalida o ya revelada.");
                }
            } catch (Exception e) {
                System.out.println("Entrada invalida. Use el formato: fila columna (ej: 0 0)");
            }
        }

        displayBoard(game.getBoard());
        if (game.isWon()) {
            printVictory();
            playerWins.put(playerName, playerWins.getOrDefault(playerName, 0) + 1);
        } else {
            printGameOver();
        }
    }

    private void displayBoard(Board board) {
        int size = board.getSize();
        System.out.print("   ");
        for (int j = 0; j < size; j++) {
            System.out.printf("%2d ", j);
        }
        System.out.println();

        for (int i = 0; i < size; i++) {
            System.out.printf("%2d ", i);
            for (int j = 0; j < size; j++) {
                System.out.print(board.getCell(i, j).toColoredString() + " ");
            }
            System.out.println();
        }
    }

    private void showFinalStats() {
        System.out.println("\n--- Estadisticas de Partidas Ganadas ---");
        if (playerWins.isEmpty()) {
            System.out.println("No hay victorias registradas.");
        } else {
            playerWins.forEach((name, wins) -> 
                System.out.println("Jugador: " + name + " | Victorias: " + wins));
        }
    }

    private void printBanner() {
        System.out.println(CYAN + "██████╗ ██╗   ██╗███████╗ ██████╗ █████╗ ███╗   ███╗██╗███╗   ██╗ █████╗ ███████╗" + RESET);
        System.out.println(CYAN + "██╔══██╗██║   ██║██╔════╝██╔════╝██╔══██╗████╗ ████║██║████╗  ██║██╔══██╗██╔════╝" + RESET);
        System.out.println(CYAN + "██████╔╝██║   ██║███████╗██║     ███████║██╔████╔██║██║██╔██╗ ██║███████║███████╗" + RESET);
        System.out.println(CYAN + "██╔══██╗██║   ██║╚════██║██║     ██╔══██║██║╚██╔╝██║██║██║╚██╗██║██╔══██║╚════██║" + RESET);
        System.out.println(CYAN + "██████╔╝╚██████╔╝███████║╚██████╗██║  ██║██║ ╚═╝ ██║██║██║ ╚████║██║  ██║███████║" + RESET);
        System.out.println(CYAN + "╚═════╝  ╚═════╝ ╚══════╝ ╚═════╝╚═╝  ╚═╝╚═╝     ╚═╝╚═╝╚═╝  ╚═══╝╚═╝  ╚═╝╚══════╝" + RESET);
        System.out.println(YELLOW + "                       M I N E S W E E P E R                                  " + RESET);
        System.out.println("================================================================================");
    }

    public void printGameOver() {
        clearScreen();
        System.out.println(RED + "              _ ._  _ , _ ._             " + RESET);
        System.out.println(RED + "            (_ ' ( `  )_  .__)           " + RESET);
        System.out.println(RED + "          ( (  (    )   `)  ) _)         " + RESET);
        System.out.println(RED + "         (__ (_   (_ . _) _) ,__)        " + RESET);
        System.out.println(RED + "             `~~`\\ ' . /`~~`             " + RESET);
        System.out.println(RED + "                  |   |                  " + RESET);
        System.out.println(RED + "                  |   |                  " + RESET);
        System.out.println(RED + "                  '-' '-'                " + RESET);
        System.out.println(RED + "\n        ¡¡¡ B O O O O O O M !!!          " + RESET);
        System.out.println(WHITE_BOLD + "       Has tocado una mina. Fin del juego." + RESET);
        System.out.println(RED + "==========================================" + RESET);
    }

    public void printVictory() {
        System.out.println(YELLOW + "******************************************" + RESET);
        System.out.println(YELLOW + "        ¡CAMPO DESPEJADO CON ÉXITO!       " + RESET);
        System.out.println(YELLOW + "******************************************" + RESET);
        System.out.println(CYAN + "   🏆  ¡Eres un experto artificiero!     " + RESET);
        System.out.println(YELLOW + "******************************************" + RESET);
    }

    private void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
