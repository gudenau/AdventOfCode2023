package net.gudenau.aoc2023;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day7 {
    enum Card {
        A('A'),
        K('K'),
        Q('Q'),
        T('T'),
        NINE('9'),
        EIGHT('8'),
        SEVEN('7'),
        SIX('6'),
        FIVE('5'),
        FOUR('4'),
        THREE('3'),
        TWO('2'),
        J('J'),
        ;

        private static final Map<Character, Card> CARDS = Stream.of(values())
            .collect(Collectors.toUnmodifiableMap(
                Card::character,
                Function.identity()
            ));

        private final char character;

        Card(char character) {
            this.character = character;
        }

        public char character() {
            return character;
        }

        public static Card valueOf(int character) {
            var card = CARDS.get((char) character);
            if(card == null) {
                throw new AssertionError();
            }
            return card;
        }
    }

    private static final BiFunction<Card, Integer, Integer> CARD_COUNTER = (card, value) -> value == null ? 1 : value + 1;

    enum HandType implements Predicate<Map<Card, Integer>> {
        FIVE_OF_A_KIND {
            @Override
            public boolean test(Map<Card, Integer> cards) {
                return cards.size() == 1;
            }
        },
        FOUR_OF_A_KIND {
            @Override
            public boolean test(Map<Card, Integer> cards) {
                return cards.values().stream()
                    .anyMatch((value) -> value == 4);
            }
        },
        FULL_HOUSE {
            @Override
            public boolean test(Map<Card, Integer> cards) {
                return cards.values().stream()
                    .allMatch((value) -> value == 2 || value == 3);
            }
        },
        THREE_OF_A_KIND {
            @Override
            public boolean test(Map<Card, Integer> cards) {
                return cards.values().stream()
                    .anyMatch((value) -> value == 3);
            }
        },
        TWO_PAIR {
            @Override
            public boolean test(Map<Card, Integer> cards) {
                int twos = 0;
                for(int value : cards.values()) {
                    if(value == 2) {
                        twos++;
                    }
                }
                return twos == 2;
            }
        },
        ONE_PAIR {
            @Override
            public boolean test(Map<Card, Integer> cards) {
                int twos = 0;
                for(int value : cards.values()) {
                    if(value == 2) {
                        twos++;
                    }
                }
                return twos >= 1;
            }
        },
        HIGH_CARD {
            @Override
            public boolean test(Map<Card, Integer> cards) {
                return true;
            }
        },
        ;

        public static HandType valueOf(List<Card> cards) {
            Map<Card, Integer> values = new HashMap<>();
            for(var card : cards) {
                values.compute(card, CARD_COUNTER);
            }
            int jokers = values.getOrDefault(Card.J, 0);
            if(jokers != 0 && values.size() > 1) {
                var best = values.entrySet().stream()
                    .filter((e) -> e.getKey() != Card.J)
                    .min((a, b) -> {
                        if(a.getValue().equals(b.getValue())) {
                            return Integer.compare(b.getKey().ordinal(), a.getKey().ordinal());
                        } else {
                            return Integer.compare(b.getValue(), a.getValue());
                        }
                    })
                    .orElseThrow();
                values.put(best.getKey(), best.getValue() + jokers);
                values.remove(Card.J);
            }

            for(var value : values()) {
                if(value.test(values)) {
                    return value;
                }
            }
            // Shouldn't be possible but no harm.
            return HIGH_CARD;
        }
    }

    record Hand(
        List<Card> cards,
        long bet,
        HandType type,
        long value
    ) implements Comparable<Hand> {
        Hand(List<Card> cards, long bet) {
            this(cards, bet, HandType.valueOf(cards));
        }

        public Hand(List<Card> cards, long bet, HandType handType) {
            this(cards, bet, handType, computeValue(handType, cards));
        }

        private static long computeValue(HandType type, List<Card> cards) {
            var value = ((long) type.ordinal()) << 50;
            for(int i = 0; i < 5; i++) {
                value |= ((long) cards.get(i).ordinal()) << (40 - i * 10);
            }
            return value;
        }

        @Override
        public int compareTo(Hand o) {
            return Long.compare(value, o.value);
        }

        @Override
        public String toString() {
            return "Hand{" +
                "cards=" + handString(cards) +
                ", bet=" + bet +
                ", type=" + type +
                ", value=" + "%016X".formatted(value) +
                '}';
        }

        private static String handString(List<Card> cards) {
            var builder = new StringBuilder();
            for(int i = 0; i < cards.size(); i++) {
                var card = cards.get(i);
                builder
                    .append("\u001B[")
                    .append(30 + card.ordinal())
                    .append("m")
                    .append(card.character)
                    .append("\u001B[0m")
                    ;
            }
            return builder.toString();
        }
    }

    public static void main(String[] args) throws Throwable {
        List<Hand> hands;
        try(var reader = new BufferedReader(new InputStreamReader(Day7.class.getResourceAsStream("/input")))) {
            hands = reader.lines()
                .filter(Predicate.not(String::isBlank))
                .map((line) -> {
                    var split = line.split(" ");
                    var cards = split[0].chars()
                        .mapToObj(Card::valueOf)
                        .toList();
                    var bet = Long.parseLong(split[1]);
                    return new Hand(cards, bet);
                })
                .sorted(Comparator.reverseOrder())
                .toList();
        }

        long total = 0;
        for(int i = 0; i < hands.size(); i++) {
            total += (i + 1) * hands.get(i).bet;
            System.out.printf("%s %d %d %d\n", hands.get(i), i + 1, hands.get(i).bet, (i + 1) * hands.get(i).bet);
        }
        System.out.println(total);
    }
}
