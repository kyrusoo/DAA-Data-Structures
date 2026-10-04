# In-Memory Workload Engine

This Java 21 project implements a resizable integer array, a singly linked integer list, and an array-based min-heap. It measures the operations used by four workloads and writes timing and operation-count data to CSV.

## Build and test

Install a Java 21 or newer JDK and Maven, then run:

```text
mvn clean test
```

The structures in `src/main/java/structures` store primitive `int` values and do not use Java collection classes. The test code uses Java collections as reference implementations.

## Generate benchmark results

On Windows, run the script from the project root:

```text
scripts\run-benchmark.cmd
```

On a POSIX shell, run:

```sh
mvn -q -DskipTests compile && java -cp target/classes benchmark.BenchmarkRunner
```

Both commands write `results/results.csv`. An optional first argument selects another CSV path. The runner uses seed 42, one warm-up run, then five timed runs; the CSV time is the median of those five. W1-W3 time only the workload operations after filling the structure. W4 times heap insertion and extraction.

The CSV columns are `workload,variant,structure,n,time_ms,steps,moves,comparisons`. W3 uses `head` and `middle`; other workloads use `-`. The four workload charts are in `results/plots` and can be regenerated after compiling with:

```text
java -cp target\classes report.PlotGenerator
```

The chart generator reads `results/results.csv`. It accepts the CSV and output directory as optional arguments.

## Operation counters

Counters are updated inside the data structure methods. A step counts an array-cell read or a linked-list link traversal. A move counts an element copy or shift in an array, a heap element swap/copy, or a linked-list pointer update. A comparison counts a comparison between stored values; index and loop-bound checks are excluded. The benchmark resets counters after initial fill for W1-W3, so those rows describe only the measured workload operations.

## Project layout

- `src/main/java/structures`: `DynamicArray`, `MyLinkedList`, `MinHeap`, and their shared interfaces and counters.
- `src/main/java/benchmark`: deterministic workload runner.
- `src/main/java/report`: PNG plot generator.
- `src/test/java/structures`: correctness and counter tests.
- `results/results.csv`: benchmark data.
- `results/plots`: workload plots.
- `REPORT.md`: complexity analysis, correctness proofs, plots, and discussion.

The submitted CSV timings were collected with IntelliJ's JBR 25.0.4 on this Windows machine. Sources compile with `--release 21`; timing values depend on the JVM, CPU, and background load.
