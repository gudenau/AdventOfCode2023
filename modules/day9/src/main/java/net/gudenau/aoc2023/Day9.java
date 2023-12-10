package net.gudenau.aoc2023;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Day9 {
    public static void main(String[] args) throws Throwable {
        try(var reader = new BufferedReader(new InputStreamReader(Day9.class.getResourceAsStream("/input")))) {
            var result = reader.lines()
                .parallel()
                .filter(Predicate.not(String::isBlank))
                .map((line) -> Stream.of(line.split(" +"))
                    .mapToInt(Integer::parseInt)
                    .toArray()
                )
                .mapToInt(Day9::processData)
                .sum();
            System.out.println(result);
        }
    }

    private static int processData(int[] data) {
        List<int[]> dataSet = new ArrayList<>();
        dataSet.add(data);

        boolean isNonZero;
        do {
            isNonZero = false;
            var current = dataSet.get(dataSet.size() - 1);
            var next = new int[current.length - 1];

            for(int i = 0; i < next.length; i++) {
                var value = current[i + 1] - current[i];
                next[i] = value;
                if(value != 0) {
                    isNonZero = true;
                }
            }

            dataSet.add(next);
        } while(isNonZero);

        int value = 0;
        for(int i = dataSet.size() - 1; i >= 0; i--) {
            var ints = dataSet.get(i);
            value = ints[0] - value;
        }

        return value;
    }
}
