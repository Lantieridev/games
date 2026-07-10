package ar.edu.utn.frc.tup.piii;

import ar.edu.utn.frc.tup.piii.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GameSimulator {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Deck deck = new Deck();
        List<Combination> table = new ArrayList<>();
        Player p1 = new MachinePlayer("IA-Martin");
        Player p2 = new MachinePlayer("IA-Alpha");

        // Initial deal
        for (int i = 0; i < 5; i++) {
            p1.addCard(deck.draw());
            p2.addCard(deck.draw());
        }

        ConsoleUI.clearScreen();
        ConsoleUI.printBanner();
        System.out.println("\n[PAUSA PARA CAPTURA: Banner Inicial] Presiona Enter para comenzar la simulacion...");
        scanner.nextLine();

        System.out.println(">>> INICIANDO SIMULACION RAPIDA <<<");
        
        while (!deck.isEmpty()) {
            p1.playTurn(deck, table);
            p1.discard();
            if (deck.isEmpty()) break;
            p2.playTurn(deck, table);
            p2.discard();
            
            if (table.size() > 1) {
                System.out.println("\n[PAUSA PARA CAPTURA: Mesa con jugadas] Presiona Enter para continuar...");
                scanner.nextLine();
            }
        }

        // Final score display using GameEngine's logic style
        ConsoleUI.clearScreen();
        System.out.println("\n=== RESULTADO DE LA SIMULACION ===");
        if (p1.getScore() > p2.getScore()) {
            ConsoleUI.printVictory(p1.getName(), p1.getScore());
            System.out.println("\nIA Derrotada: " + p2.getName() + " (" + p2.getScore() + " pts)");
        } else if (p2.getScore() > p1.getScore()) {
            ConsoleUI.printVictory(p2.getName(), p2.getScore());
            System.out.println("\nIA Derrotada: " + p1.getName() + " (" + p1.getScore() + " pts)");
        } else {
            System.out.println("¡ES UN EMPATE! (" + p1.getScore() + " pts)");
        }
        System.out.println("\n[PAUSA PARA CAPTURA: Pantalla Final] Presiona Enter para cerrar...");
        scanner.nextLine();
    }
}
