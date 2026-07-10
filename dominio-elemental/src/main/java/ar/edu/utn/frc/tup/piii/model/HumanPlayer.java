package ar.edu.utn.frc.tup.piii.model;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import ar.edu.utn.frc.tup.piii.ConsoleUI;

public class HumanPlayer extends Player {
    private final Scanner scanner;
    private final String RESET = ConsoleUI.RESET;

    /**
     * Takes a shared Scanner rather than opening its own on System.in — two
     * independent Scanners wrapping the same stream silently lose input
     * (each buffers ahead independently), which also made this class
     * impossible to drive deterministically in tests.
     */
    public HumanPlayer(String name, Scanner scanner) {
        super(name);
        this.scanner = scanner;
    }

    @Override
    public void playTurn(Deck deck, List<Combination> table) {
        Card drawn = deck.draw();
        System.out.println("\n" + ConsoleUI.YELLOW + ">>> HAS ROBADO: " + drawn + RESET + "<<<");
        addCard(drawn);

        boolean finished = false;
        while (!finished) {
            ConsoleUI.renderTable(table);
            System.out.println(ConsoleUI.WHITE_BOLD + "\nTU MANO ACTUAL:" + RESET);
            ConsoleUI.renderHand(hand);
            
            System.out.println("\n" + ConsoleUI.CYAN + "¿Qué deseas hacer?" + RESET);
            System.out.println("1. ➕ Crear Nueva Combinación");
            System.out.println("2. 🔍 Extender Combinación (Mesa)");
            System.out.println("0. ⏭️  Terminar Turno");
            
            System.out.print(ConsoleUI.CYAN + "> " + RESET);
            int choice = readInt();
            if (choice == 1) {
                createCombination(table);
            } else if (choice == 2) {
                extendCombination(table);
            } else if (choice == 0) {
                finished = true;
            } else {
                System.out.println("Opcion invalida.");
            }
        }
    }

    /** Reads an int from the shared scanner without crashing the game on bad input. */
    private int readInt() {
        try {
            return scanner.nextInt();
        } catch (InputMismatchException e) {
            scanner.next(); // discard the invalid token
            return -1;
        }
    }

    private void createCombination(List<Combination> table) {
        System.out.println("Selecciona las cartas por indice (ej: 0,1,2):");
        System.out.println(formatHandWithIndices());
        scanner.nextLine(); // Clear buffer
        String input = scanner.nextLine();
        try {
            String[] indices = input.split(",");
            List<Card> selected = new ArrayList<>();
            for (String idx : indices) {
                selected.add(hand.get(Integer.parseInt(idx.trim())));
            }
            
            Combination combo = validateAndCreate(selected);
            if (combo != null) {
                table.add(combo);
                removeCards(selected);
                addScore(combo.calculateScore());
                System.out.println("¡Combinacion aceptada! +" + combo.calculateScore() + " puntos.");
            } else {
                System.out.println("Combinacion invalida.");
            }
        } catch (Exception e) {
            System.out.println("Error al seleccionar cartas.");
        }
    }

    private Combination validateAndCreate(List<Card> selected) {
        if (selected.size() < 3) return null;

        // Check Triad
        boolean allSameValue = selected.stream().allMatch(c -> c.value() == selected.get(0).value());
        boolean allDiffElements = selected.stream().map(Card::element).distinct().count() == selected.size();
        if (allSameValue && allDiffElements) return new Triad(selected);

        // Check Sequence
        Element e = selected.get(0).element();
        boolean sameElement = selected.stream().allMatch(c -> c.element() == e);
        if (sameElement) {
            List<Integer> vals = selected.stream().map(Card::value).sorted().collect(Collectors.toList());
            boolean consecutive = true;
            for (int i = 0; i < vals.size() - 1; i++) {
                if (vals.get(i + 1) != vals.get(i) + 1) consecutive = false;
            }
            if (consecutive) return new Sequence(selected);
        }

        // Check Reactive Chain (Order: F, A, T, Ag)
        if (selected.size() == 4) {
            boolean isChain = selected.get(0).element() == Element.FUEGO &&
                              selected.get(1).element() == Element.AIRE &&
                              selected.get(2).element() == Element.TIERRA &&
                              selected.get(3).element() == Element.AGUA;
            if (isChain) return new ReactiveChain(selected);
        }

        return null;
    }

    @Override
    public Card discard() {
        if (hand.size() <= 7) return null;
        System.out.println("Debes descartar. Tienes " + hand.size() + " cartas.");
        System.out.println(formatHandWithIndices());
        System.out.print("Indice a descartar: ");
        int idx = readInt();
        if (idx < 0 || idx >= hand.size()) {
            System.out.println("Indice invalido. No se descarto ninguna carta.");
            return null;
        }
        return hand.remove(idx);
    }

    private void extendCombination(List<Combination> table) {
        if (table.isEmpty()) {
            System.out.println("No hay combinaciones en la mesa.");
            return;
        }
        System.out.println("Selecciona la combinacion de la mesa por indice:");
        for (int i = 0; i < table.size(); i++) {
            System.out.println(i + ": " + table.get(i));
        }
        int tableIdx = readInt();
        if (tableIdx < 0 || tableIdx >= table.size()) {
            System.out.println("Indice de combinacion invalido.");
            return;
        }

        System.out.println("Selecciona la carta de tu mano por indice:");
        System.out.println(formatHandWithIndices());
        int handIdx = readInt();
        if (handIdx < 0 || handIdx >= hand.size()) {
            System.out.println("Indice de carta invalido.");
            return;
        }

        Card card = hand.get(handIdx);
        Combination combo = table.get(tableIdx);
        if (combo.canAdd(card)) {
            // Score the actual point delta the combination gains, not a flat
            // card value — a Triad's score is value*3 regardless of card
            // count, so extending one shouldn't hand out bonus points a
            // Sequence extension would legitimately earn.
            int before = combo.calculateScore();
            combo.addCard(card);
            hand.remove(handIdx);
            int gained = combo.calculateScore() - before;
            addScore(gained);
            System.out.println("¡Carta agregada! +" + gained + " puntos.");
        } else {
            System.out.println("Esa carta no encaja en esa combinacion.");
        }
    }

    private String formatHandWithIndices() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hand.size(); i++) {
            sb.append(i).append(": ").append(hand.get(i)).append("  ");
        }
        return sb.toString();
    }
}
