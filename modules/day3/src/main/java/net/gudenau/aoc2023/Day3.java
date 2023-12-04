package net.gudenau.aoc2023;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class Day3 {
    public static void main(String[] args) throws Throwable {
        try(var reader = new BufferedReader(new InputStreamReader(Day3.class.getResourceAsStream("/input")))) {
            byte[][] data = reader.lines()
                .filter(Predicate.not(String::isBlank))
                .map((line) -> line.getBytes(StandardCharsets.UTF_8))
                .toArray(byte[][]::new);

            Map<Long, List<Integer>> gearCandidates = new HashMap<>();

            var total = IntStream.range(0, data.length)
                //.parallel()
                .map((x) -> {
                    var data2 = data[x];
                    int sum = 0;
                    for(int y = 0, length = data2.length; y < length; y++) {
                        if(!Character.isDigit(data2[y])) {
                            continue;
                        }

                        int start = y;
                        int end = length;
                        while(y < length) {
                            if(!Character.isDigit(data2[y++])) {
                                end = --y;
                                break;
                            }
                        }
                        int value = Integer.parseInt(new String(data2, start, end - start));

                        int startX = Math.max(0, x - 1);
                        int startY = Math.max(0, start - 1);
                        int endX = Math.min(data.length, x + 2);
                        int endY = Math.min(length, end + 1);

                        boolean symbol = false;
                        for(int y2 = startY; y2 < endY; y2++) {
                            for(int x2 = startX; x2 < endX; x2++) {
                                var current = data[x2][y2];
                                if(current != '.' && !Character.isDigit(current)) {
                                    symbol = true;
                                }
                                if(current == '*') {
                                    gearCandidates.computeIfAbsent(x2 | ((long) y2 << 32), (ignored) -> new ArrayList<>()).add(value);
                                }
                            }
                        }

                        if(symbol) {
                            sum += value;
                        }
                    }
                    return sum;
                })
                .sum();

            System.out.println(total);

            System.out.println(gearCandidates.values().stream()
                .filter((list) -> list.size() == 2)
                .mapToInt((list) -> list.get(0) * list.get(1))
                .sum()
            );
        }
    }
}
