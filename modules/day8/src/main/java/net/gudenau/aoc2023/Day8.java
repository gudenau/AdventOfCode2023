package net.gudenau.aoc2023;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

// Yeah this is gross, I don't fully understand the math.
public class Day8 {
    enum Direction {
        LEFT('L'),
        RIGHT('R'),
        ;

        private static final Map<Character, Direction> VALUES = Stream.of(values())
            .collect(Collectors.toUnmodifiableMap(
                Direction::value,
                Function.identity()
            ));

        public static Direction valueOf(char value) {
            var direction = VALUES.get(value);
            if(direction == null) {
                throw new IllegalArgumentException();
            }
            return direction;
        }

        private final char value;

        Direction(char value) {
            this.value = value;
        }

        public char value() {
            return value;
        }
    }

    record Pair<L, R>(L left, R right) {
        public static <L, R> Pair<L, R> of(L left, R right) {
            return new Pair<>(left, right);
        }
    }

    static class Ghost {
        private final Map<String, Pair<String, String>> instructions;
        int loopPoint = -1;
        int index = -1;
        int loopEnd = -1;
        int z = -1;
        List<String> path = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        String location;
        int mode = 0;

        Ghost(Map<String, Pair<String, String>> instructions, String location) {
            this.instructions = instructions;
            this.location = location;
        }

        public boolean step(Direction direction) {
            switch(mode) {
                case 0 -> {
                    var instruction = instructions.get(location);
                    var next = direction == Direction.LEFT ? instruction.left : instruction.right;
                    if(!visited.add(next)) {
                        // We are looping!
                        loopPoint = path.indexOf(next);
                        loopEnd = path.size();
                        index = loopPoint + 1;
                        mode = 1;
                    } else {
                        path.add(next);
                    }
                    location = next;
                }
                case 1 -> {
                    location = path.get(index++);
                    if(index == path.size()) {
                        index = loopPoint;
                    }
                }
                // Stupid mode!
                case 2 -> {
                    var instruction = instructions.get(location);
                    location = direction == Direction.LEFT ? instruction.left : instruction.right;
                    return location.endsWith("Z");
                }
            }

            if(location.endsWith("Z") && z == -1) {
                z = path.size();
            }

            return mode == 1;
        }
    }

    public static void main(String[] args) throws Throwable {
        try(var reader = new BufferedReader(new InputStreamReader(Day8.class.getResourceAsStream("/input")))) {
            var lines = reader.lines()
                .toList();

            List<Direction> directions = new ArrayList<>();
            int i;
            for(i = 0; i < lines.size(); i++) {
                var line = lines.get(i);
                if(line.isBlank()) {
                    break;
                }
                for(char direction : line.toCharArray()) {
                    directions.add(Direction.valueOf(direction));
                }
            }

            while(lines.get(i).isBlank()) {
                i++;
            }

            Map<String, Pair<String, String>> instructions = new HashMap<>();
            var instructionPattern = Pattern.compile("^([A-Z\\d]{3}) = \\(([A-Z\\d]{3}), ([A-Z\\d]{3})\\)$");
            for(; i < lines.size(); i++) {
                var line = lines.get(i);
                var matcher = instructionPattern.matcher(line);
                if(!matcher.find()) {
                    break;
                }

                instructions.put(matcher.group(1), Pair.of(matcher.group(2), matcher.group(3)));
            }

            var ghosts = instructions.keySet().stream()
                .filter((location) -> location.endsWith("A"))
                .map((location) -> new Ghost(instructions, location))
                .toList();

            var steps = 0L;
            i = 0;

            /*
            long time = 0;
            while(true) {
                var direction = directions.get(i++);
                if(i >= directions.size()) {
                    i = 0;
                }

                var start = System.nanoTime();
                steps++;
                boolean stopping = true;
                for(var ghost : ghosts) {
                    stopping &= ghost.step(direction);
                }
                var end = System.nanoTime();
                time += end - start;

                if(stopping) {
                    break;
                }

                if((steps & 0xFFFFFF) == 0) {
                    System.out.println("Working (step " + steps + ") " + (time / steps));
                }
            }
             */

            var numbers = ghosts.stream().map((ghost) -> ghost.location)
                .mapToLong((location) -> {
                    long index = 0;
                    for(int o = 0; true; o++) {
                        if(location.endsWith("Z")) {
                            return index;
                        }
                        index++;

                        if(o == directions.size()) {
                            o = 0;
                        }

                        var direction = directions.get(o);
                        var instruction = instructions.get(location);
                        location = direction == Direction.LEFT ? instruction.left : instruction.right;
                    }
                })
                .toArray();

            System.out.println(steps);
            /*
            System.out.println(lcm(
                ghosts.stream()
                    .mapToLong((ghost) -> ghost.loopEnd)
                    .toArray()
            ));
            System.out.println(lcm2(
                ghosts.stream()
                    .mapToLong((ghost) -> ghost.loopEnd)
                    .toArray()
            ));
             */
            System.out.println(lcm(numbers));
            System.out.println(lcm2(numbers));
            System.out.println(12261332223L);

            /*
            paths.forEach((path) -> {
                path.forEach((location) -> {
                    System.out.print(location);
                    System.out.print(" ");
                });
                System.out.println();
            });
             */

            //System.out.println(steps);
        }
    }

    static long lcm2(long... numbers) {
        return lcm2(LongStream.of(numbers).mapToObj(BigInteger::valueOf).toArray(BigInteger[]::new)).longValueExact();
    }

    static BigInteger lcm2(BigInteger... numbers) {
        var result = numbers[0];
        for(int i = 1; i < numbers.length; i++) {
            result = lcm3(result, numbers[i]);
        }
        return result;
    }

    static BigInteger lcm3(BigInteger a, BigInteger b) {
        var gcd = a.gcd(b);
        var product = a.multiply(b).abs();
        return product.divide(gcd);
    }

    static long lcm(long ... numbers) {
        List<Map<Long, Long>> factors = LongStream.of(numbers)
            .mapToObj(Day8::getPrimeFactors)
            .toList();

        Set<Long> keys = factors.stream()
            .map(Map::keySet)
            .flatMap(Collection::stream)
            .collect(Collectors.toSet());

        long lcm = 1;
        for(long factor : keys) {
            long max = 0;
            for(var primes : factors) {
                max = Math.max(primes.getOrDefault(factor, 0L), max);
            }
            long value = factor;
            for(long i = 1; i < max; i++) {
                value *= factor;
            }
            lcm *= value;
        }
        return lcm;
    }

    static Map<Long, Long> getPrimeFactors(long value) {
        var factors = new HashMap<Long, Long>();
        for(long factor = 2; factor <= value; factor++) {
            while(value % factor == 0) {
                long power = factors.getOrDefault(factor, 0L);
                factors.put(factor, power + 1L);
                value /= factor;
            }
        }
        return factors;
    }
}
