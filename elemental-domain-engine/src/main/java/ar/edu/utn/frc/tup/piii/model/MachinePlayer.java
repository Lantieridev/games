package ar.edu.utn.frc.tup.piii.model;

import java.util.*;
import java.util.stream.Collectors;
import ar.edu.utn.frc.tup.piii.ConsoleUI;

public class MachinePlayer extends Player {

    /** Disabled in tests so a full simulated game doesn't take real seconds per AI action. */
    private static boolean thinkingDelayEnabled = true;

    public static void setThinkingDelayEnabled(boolean enabled) {
        thinkingDelayEnabled = enabled;
    }

    private static void think(long millis) {
        if (!thinkingDelayEnabled) return;
        try { Thread.sleep(millis); } catch (InterruptedException e) {}
    }

    public MachinePlayer(String name) {
        super(name);
    }

    @Override
    public void playTurn(Deck deck, List<Combination> table) {
        Card drawn = deck.draw();
        System.out.println("\n" + ConsoleUI.RED + ">>> TURNO DE LA IA: " + name + " <<<" + ConsoleUI.RESET);
        System.out.println(name + " está analizando la mesa...");
        think(1000);
        addCard(drawn);

        // IA Logic: Search for combinations
        findAndExtendCombinations(table);
        findAndPlayCombinations(table);
    }

    private void findAndExtendCombinations(List<Combination> table) {
        boolean cardAdded = true;
        while (cardAdded) {
            cardAdded = false;
            for (Combination combo : table) {
                for (Card card : new ArrayList<>(hand)) {
                    if (combo.canAdd(card)) {
                        System.out.println(ConsoleUI.YELLOW + name + " dice: \"He visto que puedo extender una " + combo.getName() + " en la mesa con mi " + card + ".\"" + ConsoleUI.RESET);
                        int before = combo.calculateScore();
                        combo.addCard(card);
                        hand.remove(card);
                        addScore(combo.calculateScore() - before);
                        cardAdded = true;
                        think(800);
                        break;
                    }
                }
                if (cardAdded) break;
            }
        }
    }

    private void findAndPlayCombinations(List<Combination> table) {
        boolean found = true;
        while (found) {
            found = false;
            
            // 1. Search for Reactive Chain (very rare, high priority)
            Combination chain = findReactiveChain();
            if (chain != null) {
                playCombo(chain, table, "He detectado una Cadena Reactiva (F-A-T-Ag), ¡doble puntaje!");
                found = true;
                continue;
            }

            // 2. Search for Triads (Value * 3)
            Combination triad = findTriad();
            if (triad != null) {
                playCombo(triad, table, "He formado una Tríada. Es eficiente porque triplica el valor de la carta.");
                found = true;
                continue;
            }

            // 3. Search for Sequences
            Combination sequence = findSequence();
            if (sequence != null) {
                playCombo(sequence, table, "He armado una Secuencia del mismo elemento.");
                found = true;
                continue;
            }
        }
    }

    private void playCombo(Combination combo, List<Combination> table, String reason) {
        System.out.println(ConsoleUI.YELLOW + name + " dice: \"" + reason + "\"" + ConsoleUI.RESET);
        System.out.println(name + " bajó: " + combo);
        table.add(combo);
        removeCards(combo.getCards());
        addScore(combo.calculateScore());
        think(1000);
    }

    private Combination findTriad() {
        Map<Integer, List<Card>> byValue = hand.stream().collect(Collectors.groupingBy(Card::value));
        for (var entry : byValue.entrySet()) {
            List<Card> cards = entry.getValue();
            if (cards.size() >= 3) {
                // Take 3 with different elements
                List<Card> uniqueElements = new ArrayList<>();
                Set<Element> seen = new HashSet<>();
                for (Card c : cards) {
                    if (seen.add(c.element())) uniqueElements.add(c);
                    if (uniqueElements.size() == 3) return new Triad(uniqueElements);
                }
            }
        }
        return null;
    }

    private Combination findSequence() {
        Map<Element, List<Card>> byElement = hand.stream().collect(Collectors.groupingBy(Card::element));
        for (var entry : byElement.entrySet()) {
            List<Card> cards = entry.getValue();
            cards.sort(Comparator.comparingInt(Card::value));
            
            List<Card> currentSeq = new ArrayList<>();
            for (int i = 0; i < cards.size(); i++) {
                if (currentSeq.isEmpty()) {
                    currentSeq.add(cards.get(i));
                } else if (cards.get(i).value() == currentSeq.get(currentSeq.size() - 1).value() + 1) {
                    currentSeq.add(cards.get(i));
                } else if (cards.get(i).value() != currentSeq.get(currentSeq.size() - 1).value()) {
                    if (currentSeq.size() >= 3) return new Sequence(new ArrayList<>(currentSeq));
                    currentSeq.clear();
                    currentSeq.add(cards.get(i));
                }
            }
            if (currentSeq.size() >= 3) return new Sequence(currentSeq);
        }
        return null;
    }

    private Combination findReactiveChain() {
        Card f = hand.stream().filter(c -> c.element() == Element.FUEGO).findFirst().orElse(null);
        Card a = hand.stream().filter(c -> c.element() == Element.AIRE).findFirst().orElse(null);
        Card t = hand.stream().filter(c -> c.element() == Element.TIERRA).findFirst().orElse(null);
        Card ag = hand.stream().filter(c -> c.element() == Element.AGUA).findFirst().orElse(null);
        
        if (f != null && a != null && t != null && ag != null) {
            return new ReactiveChain(Arrays.asList(f, a, t, ag));
        }
        return null;
    }

    @Override
    public Card discard() {
        if (hand.size() <= 7) return null;
        // Simple AI: Discard the lowest value card that isn't part of a near-combo
        Card low = hand.stream().min(Comparator.comparingInt(Card::value)).orElse(null);
        if (low != null) {
            System.out.println(name + " descarto: " + low + " porque tiene bajo valor y no me sirve.");
            hand.remove(low);
        }
        return low;
    }
}
