package net.gudenau.aoc2023;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class Day6 {
    public static void main(String[] args) throws Throwable {
        Map<String, List<Long>> data;
        try(var reader = new BufferedReader(new InputStreamReader(Day6.class.getResourceAsStream("/input")))) {
            data = reader.lines()
                .filter(Predicate.not(String::isBlank))
                .map((line) -> line.split(": *"))
                .collect(Collectors.toMap(
                    (split) -> split[0],
                    (split) -> List.of(Long.parseLong(String.join("", split[1].split(" +"))))
                ));
        }

        var times = data.get("Time");
        var records = data.get("Distance");

        record Range(long start, long end){
            long count() {
                return end - start + 1;
            }
        }
        var ranges = new ArrayList<Range>(times.size());

        for(int i = 0; i < times.size(); i++) {
            var time = times.get(i);
            var record = records.get(i);
            var bestTime = time / 2;
            var bestPossible = calcDistance(time, time / 2);

            var min = bestTime;
            while(calcDistance(time, min - 1) > record) {
                min--;
            }

            var max = bestTime;
            while(calcDistance(time, max + 1) > record) {
                max++;
            }

            ranges.add(new Range(min, max));
        }

        var total = new AtomicLong(1);
        ranges.stream()
            .mapToLong(Range::count)
            .forEach((value) -> {
                total.set(total.get() * value);
            });
        System.out.println(total);
    }

    private static void graph(int ms) {
        var trials = new int[ms + 1];
        for(int i = 0; i <= ms; i++) {
            trials[i] = (int) calcDistance(ms, i);
        }
        var max = IntStream.of(trials)
            .max()
            .orElse(0);

        System.out.print("   ");
        for(int x = 0; x <= ms; x++) {
            System.out.print(x);
        }
        System.out.println();

        for(int y = max; y >= 0; y--) {
            System.out.printf("%2d ", y);
            for(int x = 0; x <= ms; x++) {
                System.out.print(trials[x] == y ? "X" : " ");
            }
            System.out.println();
        }
    }

    static long calcDistance(long totalTime, long holdTime) {
        return (totalTime - holdTime) * holdTime;
    }
}
