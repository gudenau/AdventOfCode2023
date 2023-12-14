package net.gudenau.aoc2023;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.function.IntUnaryOperator;

public class Day13 {
    static class Region {
        final BitSet data = new BitSet();
        final int width;
        final int height;

        Region(List<Integer> data) {
            this.height = data.size();
            this.width = data.stream()
                .mapToInt(Integer::intValue)
                .map((value) -> 32 - Integer.numberOfLeadingZeros(value))
                .max()
                .getAsInt();

            for(int y = 0; y < height; y++) {
                var row = data.get(y);
                for(int x = 0; x < width; x++) {
                    if((row & (1 << x)) != 0) {
                        this.data.set(x + y * width);
                    }
                }
            }
        }

        boolean get(int x, int y) {
            return data.get(x + y * width);
        }

        int row(int y) {
            int data = 0;
            for(int x = 0; x < width; x++) {
                data |= get(x, y) ? 1 << x : 0;
            }
            return data;
        }

        int column(int x) {
            int data = 0;
            for(int y = 0; y < height; y++) {
                data |= get(x, y) ? 1 << y : 0;
            }
            return data;
        }
    }

    public static void main(String[] args) throws Throwable {
        List<Region> mirrorRegions;
        try(var stream = new BufferedReader(new InputStreamReader(Day13.class.getResourceAsStream("/input")))) {
            List<Integer> rawMirrors = stream.lines()
                .map((line) -> {
                    int mirrors = 0;
                    var chars = line.toCharArray();
                    for(int i = 0; i < chars.length; i++) {
                        if(chars[i] == '#') {
                            mirrors |= 1 << i;
                        }
                    }
                    return mirrors;
                })
                .toList();

            mirrorRegions = new ArrayList<>();
            var region = new ArrayList<Integer>();
            for(var row : rawMirrors) {
                if(row == 0) {
                    mirrorRegions.add(new Region(region));
                    region.clear();
                } else {
                    region.add(row);
                }
            }
            mirrorRegions.add(new Region(region));
        }

        List<Integer> vertical = new ArrayList<>();
        List<Integer> horizontal = new ArrayList<>();
        List<Integer> verticalFudge = new ArrayList<>();
        List<Integer> horizontalFudge = new ArrayList<>();

        for(var region : mirrorRegions) {
            vertical.addAll(findSplits(region::row, region.width, region.height, false));
            horizontal.addAll(findSplits(region::column, region.height, region.width, false));
            verticalFudge.addAll(findSplits(region::row, region.width, region.height, true));
            horizontalFudge.addAll(findSplits(region::column, region.height, region.width, true));
        }

        System.out.print("Part 1: ");
        System.out.println(
            vertical.stream().mapToInt(Integer::intValue).sum() +
            horizontal.stream().mapToInt(Integer::intValue).sum() * 100
        );

        System.out.print("Part 2: ");
        System.out.println(
            verticalFudge.stream().mapToInt(Integer::intValue).sum() +
                horizontalFudge.stream().mapToInt(Integer::intValue).sum() * 100
        );
    }

    private static List<Integer> findSplits(IntUnaryOperator extractor, int limitA, int limitB, boolean fudge) {
        int half = limitA / 2;
        List<Integer> splits = new ArrayList<>();
        for(int i = 1; i < limitA; i++) {
            int bits = i > half ? limitA - i : i;
            int mask = (-1 >>> (32 - bits));
            int incorrectBits = 0;
            for(int o = 0; o < limitB; o++) {
                var row = extractor.applyAsInt(o);
                var a = (row >> (i > half ? i - bits : 0)) & mask;
                var b = Integer.reverse(((row >> i) & mask) << (32 - bits));
                var diff = a ^ b;
                incorrectBits += Integer.bitCount(diff);
            }
            if(incorrectBits == (fudge ? 1 : 0)) {
                splits.add(i);
            }
        }
        return splits;
    }
}
