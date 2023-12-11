package net.gudenau.aoc2023;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day11 {
    private static final long SCALE = 1000000;

    enum Mark {
        GALAXY('#', false),
        EMPTY('.', true),
        EXPANSION('+', true),
        ;

        static final Map<Integer, Mark> MARKS = Stream.of(values()).collect(Collectors.toUnmodifiableMap(
            Mark::character,
            Function.identity()
        ));

        static Mark valueOf(int character) {
            return Objects.requireNonNull(MARKS.get(character));
        }

        final int character;
        final boolean empty;

        Mark(int character, boolean empty) {
            this.character = character;
            this.empty = empty;
        }

        private int character() {
            return character;
        }
    }

    record Galaxy(long x, long y) {}

    public static void main(String[] args) throws Throwable {
        Mark[][] map;
        try(var reader = new BufferedReader(new InputStreamReader(Day11.class.getResourceAsStream("/input")))) {
            map = reader.lines()
                .filter(Predicate.not(String::isBlank))
                .map((line) -> line.chars()
                    .mapToObj(Mark::valueOf)
                    .toArray(Mark[]::new)
                )
                .toArray(Mark[][]::new);
        }
        final int height = map.length;
        final int width = map[0].length;
        Stream.of(map)
            .filter((row) -> row.length != width)
            .findAny()
            .ifPresent((row) -> { throw new IllegalArgumentException(); });

        { // Expand the columns
            outer:
            for(int x = 0; x < width; x++) {
                for(var row : map) {
                    if(!row[x].empty) {
                        continue outer;
                    }
                }
                for(int y = 0; y < height; y++) {
                    map[y][x] = Mark.EXPANSION;
                }
            }
        }
        { // Expand rows
            for(var row : map) {
                boolean emptyRow = true;
                for(int x = 0; x < width && emptyRow; x++) {
                    emptyRow &= row[x].empty;
                }
                if(emptyRow) {
                    Arrays.fill(row, Mark.EXPANSION);
                }
            }
        }
        List<Galaxy> galaxies = new ArrayList<>();
        { // Find galaxies
            // We can make some assumptions here because the expansions are always entire rows and columns.
            long y = 0;
            for(var row : map) {
                if(row[0] == Mark.EXPANSION) {
                    y += SCALE - 1;
                }
                long x = 0;
                for(var mark : row) {
                    switch(mark) {
                        case EXPANSION -> x += SCALE - 1;
                        case GALAXY -> galaxies.add(new Galaxy(x, y));
                    }
                    x++;
                }
                y++;
            }
        }

        long distance = 0;
        {// Find distance
            for(int i = 0; i < galaxies.size(); i++) {
                var a = galaxies.get(i);
                for(int o = i + 1; o < galaxies.size(); o++) {
                    var b = galaxies.get(o);
                    distance += manhattanDistance(a, b);
                }
            }
        }

        for(var row : map) {
            for(var mark : row) {
                System.out.print((char) mark.character);
            }
            System.out.println();
        }
        System.out.println(galaxies.size() + " galaxies");
        System.out.println("Distance sum: " + distance);
    }

    private static long manhattanDistance(Galaxy a, Galaxy b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
}
