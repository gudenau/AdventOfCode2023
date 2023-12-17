package net.gudenau.aoc2023;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day16 {
    record Pos(int x, int y) {
        Pos add(Direction direction) {
            return add(direction.vector);
        }

        private Pos add(Pos other) {
            return new Pos(x + other.x, y + other.y);
        }
    }

    enum Direction {
        UP( 0,-1),
        DOWN( 0,1),
        RIGHT( 1,0),
        LEFT(-1,0),
        ;

        final Pos vector;

        Direction(int x, int y) {
            vector = new Pos(x, y);
        }
    }

    enum Space implements Function<Direction, Set<Direction>> {
        EMPTY('.') {
            @Override
            public Set<Direction> apply(Direction direction) {
                return Set.of(direction);
            }
        },
        FORWARD_MIRROR('/') {
            @Override
            public Set<Direction> apply(Direction direction) {
                return Set.of(switch(direction) {
                    case UP -> Direction.RIGHT;
                    case DOWN -> Direction.LEFT;
                    case RIGHT -> Direction.UP;
                    case LEFT -> Direction.DOWN;
                });
            }
        },
        BACKWARD_MIRROR('\\') {
            @Override
            public Set<Direction> apply(Direction direction) {
                return Set.of(switch(direction) {
                    case UP -> Direction.LEFT;
                    case DOWN -> Direction.RIGHT;
                    case RIGHT -> Direction.DOWN;
                    case LEFT -> Direction.UP;
                });
            }
        },
        VERTICAL_SPLIT('|') {
            @Override
            public Set<Direction> apply(Direction direction) {
                return switch(direction) {
                    case UP, DOWN -> Set.of(direction);
                    case RIGHT, LEFT -> Set.of(Direction.UP, Direction.DOWN);
                };
            }
        },
        HORIZONTAL_SPLIT('-') {
            @Override
            public Set<Direction> apply(Direction direction) {
                return switch(direction) {
                    case UP, DOWN -> Set.of(Direction.RIGHT, Direction.LEFT);
                    case RIGHT, LEFT -> Set.of(direction);
                };
            }
        },
        ;

        final static Map<Integer, Space> VALUES = Stream.of(values()).collect(Collectors.toUnmodifiableMap(
            (space) -> space.character,
            Function.identity()
        ));

        static Space valueOf(int character) {
            return VALUES.getOrDefault(character, EMPTY);
        }

        final int character;

        Space(int character) {
            this.character = character;
        }
    }

    record Board(Space[] map, int width, int height) {
        static Board of(Stream<String> source) {
            var height = new AtomicInteger();
            var map = source.flatMap((line) -> {
                height.addAndGet(1);
                return line.chars().mapToObj(Day16.Space::valueOf);
                })
                .toArray(Space[]::new);
            return new Board(map, map.length / height.get(), height.get());
        }

        public Space space(Pos pos) {
            return map[pos.x + pos.y * width];
        }

        public boolean inBounds(Pos pos) {
            return pos.x >= 0 && pos.x < width && pos.y >= 0 && pos.y < height;
        }
    }

    record State(Pos pos, Direction direction) {
        State() {
            this(new Pos(0, 0), Direction.RIGHT);
        }

        Set<State> step(Board board) {
            var space = board.space(pos);
            return space.apply(direction).stream()
                .map((dir) -> new State(pos.add(dir), dir))
                .filter((state) -> board.inBounds(state.pos))
                .collect(Collectors.toUnmodifiableSet());
        }
    }

    static long compute(Board board, State starting) {
        Queue<State> workingStates = new LinkedList<>();
        workingStates.add(starting);
        Set<State> visited = new HashSet<>();
        visited.add(workingStates.peek());
        while(!workingStates.isEmpty()) {
            var state = workingStates.poll();
            state.step(board).stream()
                .filter(visited::add)
                .forEach(workingStates::add);
        }

        return visited.stream().map(State::pos).distinct().count();
    }

    public static void main(String[] args) throws Throwable {
        Board board;
        try(var stream = new BufferedReader(new InputStreamReader(Day16.class.getResourceAsStream("/input")))) {
            board = Board.of(stream.lines());
        }

        Set<State> startingPositions = new HashSet<>();
        var width = board.width;
        var height = board.height;
        for(int x = 0; x < width; x++) {
            startingPositions.add(new State(new Pos(x, 0), Direction.DOWN));
            startingPositions.add(new State(new Pos(x, height - 1), Direction.UP));
        }
        for(int y = 0; y < width; y++) {
            startingPositions.add(new State(new Pos(0, y), Direction.RIGHT));
            startingPositions.add(new State(new Pos(width - 1, y), Direction.LEFT));
        }

        System.out.println(startingPositions.parallelStream()
            .mapToLong((state) -> compute(board, state))
            .max()
            .orElse(-1L)
        );
    }
}
