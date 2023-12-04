package net.gudenau.aoc2023;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class Day4 {
    static final class Card {
        private final int index;
        private final Set<Integer> winning;
        private final List<Integer> numbers;
        private int matches = 0;
        private long count = 1;

        Card(int index, Set<Integer> winning, List<Integer> numbers) {
            this.index = index;
            this.winning = winning;
            this.numbers = numbers;
        }

        public int index() {
            return index;
        }

        public long count() {
            return count;
        }
    }

    public static void main(String[] args) throws Throwable {
        try(var reader = new BufferedReader(new InputStreamReader(Day4.class.getResourceAsStream("/input")))) {
            var cards = reader.lines()
                .parallel()
                .filter(Predicate.not(String::isBlank))
                .map((line) -> {
                    var split = line.split(" +");
                    int id = Integer.parseInt(split[1].substring(0, split[1].length() - 1));
                    Set<Integer> winning = new HashSet<>();
                    List<Integer> numbers = new ArrayList<>();
                    Collection<Integer> current = winning;
                    for(int i = 2, length = split.length; i < length; i++) {
                        var s = split[i];
                        if(s.equals("|")) {
                            current = numbers;
                        } else {
                            current.add(Integer.parseInt(s));
                        }
                    }
                    return new Card(id, Collections.unmodifiableSet(winning), Collections.unmodifiableList(numbers));
                })
                .peek((card) -> {
                    int winners = 0;
                    for(var number : card.numbers) {
                        if(card.winning.contains(number)) {
                            winners++;
                        }
                    }
                    card.matches = winners;
                })
                .sorted(Comparator.comparingInt(Card::index))
                .toList();

            cards.forEach((card) -> {
                for(int i = card.index, limit = Math.min(cards.size(), i + card.matches); i < limit; i++) {
                    cards.get(i).count += card.count;
                }
            });

            System.out.println(cards.stream()
                .mapToLong(Card::count)
                .sum()
            );
        }
    }
}
