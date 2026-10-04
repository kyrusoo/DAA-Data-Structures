package benchmark;

import structures.DynamicArray;
import structures.IntSequence;
import structures.MinHeap;
import structures.MyLinkedList;
import structures.OperationMetrics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;

public final class BenchmarkRunner {
    private static final int[] SIZES = {100, 1_000, 10_000, 100_000};
    private static final int MEASURED_RUNS = 5;
    private static final int WARMUP_RUNS = 1;
    private static volatile long sink;

    private BenchmarkRunner() {
    }

    public static void main(String[] args) throws IOException {
        Path output = args.length == 0 ? Path.of("results", "results.csv") : Path.of(args[0]);
        Path parent = output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        StringBuilder csv = new StringBuilder();
        csv.append("workload,variant,structure,n,time_ms,steps,moves,comparisons\n");
        for (int n : SIZES) {
            int[] data = createData(n);
            int[] randomIndexes = createRandomIndexes(n);
            int[] queries = createSearchQueries(data);

            for (String structure : new String[]{"DynamicArray", "MyLinkedList"}) {
                appendRow(csv, "W1", "-", structure, n,
                        () -> runRandomAccess(structure, data, randomIndexes));
                appendRow(csv, "W2", "-", structure, n,
                        () -> runSearch(structure, data, queries));
                for (String variant : new String[]{"head", "middle"}) {
                    appendRow(csv, "W3", variant, structure, n,
                            () -> runInsertRemove(structure, data, variant, n));
                }
            }
            appendRow(csv, "W4", "-", "MinHeap", n, () -> runPriorityProcessing(data));
        }

        Files.writeString(output, csv, StandardCharsets.UTF_8);
        System.out.println("Wrote " + output.toAbsolutePath());
    }

    private static int[] createData(int n) {
        int[] data = new int[n];
        Random random = new Random(42);
        for (int i = 0; i < n; i++) {
            data[i] = random.nextInt(1_000_000);
        }
        return data;
    }

    private static int[] createRandomIndexes(int n) {
        int[] indexes = new int[10_000];
        Random random = new Random(42);
        for (int i = 0; i < indexes.length; i++) {
            indexes[i] = random.nextInt(n);
        }
        return indexes;
    }

    private static int[] createSearchQueries(int[] data) {
        int[] queries = new int[1_000];
        Random random = new Random(42);
        for (int i = 0; i < queries.length / 2; i++) {
            queries[i] = data[random.nextInt(data.length)];
            queries[queries.length / 2 + i] = 2_000_000 + i;
        }
        Random shuffleRandom = new Random(84);
        for (int i = queries.length - 1; i > 0; i--) {
            int other = shuffleRandom.nextInt(i + 1);
            int value = queries[i];
            queries[i] = queries[other];
            queries[other] = value;
        }
        return queries;
    }

    private static Sample runRandomAccess(String structureName, int[] data, int[] indexes) {
        IntSequence structure = createSequence(structureName);
        fill(structure, data);
        structure.resetMetrics();

        long checksum = 0;
        long start = System.nanoTime();
        for (int index : indexes) {
            checksum += structure.get(index);
        }
        long elapsed = System.nanoTime() - start;
        sink ^= checksum;
        return new Sample(elapsed, structure.metrics());
    }

    private static Sample runSearch(String structureName, int[] data, int[] queries) {
        IntSequence structure = createSequence(structureName);
        fill(structure, data);
        structure.resetMetrics();

        int found = 0;
        long start = System.nanoTime();
        for (int query : queries) {
            if (structure.contains(query)) {
                found++;
            }
        }
        long elapsed = System.nanoTime() - start;
        sink ^= found;
        return new Sample(elapsed, structure.metrics());
    }

    private static Sample runInsertRemove(String structureName, int[] data, String variant, int n) {
        IntSequence structure = createSequence(structureName);
        fill(structure, data);
        structure.resetMetrics();

        int index = variant.equals("head") ? 0 : n / 2;
        long removedChecksum = 0;
        long start = System.nanoTime();
        for (int i = 0; i < 1_000; i++) {
            structure.add(index, -i - 1);
        }
        for (int i = 0; i < 1_000; i++) {
            removedChecksum += structure.remove(index);
        }
        long elapsed = System.nanoTime() - start;
        sink ^= removedChecksum;
        return new Sample(elapsed, structure.metrics());
    }

    private static Sample runPriorityProcessing(int[] data) {
        MinHeap heap = new MinHeap();
        heap.resetMetrics();

        int previous = Integer.MIN_VALUE;
        long start = System.nanoTime();
        for (int value : data) {
            heap.insert(value);
        }
        for (int i = 0; i < data.length; i++) {
            int current = heap.extractMin();
            if (current < previous) {
                throw new IllegalStateException("heap extraction is not sorted");
            }
            previous = current;
        }
        long elapsed = System.nanoTime() - start;
        sink ^= previous;
        return new Sample(elapsed, heap.metrics());
    }

    private static IntSequence createSequence(String structureName) {
        return switch (structureName) {
            case "DynamicArray" -> new DynamicArray();
            case "MyLinkedList" -> new MyLinkedList();
            default -> throw new IllegalArgumentException("unknown sequence: " + structureName);
        };
    }

    private static void fill(IntSequence structure, int[] data) {
        for (int value : data) {
            structure.add(value);
        }
    }

    private static void appendRow(StringBuilder csv, String workload, String variant,
                                  String structure, int n, SampleRun run) {
        long[] elapsedNanos = new long[MEASURED_RUNS];
        OperationMetrics lastMetrics = null;
        for (int runIndex = 0; runIndex < WARMUP_RUNS + MEASURED_RUNS; runIndex++) {
            Sample sample = run.run();
            if (runIndex >= WARMUP_RUNS) {
                elapsedNanos[runIndex - WARMUP_RUNS] = sample.elapsedNanos;
                lastMetrics = sample.metrics;
            }
        }
        Arrays.sort(elapsedNanos);
        csv.append(workload).append(',')
                .append(variant).append(',')
                .append(structure).append(',')
                .append(n).append(',')
                .append(String.format(Locale.ROOT, "%.3f", elapsedNanos[MEASURED_RUNS / 2] / 1_000_000.0))
                .append(',').append(lastMetrics.steps())
                .append(',').append(lastMetrics.moves())
                .append(',').append(lastMetrics.comparisons())
                .append('\n');
    }

    @FunctionalInterface
    private interface SampleRun {
        Sample run();
    }

    private record Sample(long elapsedNanos, OperationMetrics metrics) {
    }
}
