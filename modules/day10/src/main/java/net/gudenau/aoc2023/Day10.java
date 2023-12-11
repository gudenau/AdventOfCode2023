package net.gudenau.aoc2023;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Day10 {
    enum Direction {
        NORTH(0, -1),
        SOUTH(0, 1),
        EAST(1, 0),
        WEST(-1, 0),
        ;

        final int x;
        final int y;

        Direction(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public long translate(long coord) {
            return coord(x(coord) + x, y(coord) + y);
        }

        public Direction opposite() {
            return switch(this) {
                case NORTH -> SOUTH;
                case SOUTH -> NORTH;
                case EAST -> WEST;
                case WEST -> EAST;
            };
        }
    }

    enum Pipe {
        VERTICAL('|', Direction.NORTH, Direction.SOUTH),
        HORIZONTAL('-', Direction.EAST, Direction.WEST),
        NORTH_EAST('L', Direction.NORTH, Direction.EAST),
        NORTH_WEST('J', Direction.NORTH, Direction.WEST),
        SOUTH_WEST('7', Direction.SOUTH, Direction.WEST),
        SOUTH_EAST('F', Direction.SOUTH, Direction.EAST),
        START('S', Direction.values()),
        BLANK('.')
        ;

        private static final Map<Integer, Pipe> VALUES = Stream.of(values()).collect(Collectors.toUnmodifiableMap(
            (pipe) -> (int) pipe.character,
            Function.identity()
        ));
        public static Pipe valueOf(int character) {
            return VALUES.getOrDefault(character, BLANK);
        }

        final char character;
        final Set<Direction> connections;

        Pipe(char character, Direction... connections) {
            this.character = character;
            this.connections = Set.of(connections);
        }

        public boolean permits(Direction direction) {
            // Honestly just want this done soooooo.....
            return switch(this) {
                case VERTICAL -> direction == Direction.NORTH || direction == Direction.SOUTH;
                case HORIZONTAL -> direction == Direction.EAST || direction == Direction.WEST;
                case NORTH_EAST -> direction == Direction.SOUTH || direction == Direction.WEST;
                case NORTH_WEST -> direction == Direction.SOUTH || direction == Direction.EAST;
                case SOUTH_WEST -> direction == Direction.NORTH || direction == Direction.EAST;
                case SOUTH_EAST -> direction == Direction.NORTH || direction == Direction.WEST;
                case START -> false;
                case BLANK -> true;
            };
        }
    }

    static long coord(int x, int y) {
        return (((long) x) & 0xFFFFFFFFL) | (((long) y) << 32);
    }

    static int x(long coord) {
        return (int)(coord & 0xFFFFFFFFL);
    }

    static int y(long coord) {
        return (int)(coord >> 32);
    }

    public static void main(String[] args) throws Throwable {
        Pipe[][] pipes;

        try(var reader = new BufferedReader(new InputStreamReader(Day10.class.getResourceAsStream("/input")))) {
            pipes = reader.lines()
                .filter(Predicate.not(String::isBlank))
                .map((line) -> '.' + line + '.')
                .map((line) ->
                    line.chars()
                        .mapToObj(Pipe::valueOf)
                        .toArray(Pipe[]::new)
                )
                .toArray(Pipe[][]::new);
        }

        {
            Pipe[][] newPipes = new Pipe[pipes.length + 2][];
            var blank = new Pipe[pipes[0].length];
            Arrays.fill(blank, Pipe.BLANK);
            newPipes[0] = blank;
            System.arraycopy(pipes, 0, newPipes, 1, pipes.length);
            newPipes[newPipes.length - 1] = blank;
            pipes = newPipes;
        }

        long[][] distances = new long[pipes.length][];
        int[][] fill = new int[pipes.length][];
        long start = -1;
        for(int y = 0; y < pipes.length; y++) {
            var row = pipes[y];
            for(int x = 0; x < row.length && start == -1; x++) {
                if(row[x] == Pipe.START) {
                    start = coord(x, y);
                }
            }

            Arrays.fill(distances[y] = new long[row.length], -1L);
            Arrays.fill(fill[y] = new int[row.length], 0);
        }
        if(start == -1) {
            throw new IllegalStateException();
        }

        distances[y(start)][x(start)] = 0;
        long maxDistance = 0;
        Set<Long> traversed = new HashSet<>();
        traversed.add(start);
        Queue<Long> remaining = new LinkedList<>();

        for(var direction : Direction.values()) {
            var neighborPos = direction.translate(start);
            var neighborX = x(neighborPos);
            var neighborY = y(neighborPos);

            Pipe neighborPipe;
            try {
                neighborPipe = pipes[neighborY][neighborX];
            } catch(ArrayIndexOutOfBoundsException ignored) {
                continue; // Laziness, don't do this in prod
            }

            if(!neighborPipe.connections.contains(direction.opposite())) {
                continue;
            }

            distances[neighborY][neighborX] = 1;
            remaining.add(neighborPos);
        }

        while(!remaining.isEmpty()) {
            var current = remaining.poll();
            if(!traversed.add(current)) {
                continue; // Shouldn't happen, oh well
            }

            var x = x(current);
            var y = y(current);
            var distance = distances[y][x];
            if(distance == -1) {
                throw new IllegalStateException();
            }
            var pipe = pipes[y][x];
            if(pipe == Pipe.BLANK) {
                continue;
            }

            distance++;
            for(var direction : pipe.connections) {
                var neighborPos = direction.translate(current);
                var neighborX = x(neighborPos);
                var neighborY = y(neighborPos);

                Pipe neighborPipe;
                try {
                    neighborPipe = pipes[neighborY][neighborX];
                } catch(ArrayIndexOutOfBoundsException ignored) {
                    continue; // Laziness, don't do this in prod
                }

                if(neighborPipe == Pipe.BLANK) {
                    continue;
                }

                var neighborDistance = distances[neighborY][neighborX];
                if(neighborDistance != -1) {
                    continue;
                }

                distances[neighborY][neighborX] = distance;
                remaining.add(neighborPos);
                maxDistance = Math.max(maxDistance, distance);
            }
        }

        for(int y = 0; y < pipes.length; y++) {
            var pipeRow = pipes[y];
            for(int x = 0; x < pipeRow.length; x++) {
                if(!traversed.contains(coord(x, y))) {
                    pipeRow[x] = Pipe.BLANK;
                }
            }
        }
        {
            Set<Direction> connections = new HashSet<>();
            var x = x(start);
            var y = y(start);
            if(pipes[y][x + 1].connections.contains(Direction.WEST)) {
                connections.add(Direction.WEST);
            }
            if(pipes[y][x - 1].connections.contains(Direction.EAST)) {
                connections.add(Direction.EAST);
            }
            if(pipes[y + 1][x].connections.contains(Direction.NORTH)) {
                connections.add(Direction.NORTH);
            }
            if(pipes[y - 1][x].connections.contains(Direction.SOUTH)) {
                connections.add(Direction.SOUTH);
            }
            for(var pipe : Pipe.values()) {
                if(pipe.connections.equals(connections)) {
                    pipes[y][x] = pipe;
                    break;
                }
            }
        }

        // I should have listened to einllama
        long insides = 0;
        for(var row : pipes) {
            boolean open = true;
            for(var pipe : row) {
                if(pipe.connections.contains(Direction.NORTH)) {
                    open = !open;
                }
                if(pipe == Pipe.BLANK && !open) {
                    insides++;
                }
                System.out.print(open ? ' ' : 'X');
            }
            System.out.println();
        }
        System.out.println(insides);

        double scale = 8;
        for(int y = 0; y < pipes.length; y++) {
            var pipeRow = pipes[y];
            var distRow = distances[y];
            var fillRow = fill[y];

            for(int x = 0; x < pipeRow.length; x++) {
                var pipe = pipeRow[x];
                var distance = distRow[x];
                var status = fillRow[x];

                if(distance == -1) {
                    distance = 0;
                } else {
                    distance *= scale;
                    distance %= 255;
                }

                var character = pipe == Pipe.BLANK ? ' ' : pipe.character; //pipe == Pipe.START ? "S" : Long.toHexString(distance & 0xF);
                var red = status == -1 ? 0 : (status == 0 ? 127 : 255);
                var green = distance & 0;
                System.out.print("\u001B[48;2;" + red + ";" + green + ";0m\u001B[38;2;255;0;255m" + character + "\u001B[0m");
            }
            System.out.println();
        }

        System.out.println("Max distance: " + maxDistance);
        System.out.println("Inner area: " + Stream.of(fill).flatMapToInt(IntStream::of).filter((state) -> state == 0).count());
    }
}
