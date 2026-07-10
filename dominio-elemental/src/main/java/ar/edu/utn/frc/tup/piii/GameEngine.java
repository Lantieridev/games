package ar.edu.utn.frc.tup.piii;

import ar.edu.utn.frc.tup.piii.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameEngine {
    private Deck deck;
    private List<Combination> table;
    private Player human;
    private Player machine;
    private final Scanner scanner = new Scanner(System.in);
    private Long seed;
    private boolean pacingEnabled = true;

    /** Forces a deterministic deck shuffle, for reproducible tests/simulations. */
    public void setSeed(long seed) {
        this.seed = seed;
    }

    /** Disabled in tests so a full simulated game doesn't pause 1.5s between every round. */
    public void setPacingEnabled(boolean enabled) {
        this.pacingEnabled = enabled;
    }

    public Player getHuman() { return human; }
    public Player getMachine() { return machine; }
    public List<Combination> getTable() { return table; }
    public Deck getDeck() { return deck; }

    public void init() {
        ConsoleUI.clearScreen();
        ConsoleUI.printBanner();
        System.out.print(ConsoleUI.WHITE_BOLD + "Ingresa tu nombre para comenzar: " + ConsoleUI.RESET);
        String name = scanner.nextLine();

        deck = (seed != null) ? new Deck(seed) : new Deck();
        table = new ArrayList<>();
        human = new HumanPlayer(name, scanner);
        machine = new MachinePlayer("IA-Alpha");

        // Initial deal
        for (int i = 0; i < 5; i++) {
            human.addCard(deck.draw());
            machine.addCard(deck.draw());
        }
    }

    public void play() {
        while (!deck.isEmpty()) {
            human.playTurn(deck, table);
            human.discard();
            
            if (deck.isEmpty()) break;

            machine.playTurn(deck, table);
            machine.discard();
            
            showStatus();
        }
        endGame();
    }

    private void showStatus() {
        System.out.println("\n" + ConsoleUI.WHITE_BOLD + "--- ESTADO DE LA PARTIDA ---" + ConsoleUI.RESET);
        System.out.println(ConsoleUI.GREEN + "JUGADOR: " + human.getName() + " | PUNTOS: " + human.getScore() + ConsoleUI.RESET);
        System.out.println(ConsoleUI.RED + "IA: " + machine.getName() + " | PUNTOS: " + machine.getScore() + ConsoleUI.RESET);
        System.out.println("Cartas restantes en mazo: " + deck.size());
        System.out.println("----------------------------");
        if (pacingEnabled) {
            try { Thread.sleep(1500); } catch (InterruptedException e) {} // Give time to read
        }
    }

    private void endGame() {
        ConsoleUI.clearScreen();
        System.out.println("\n========================================");
        System.out.println("           ¡FIN DEL JUEGO!             ");
        System.out.println("========================================");
        
        if (human.getScore() > machine.getScore()) {
            ConsoleUI.printVictory(human.getName(), human.getScore());
        } else if (machine.getScore() > human.getScore()) {
            ConsoleUI.printDefeat(human.getName(), human.getScore());
        } else {
            System.out.println("¡ES UN EMPATE! (" + human.getScore() + " pts)");
        }
        
        System.out.println("\nPuntaje IA: " + machine.getScore() + " pts");
    }
}
