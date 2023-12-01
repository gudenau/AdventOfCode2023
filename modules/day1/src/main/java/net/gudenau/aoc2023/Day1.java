package net.gudenau.aoc2023;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Day1 {
    private static final Map<String, Integer> VALUES = Map.ofEntries(
        Map.entry("one", 1),
        Map.entry("two", 2),
        Map.entry("three", 3),
        Map.entry("four", 4),
        Map.entry("five", 5),
        Map.entry("six", 6),
        Map.entry("seven", 7),
        Map.entry("eight", 8),
        Map.entry("nine", 9),
        Map.entry("1", 1),
        Map.entry("2", 2),
        Map.entry("3", 3),
        Map.entry("4", 4),
        Map.entry("5", 5),
        Map.entry("6", 6),
        Map.entry("7", 7),
        Map.entry("8", 8),
        Map.entry("9", 9)
    );
    private static final Set<Character> STARTERS = VALUES.keySet().stream()
        .map((value) -> value.charAt(0))
        .collect(Collectors.toUnmodifiableSet());
    private static final int MAX_LENGTH = VALUES.keySet().stream()
        .mapToInt(String::length)
        .max()
        .getAsInt();

    public static void main(String[] args) throws Throwable {
        try(var reader = new BufferedReader(new InputStreamReader(Day1.class.getResourceAsStream("/input")))) {
            var total = reader.lines()
                .map((line) -> {
                    var numbers = new ArrayList<Integer>();
                    for(int i = 0, length = line.length(); i < length; i++) {
                        if(!STARTERS.contains(line.charAt(i))) {
                            continue;
                        }
                        for(int o = i + 1, end = Math.min(i + MAX_LENGTH, line.length()); o <= end; o++) {
                            var sub = line.substring(i, o);
                            var replacement = VALUES.get(sub);
                            if(replacement == null) {
                                continue;
                            }
                            numbers.add(replacement);
                            break;
                        }
                    }
                    return numbers;
                })
                .filter(Predicate.not(List::isEmpty))
                .mapToInt((list) -> list.get(0) * 10 + list.get(list.size() - 1))
                .sum();
            System.out.println(total);
        }
    }
}
