package structures;

public final class OperationMetrics {
    private long steps;
    private long moves;
    private long comparisons;

    public long steps() {
        return steps;
    }

    public long moves() {
        return moves;
    }

    public long comparisons() {
        return comparisons;
    }

    void recordStep() {
        steps++;
    }

    void recordMove() {
        moves++;
    }

    void recordComparison() {
        comparisons++;
    }

    void reset() {
        steps = 0;
        moves = 0;
        comparisons = 0;
    }
}
