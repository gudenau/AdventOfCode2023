package net.gudenau.aoc2023;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class Day5 {
    record Key(String from, String to) {
        public static Key of(String title) {
            var split = title.substring(0, title.indexOf(' ')).split("-");
            return new Key(split[0], split[2]);
        }
    }

    record MapEntry(long source, long dest, long size) {
        public static MapEntry of(String line) {
            var split = line.split(" ");
            return new MapEntry(Long.parseLong(split[1]), Long.parseLong(split[0]), Long.parseLong(split[2]));
        }
    }

    @FunctionalInterface
    interface SimpleMap<K, V> extends Map<K, V> {
        @Override
        default int size() {
            return Integer.MAX_VALUE;
        }

        @Override
        default boolean isEmpty() {
            return false;
        }

        @Override
        default boolean containsKey(Object key) {
            return true;
        }

        @Override
        default boolean containsValue(Object value) {
            return true;
        }

        @Override
        default V put(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        default V remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void putAll(Map<? extends K, ? extends V> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        default void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        default Set<K> keySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        default Collection<V> values() {
            throw new UnsupportedOperationException();
        }

        @Override
        default Set<Entry<K, V>> entrySet() {
            throw new UnsupportedOperationException();
        }
    }

    static final class Mapper implements SimpleMap<Long, Long> {
        private final Set<MapEntry> entries;

        private Mapper(Set<MapEntry> entries) {
            this.entries = entries;
        }

        @Override
        public Long get(Object key) {
            if(!(key instanceof Long longKey)) {
                throw new IllegalArgumentException();
            }

            return entries.stream()
                .filter((entry) -> longKey >= entry.source && longKey < entry.source + entry.size)
                .findAny()
                .map((entry) -> longKey - entry.source + entry.dest)
                .orElse(longKey);
        }
    }

    public static void main(String[] args) throws Throwable {
        List<List<String>> groups;
        try(var reader = new BufferedReader(new InputStreamReader(Day5.class.getResourceAsStream("/input")))) {
            var currentGroup = new ArrayList<String>();
            groups = reader.lines()
                .<List<String>>mapMulti((value, consumer) -> {
                    if(value.isBlank()) {
                        consumer.accept(List.copyOf(currentGroup));
                        currentGroup.clear();
                    } else {
                        currentGroup.add(value);
                    }
                })
                .toList();
        }

        Map<Key, Map<Long, Long>> maps = new HashMap<>(groups.size());

        groups.parallelStream().forEach((group) -> {
            var title = group.get(0);
            if(!title.endsWith(":")) {
                return;
            }

            var key = Key.of(title);
            var values = group.stream()
                .skip(1)
                .map(MapEntry::of)
                .collect(Collectors.toUnmodifiableSet());
            synchronized(maps) {
                maps.put(key, new Mapper(values));
            }
        });

        generateMapping(maps, "seed", "fertilizer");
        generateMapping(maps, "seed", "water");
        generateMapping(maps, "seed", "light");
        generateMapping(maps, "seed", "temperature");
        generateMapping(maps, "seed", "humidity");
        generateMapping(maps, "seed", "location");

        var map = maps.get(new Key("seed", "location"));
        var lowestLocation = groups.parallelStream()
            .map((group) -> group.get(0))
            .filter((title) -> title.startsWith("seeds: "))
            .map((title) -> title.substring(7))
            .flatMap((title) -> {
                var values = Stream.of(title.split(" "))
                    .mapToLong(Long::parseLong)
                    .toArray();

                if((values.length & 1) != 0) {
                    throw new AssertionError();
                }
                var streams = new ArrayList<LongStream>();
                for(int i = 0; i < values.length; i += 2) {
                    var start = values[i];
                    streams.add(
                        LongStream.range(start, start + values[i + 1]).parallel()
                            .map(map::get)
                    );
                }
                return streams.stream();
            })
            .map(LongStream::min)
            .mapToLong((optional) -> optional.orElse(Long.MAX_VALUE))
            .min()
            .orElse(-1);

        System.out.println(lowestLocation);
    }

    private static void generateMapping(Map<Key, Map<Long, Long>> maps, String source, String dest) {
        var keys = maps.keySet();
        var sourceKeys = keys.stream()
            .filter((key) -> key.from.equals(source))
            .toList();
        var destKeys = keys.stream()
            .filter((key) -> key.to.equals(dest))
            .toList();

        sourceKeys.stream()
            .flatMap((sourceKey) -> destKeys.stream()
                .filter((destKey) -> sourceKey.to.equals(destKey.from))
                .<SimpleMap<Long, Long>>map((destKey) -> {
                    var sourceMap = maps.get(sourceKey);
                    var destMap = maps.get(destKey);

                    return (key) -> destMap.get(sourceMap.get(key));
                })
            )
            .findFirst()
            .ifPresentOrElse(
                (map) -> maps.put(new Key(source, dest), map),
                () -> { throw new RuntimeException(); }
            );
    }
}
