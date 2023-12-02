package net.gudenau.aoc2023;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class Day2 {
    private static final Map<String, Integer> CUBES = Map.of(
        "red", 12,
        "green", 13,
        "blue", 14
    );

    record Game(int index, Map<String, Integer> maxes, List<Map<String, Integer>> rounds) {
        boolean possible() {
            if(!CUBES.keySet().containsAll(maxes.keySet())) {
                return false;
            }
            return maxes.entrySet().stream()
                .allMatch((entry) -> CUBES.get(entry.getKey()) >= entry.getValue());
        }
    }

    public static void main(String[] args) throws Throwable {
        try(var reader = new BufferedReader(new InputStreamReader(Day2.class.getResourceAsStream("/input")))) {
            var sum = reader.lines()
                .filter(Predicate.not(String::isBlank))
                .map((line) -> {
                    var split = line.split(":");
                    var game = split[0].trim();
                    var gameNumber = Integer.parseInt(game.substring(game.lastIndexOf(' ') + 1));
                    var input = split[1].trim();
                    Map<String, Integer> maxes = new HashMap<>(3);
                    List<Map<String, Integer>> rounds = new ArrayList<>();
                    for(var round : input.split(";")) {
                        Map<String, Integer> cubes = new HashMap<>(3);
                        for(var cubeSplit : round.split(",")) {
                            var asdf = cubeSplit.trim().split(" ");
                            cubes.put(asdf[1], Integer.parseInt(asdf[0]));
                        }
                        rounds.add(cubes);
                        cubes.forEach((key, value) -> maxes.compute(key, (k, current) -> current == null ? value : Math.max(current, value)));
                    }
                    return new Game(gameNumber, maxes, rounds);
                })
                //.filter(Game::possible)
                //.mapToInt(Game::index)
                .mapToInt((game) -> {
                    int total = 1;
                    for(int value : game.maxes.values()) {
                        total *= value;
                    }
                    return total;
                })
                .sum();
            System.out.println(sum);
        }
    }
}
