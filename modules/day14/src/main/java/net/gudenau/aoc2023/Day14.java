package net.gudenau.aoc2023;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day14 {
    enum Rock {
        ROUND('O'),
        SQUARE('#'),
        EMPTY('.'),
        ;

        static final Map<Integer, Rock> VALUES = Stream.of(values()).collect(Collectors.toUnmodifiableMap(
            Rock::character,
            Function.identity()
        ));

        static Rock valueOf(int character) {
            return VALUES.getOrDefault(character, EMPTY);
        }

        final int character;

        Rock(int character) {
            this.character = character;
        }

        public int character() {
            return character;
        }
    }

    enum Direction {
        NORTH,
        WEST,
        SOUTH,
        EAST,
    }

    static class Platform {
        final int width;
        final int height;
        final Rock[] rocks;

        public Platform(List<List<Rock>> lists) {
            height = lists.size();
            width = lists.getFirst().size();
            if(lists.stream().anyMatch((list) -> list.size() != width)) {
                throw new RuntimeException();
            }
            rocks = lists.stream()
                .flatMap(Collection::stream)
                .toArray(Rock[]::new);
        }

        static Platform of(Stream<String> lines) {
            return lines.map((line) -> line.chars()
                    .mapToObj(Rock::valueOf)
                    .toList()
                )
                .collect(Collector.<List<Rock>, List<List<Rock>>, Platform>of(
                    ArrayList::new,
                    List::add,
                    (a, b) -> {
                        List<List<Rock>> q = new ArrayList<>(a.size() + b.size());
                        q.addAll(a);
                        q.addAll(b);
                        return q;
                    },
                    Platform::new
                ));
        }

        Rock rock(int x, int y) {
            return rocks[x + y * width];
        }

        void rock(int x, int y, Rock rock) {
            rocks[x + y * width] = rock;
        }

        void fillColumn(int x, int start, int end, Rock rock) {
            if(start > end) {
                int tmp = start;
                start = end;
                end = tmp;
            }

            for(int y = start; y < end; y++) {
                rock(x, y, rock);
            }
        }

        void fillRow(int y, int start, int end, Rock rock) {
            if(start > end) {
                int tmp = start;
                start = end;
                end = tmp;
            }

            int offset = y * width;
            Arrays.fill(rocks, start + offset, end + offset, rock);
        }

        // This is probably the worst possible way to do this.
        void shift(Direction direction) {
            switch(direction) {
                case NORTH -> shiftNorth();
                case WEST -> shiftWest();
                case SOUTH -> shiftSouth();
                case EAST -> shiftEast();
            }
        }

        void shiftNorth() {
            for(int x = 0; x < width; x++) {
                int stopper = 0;
                int count = 0;
                for(int y = 0; y < height; y++) {
                    var rock = rock(x, y);
                    if(rock == Rock.EMPTY) {
                        continue;
                    } else if(rock == Rock.ROUND) {
                        rock(x, y, Rock.EMPTY);
                        count++;
                    } else {
                        fillColumn(x, stopper, stopper + count, Rock.ROUND);
                        count = 0;
                        stopper = y + 1;
                    }
                }
                fillColumn(x, stopper, stopper + count, Rock.ROUND);
            }
        }

        void shiftSouth() {
            for(int x = 0; x < width; x++) {
                int stopper = height;
                int count = 0;
                for(int y = height - 1; y >= 0; y--) {
                    var rock = rock(x, y);
                    if(rock == Rock.EMPTY) {
                        continue;
                    } else if(rock == Rock.ROUND) {
                        rock(x, y, Rock.EMPTY);
                        count++;
                    } else {
                        fillColumn(x, stopper, stopper - count, Rock.ROUND);
                        count = 0;
                        stopper = y;
                    }
                }
                fillColumn(x, stopper, stopper - count, Rock.ROUND);
            }
        }

        void shiftEast() {
            for(int y = 0; y < height; y++) {
                int stopper = width;
                int count = 0;
                int yOff = y * width;
                for(int x = width - 1; x >= 0; x--) {
                    var rock = rocks[x + yOff];
                    if(rock == Rock.EMPTY) {
                        continue;
                    } else if(rock == Rock.ROUND) {
                        rocks[x + yOff] = Rock.EMPTY;
                        count++;
                    } else {
                        fillRow(y, stopper, stopper - count, Rock.ROUND);
                        count = 0;
                        stopper = x;
                    }
                }
                fillRow(y, stopper, stopper - count, Rock.ROUND);
            }
        }

        void shiftWest() {
            for(int y = 0; y < height; y++) {
                int stopper = 0;
                int count = 0;
                int yOff = y * width;
                for(int x = 0; x < width; x++) {
                    var rock = rocks[x + yOff];
                    if(rock == Rock.EMPTY) {
                        continue;
                    } else if(rock == Rock.ROUND) {
                        rocks[x + yOff] = Rock.EMPTY;
                        count++;
                    } else {
                        fillRow(y, stopper, stopper + count, Rock.ROUND);
                        count = 0;
                        stopper = x + 1;
                    }
                }
                fillRow(y, stopper, stopper + count, Rock.ROUND);
            }
        }

        @Override
        public String toString() {
            var builder = new StringBuilder((width + 1) * height);
            for(int y = 0; y < height; y++) {
                for(int x = 0; x < width; x++) {
                    builder.append((char) rock(x, y).character);
                }
                builder.append('\n');
            }
            return builder.toString();
        }

        public long weigh() {
            long weight = 0;
            for(int y = 0; y < height; y++) {
                long rocks = 0;
                for(int x = 0; x < width; x++) {
                    if(rock(x, y) == Rock.ROUND) {
                        rocks++;
                    }
                }
                weight += rocks * (height - y);
            }
            return weight;
        }
    }

    public static void main(String[] args) throws Throwable {
        Platform platform;
        try(var stream = new BufferedReader(new InputStreamReader(Day14.class.getResourceAsStream("/input")))) {
            platform = Platform.of(stream.lines());
        }

        System.out.println(platform);

        final int cycles = 1000000000;
        final long start = System.currentTimeMillis();

        var window = new ArrayList<String>();

        int cycleStart = -1;
        int i = 0;
        for(; i < cycles; i++) {
            if(i % 50000 == 0) {
                var percentage = (double) i / cycles;
                long time = System.currentTimeMillis();
                long elapsed = time - start;
                long eta = (long) (elapsed / percentage);
                System.out.println("Cycle " + i + " of " + cycles + ": " + (((int)(percentage * 10000)) / 100.0) + "%, ETA: " + (eta / 1000));
            }
            for(var direction : Direction.values()) {
                platform.shift(direction);
            }
            var string = platform.toString();
            cycleStart = window.indexOf(string);
            if(cycleStart != -1) {
                break;
            }
            window.add(string);
        }
        if(cycleStart != -1) {
            int cycle = cycleStart;
            int cycleEnd = window.size();
            i++;
            for(; i < cycles; i++) {
                cycle++;
                if(cycle == cycleEnd) {
                    cycle = cycleStart;
                }
            }
            platform = Platform.of(window.get(cycle).lines());
        }
        System.out.println(platform.weigh());
    }
}
