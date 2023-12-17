package net.gudenau.aoc2023;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

public class Day15 {
    static byte hash(String string) {
        byte hash = 0;
        for(var datum : string.getBytes(StandardCharsets.US_ASCII)) {
            hash += datum;
            hash *= 17;
        }
        return hash;
    }

    record Input(byte hash, Lens value) {
        boolean add() {
            return value.focal != -1;
        }

        public static Input of(String value) {
            int index = value.indexOf('=');
            String label;
            int lens;
            if(index == -1) {
                label = value.substring(0, value.length() - 1);
                lens = -1;
            } else {
                label = value.substring(0, index);
                lens = Integer.parseInt(value, index + 1, value.length(), 10);
            }
            return new Input(Day15.hash(label), new Lens(label, lens));
        }
    }

    record Lens(String label, int focal) {}

    public static void main(String[] args) throws Throwable {
        List<Input> inputs;
        try(var stream = new BufferedReader(new InputStreamReader(Day15.class.getResourceAsStream("/input")))) {
            var builder = new StringBuilder();
            inputs = Stream.concat(stream.lines(), Stream.of(","))
                .flatMapToInt(String::chars)
                .mapToObj(Character::toString)
                .<String>mapMulti((segment, consumer) -> {
                    if(segment.equals(",")) {
                        consumer.accept(builder.toString());
                        builder.setLength(0);
                    } else {
                        builder.append(segment);
                    }
                })
                .map(Input::of)
                .toList();
        }

        Map<Byte, SequencedMap<String, Lens>> boxes = new HashMap<>();
        for(var input : inputs) {
            {
                var box = boxes.computeIfAbsent(input.hash(), (key) -> new LinkedHashMap<>());
                var lens = input.value;

                if(input.add()) {
                    box.put(lens.label(), lens);
                } else {
                    box.remove(lens.label());
                    if(box.isEmpty()) {
                        boxes.remove(input.hash());
                    }
                }
            }

            /* Debugging
            {
                boxes.forEach((bucket, box) -> {
                    System.out.print("Box " + Byte.toUnsignedInt(bucket) + ": ");
                    for(var lens : box.sequencedValues()) {
                        System.out.print("[" + lens.label() + " " + lens.focal() + "]");
                    }
                    System.out.println();
                });
                System.out.println();
            }
             */
        }

        var result = boxes.entrySet().parallelStream()
            .mapToLong((entry) -> {
                var bucket = Byte.toUnsignedInt(entry.getKey()) + 1;
                var box = entry.getValue();
                int index = 0;
                int total = 0;
                for(var lens : box.sequencedValues()) {
                    total += bucket * ++index * lens.focal();
                }
                return total;
            })
            .sum();
        System.out.println(result);
    }
}
