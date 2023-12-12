package net.gudenau.aoc2023;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day12 {
    public static void main(String[] args) throws Throwable {
        try(var stream = new BufferedReader(new InputStreamReader(Day12.class.getResourceAsStream("/input")))) {
            var result = stream.lines()
                .parallel()
                .filter(Predicate.not(String::isBlank))
                .mapToLong(Day12::permutations)
                .sum();
            System.out.println(result);
        }
    }

    enum Status {
        BROKEN('#'),
        UNKNOWN('?'),
        FUNCTIONAL('.'),
        ;

        static final Map<Integer, Status> VALUES = Stream.of(values()).collect(Collectors.toUnmodifiableMap(
            (status) -> status.character,
            Function.identity()
        ));

        static Status valueOf(int value) {
            return Objects.requireNonNull(VALUES.get(value));
        }

        final int character;

        Status(int character) {
            this.character = character;
        }
    }

    static final int DUPES = 5;

    static String duplicate(String input, char separator) {
        var value = (input + separator).repeat(DUPES);
        return value.substring(0, value.length() - 1);
    }

    static long permutations(String line) {
        var split = line.split(" ");

        var data = duplicate(split[0], '?');
        var parityData = duplicate(split[1], ',');

        var parity = Stream.of(parityData.split(","))
            .map(Integer::parseInt)
            .toList();

        var groups = new int[parity.size() * 2 + 1];
        Arrays.fill(groups, 1, groups.length - 1, 1);
        int groupSum = parity.size() - 1;
        for(int i = 0; i < parity.size(); i++) {
            int parityValue = parity.get(i);
            groups[i * 2 + 1] = parityValue;
            groupSum += parityValue;
        }

        Map<Key, Long> cache = new HashMap<>();
        return calculateGroups(cache, data.length() - groupSum, 0, groups, data);

        /* This only works for part 1.
        var split = line.split(" ");

        Status[] statuses = split[0].chars()
                .mapToObj(Status::valueOf)
                .toArray(Status[]::new);

        List<Integer> parity = Stream.of(split[1].split(","))
                .map(Integer::parseInt)
                .toList();

        int[] unknown;
        { // Find unknown spots
            int unknownCount = (int) Stream.of(statuses)
                .filter(Predicate.isEqual(Status.UNKNOWN))
                .count();
            unknown = new int[unknownCount];
            int unknownIndex = 0;
            for(int i = 0; i < statuses.length && unknownIndex < unknownCount; i++) {
                if(statuses[i] == Status.UNKNOWN) {
                    unknown[unknownIndex++] = i;
                }
            }
        }
        if(unknown.length == 0) {
            return 0;
        }

        // Brute force, fingers crossed this finishes within a reasonable time frame.
        {
            int permutations = 1;
            for(int i = 0; i < unknown.length; i++) {
                permutations *= 2;
                if(permutations < 1) {
                    throw new RuntimeException(line);
                }
            }

            long total = 0;
            for(int permutation = 0; permutation < permutations; permutation++) {
                for(int bit = 0; bit < unknown.length; bit++) {
                    var position = unknown[bit];
                    var status = (permutation & (1 << bit)) != 0 ?
                        Status.FUNCTIONAL :
                        Status.BROKEN;
                    statuses[position] = status;
                }
                if(validate(statuses, parity)) {
                    total++;
                }
            }
            return total;
        }
         */
    }

    record Key(int offset, int count) {}

    // Part two method
    static long calculateGroups(Map<Key, Long> cache, int emptyCount, int offset, int[] groups, String data) {
        if(emptyCount == 0) {
            return validate(groups.length, groups, data) ? 1 : 0;
        } else if(offset > groups.length) {
            return 0;
        }

        long count = 0;
        for(int i = 0; i <= emptyCount; i++) {
            groups[offset] += i;

            if(validate(offset + 1, groups, data)) {
                var key = new Key(offset + 2, emptyCount - i);
                var value = cache.get(key);
                if(value == null) {
                    value = calculateGroups(cache, key.count(), key.offset(), groups, data);
                    cache.put(key, value);
                }
                count += value;
            }

            groups[offset] -= i;
        }
        return count;
    }

    static boolean validate(int offset, int[] groups, String data) {
        var result = new StringBuilder();
        for(int i = 0; i < offset; i++) {
            var value = (i % 2 == 0) ? "." : "#";
            result.append(value.repeat(groups[i]));
        }
        for(int i = 0; i < result.length(); i++) {
            if(data.charAt(i) != '?' && result.charAt(i) != data.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    // Part one method
    static boolean validate(Status[] statuses, List<Integer> parity) {
        List<Integer> calculatedParity = new ArrayList<>(parity.size());

        int broken = 0;
        for(var status : statuses) {
            if(status == Status.BROKEN) {
                broken++;
            } else if(broken != 0) {
                calculatedParity.add(broken);
                broken = 0;
            }
        }
        if(broken != 0) {
            calculatedParity.add(broken);
        }

        return calculatedParity.equals(parity);
    }
}
