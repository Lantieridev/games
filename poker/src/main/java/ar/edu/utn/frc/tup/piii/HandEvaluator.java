package ar.edu.utn.frc.tup.piii;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class HandEvaluator {

    private static final List<Integer> ROYAL_FLUSH_VALUES = List.of(
            CardSymbol.ACE.ordinal(),
            CardSymbol.TEN.ordinal(),
            CardSymbol.JACK.ordinal(),
            CardSymbol.QUEEN.ordinal(),
            CardSymbol.KING.ordinal()
    );

    public static boolean isRoyalFlush(List<Card> cards) {
        Map<CardSuit, List<Card>> cardsBySuit = cards.stream()
                .collect(Collectors.groupingBy(Card::getCardSuit));

        for (Map.Entry<CardSuit, List<Card>> entry : cardsBySuit.entrySet()) {
            List<Card> suitedCards = entry.getValue();
            if (suitedCards.size() < 5) {
                continue;
            }

            List<Integer> values = suitedCards.stream()
                    .map(card -> card.getCardSymbol().ordinal())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            if (values.containsAll(ROYAL_FLUSH_VALUES)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isStraightFlush(List<Card> cards) {
        Map<CardSuit, List<Card>> cardsBySuit = cards.stream()
                .collect(Collectors.groupingBy(Card::getCardSuit));

        for (Map.Entry<CardSuit, List<Card>> entry : cardsBySuit.entrySet()) {
            List<Card> suitedCards = entry.getValue();
            if (suitedCards.size() < 5) {
                continue;
            }

            if (hasConsecutiveSequence(cards)) {
                return true;
            }
        }
        return false;
    }

    /**
     * This function check if the list of cards are all the same suit.
     * @param cards the list of cards to check.
     * @return true if the list of cards are all the same suit, false otherwise.
     */
    public static boolean isFlush(List<Card> cards) {
        if (cards.isEmpty()) {
            return false;
        }
        CardSuit firstSuit = cards.get(0).getCardSuit();
        return cards.stream().allMatch(card -> card.getCardSuit() == firstSuit);
    }

    public static boolean isStraight(List<Card> cards) {
        return hasConsecutiveSequence(cards);
    }

    public static int countPairs(List<Card> cards) {
        return countGroupsOfSameSymbol(cards, 2);
    }

    public static int countTrios(List<Card> cards) {
        return countGroupsOfSameSymbol(cards, 3);
    }

    public static boolean isPoker(List<Card> cards) {
        return countGroupsOfSameSymbol(cards, 4) == 1;
    }

    public static boolean isBetterHand(Map.Entry<List<Card>, PokerHandRank> hand1,
                                       Map.Entry<List<Card>, PokerHandRank> hand2) {
        int rankCompare = hand1.getValue().compareTo(hand2.getValue());
        if (rankCompare > 0) return true;
        if (rankCompare < 0) return false;

        PokerHandRank rank = hand1.getValue();
        List<Card> cards1 = hand1.getKey();
        List<Card> cards2 = hand2.getKey();

        switch (rank) {
            case ONE_PAIR:
                return comparePairs(cards1, cards2);
            case TWO_PAIR:
                return compareTwoPairs(cards1, cards2);
            case THREE_OF_A_KING:
                return compareTrio(cards1, cards2);
            case FULL_HOUSE:
                return compareFullHouse(cards1, cards2);
            case POKER:
                return comparePoker(cards1, cards2);
            default:
                // Para otras manos simplemente comparamos los valores de las cartas
                return compareHighCards(cards1, cards2);
        }
    }

    private static boolean compareHighCards(List<Card> cards1, List<Card> cards2) {
        List<Integer> values1 = getSortedCardValues(cards1);
        List<Integer> values2 = getSortedCardValues(cards2);

        for (int i = 0; i < Math.min(values1.size(), values2.size()); i++) {
            int compare = Integer.compare(values1.get(i), values2.get(i));
            if (compare != 0) {
                return compare > 0;
            }
        }
        return false;
    }

    private static boolean comparePairs(List<Card> cards1, List<Card> cards2) {
        int pairValue1 = findGroupValue(cards1, 2);
        int pairValue2 = findGroupValue(cards2, 2);

        if (pairValue1 != pairValue2) {
            return pairValue1 > pairValue2;
        }

        // Si los pares son iguales, comparamos las cartas sacando esos pares
        return compareRemainingCard(cards1, cards2, pairValue1);
    }

    private static boolean compareTwoPairs(List<Card> cards1, List<Card> cards2) {
        List<Integer> pairValues1 = findAllGroupValues(cards1, 2);
        List<Integer> pairValues2 = findAllGroupValues(cards2, 2);

        pairValues1.sort(Comparator.reverseOrder());
        pairValues2.sort(Comparator.reverseOrder());

        if (pairValues1.get(0) != pairValues2.get(0)) {
            return pairValues1.get(0) > pairValues2.get(0);
        }

        if (pairValues1.get(1) != pairValues2.get(1)) {
            return pairValues1.get(1) > pairValues2.get(1);
        }

        return compareRemainingCard(cards1, cards2, pairValues1.get(0), pairValues1.get(1));
    }

    private static boolean compareTrio(List<Card> cards1, List<Card> cards2) {
        int trioValue1 = findGroupValue(cards1, 3);
        int trioValue2 = findGroupValue(cards2, 3);

        if (trioValue1 != trioValue2) {
            return trioValue1 > trioValue2;
        }

        return compareRemainingCard(cards1, cards2, trioValue1);
    }

    private static boolean compareFullHouse(List<Card> cards1, List<Card> cards2) {
        int trioValue1 = findGroupValue(cards1, 3);
        int trioValue2 = findGroupValue(cards2, 3);

        if (trioValue1 != trioValue2) {
            return trioValue1 > trioValue2;
        }

        int pairValue1 = findGroupValue(cards1, 2);
        int pairValue2 = findGroupValue(cards2, 2);

        return pairValue1 > pairValue2;
    }

    private static boolean comparePoker(List<Card> cards1, List<Card> cards2) {
        int pokerValue1 = findGroupValue(cards1, 4);
        int pokerValue2 = findGroupValue(cards2, 4);

        if (pokerValue1 != pokerValue2) {
            return pokerValue1 > pokerValue2;
        }

        return compareRemainingCard(cards1, cards2, pokerValue1);
    }

    private static boolean compareRemainingCard(List<Card> cards1, List<Card> cards2, int... excludeValues) {
        // Filtrar las cartas que no son parte de los grupos, vienen ordenados de mayor a menor
        List<Integer> kickers1 = getRemainingCards(cards1, excludeValues);
        List<Integer> kickers2 = getRemainingCards(cards2, excludeValues);

        for (int i = 0; i < Math.min(kickers1.size(), kickers2.size()); i++) {
            int compare = Integer.compare(kickers1.get(i), kickers2.get(i));
            if (compare != 0) {
                return compare > 0;
            }
        }
        return false;
    }

    private static List<Integer> getRemainingCards(List<Card> cards, int... excludeValues) {
        Set<Integer> excludeSet = new HashSet<>();
        for (int value : excludeValues) {
            excludeSet.add(value);
        }

        return cards.stream()
                .map(card -> card.getCardSymbol().getValue())
                .filter(value -> !excludeSet.contains(value))
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    /**
     * This function search a value that are inside the cards X times.
     *
     * @param cards the list of cards to check.
     * @param groupSize the X times that the card has been repeated.
     * @return the value of the card
     */
    private static int findGroupValue(List<Card> cards, int groupSize) {
        Map<Integer, Long> valueCounts = cards.stream()
                .collect(Collectors.groupingBy(
                        card -> card.getCardSymbol().getValue(),
                        Collectors.counting()
                ));

        for (Map.Entry<Integer, Long> entry : valueCounts.entrySet()) {
            if (entry.getValue() == groupSize) {
                return entry.getKey();
            }
        }
        return -1;
    }

    private static List<Integer> findAllGroupValues(List<Card> cards, int groupSize) {
        Map<Integer, Long> valueCounts = cards.stream()
                .collect(Collectors.groupingBy(
                        card -> card.getCardSymbol().getValue(),
                        Collectors.counting()
                ));

        List<Integer> groupValues = new ArrayList<>();
        for (Map.Entry<Integer, Long> entry : valueCounts.entrySet()) {
            if (entry.getValue() == groupSize) {
                groupValues.add(entry.getKey());
            }
        }
        return groupValues;
    }

    private static List<Integer> getSortedCardValues(List<Card> cards) {
        return cards.stream()
                .map(card -> card.getCardSymbol().getValue())
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }

    /**
     * This function count the number of groups of the same symbol in the list of cards.
     *
     * @param cards the list of cards to check.
     * @param groupSize the size of the group to count.
     * @return the number of groups of the same symbol in the list of cards.
     */
    private static Integer countGroupsOfSameSymbol(List<Card> cards, int groupSize) {
        Map<CardSymbol, Long> counts = cards.stream()
                .collect(Collectors.groupingBy(Card::getCardSymbol, Collectors.counting()));
        return (int) counts.values().stream().filter(count -> count == (long) groupSize).count();
    }

    /**
     * This function check if the list of values has a consecutive sequence of 5 cards.
     *
     * @param cards the list of Cards to check.
     * @return true if the list has a consecutive sequence of 5 cards, false otherwise.
     */
    private static Boolean hasConsecutiveSequence(List<Card> cards) {
        Set<Integer> values = cards.stream()
                .map(card -> card.getCardSymbol().getValue())
                .collect(Collectors.toSet());

        // Normal straight or high straight (including Ace as 14)
        List<Integer> sortedValues = values.stream().sorted().collect(Collectors.toList());
        if (checkSequence(sortedValues)) return true;

        // Low straight: Ace (14) becomes 1
        if (values.contains(14)) {
            Set<Integer> lowValues = new HashSet<>(values);
            lowValues.remove(14);
            lowValues.add(1);
            List<Integer> sortedLowValues = lowValues.stream().sorted().collect(Collectors.toList());
            if (checkSequence(sortedLowValues)) return true;
        }

        return false;
    }

    private static boolean checkSequence(List<Integer> values) {
        if (values.size() < 5) return false;
        int consecutiveCount = 1;
        for (int i = 0; i < values.size() - 1; i++) {
            if (values.get(i + 1) == values.get(i) + 1) {
                consecutiveCount++;
                if (consecutiveCount >= 5) return true;
            } else {
                consecutiveCount = 1;
            }
        }
        return false;
    }
}
