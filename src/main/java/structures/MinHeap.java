package structures;

public final class MinHeap implements InstrumentedStructure {
    private static final int DEFAULT_CAPACITY = 8;

    private int[] elements = new int[DEFAULT_CAPACITY];
    private int size;
    private final OperationMetrics metrics = new OperationMetrics();

    public void insert(int value) {
        ensureCapacityForOneMore();
        elements[size] = value;
        bubbleUp(size);
        size++;
    }

    public int peekMin() {
        if (size == 0) {
            throw new IllegalStateException("heap is empty");
        }
        metrics.recordStep();
        return elements[0];
    }

    public int extractMin() {
        if (size == 0) {
            throw new IllegalStateException("heap is empty");
        }

        metrics.recordStep();
        int minimum = elements[0];
        size--;
        if (size > 0) {
            metrics.recordStep();
            elements[0] = elements[size];
            metrics.recordMove();
            bubbleDown(0);
        }
        return minimum;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public OperationMetrics metrics() {
        return metrics;
    }

    @Override
    public void resetMetrics() {
        metrics.reset();
    }

    boolean hasValidHeapProperty() {
        for (int child = 1; child < size; child++) {
            int parent = (child - 1) / 2;
            if (elements[parent] > elements[child]) {
                return false;
            }
        }
        return true;
    }

    private void bubbleUp(int child) {
        while (child > 0) {
            int parent = (child - 1) / 2;
            metrics.recordStep();
            metrics.recordStep();
            metrics.recordComparison();
            if (elements[parent] <= elements[child]) {
                break;
            }
            swap(parent, child);
            child = parent;
        }
    }

    private void bubbleDown(int parent) {
        while (true) {
            int left = parent * 2 + 1;
            if (left >= size) {
                return;
            }

            int right = left + 1;
            int smallerChild = left;
            if (right < size) {
                metrics.recordStep();
                metrics.recordStep();
                metrics.recordComparison();
                if (elements[right] < elements[left]) {
                    smallerChild = right;
                }
            }
            metrics.recordStep();
            metrics.recordStep();
            metrics.recordComparison();
            if (elements[parent] <= elements[smallerChild]) {
                return;
            }

            swap(parent, smallerChild);
            parent = smallerChild;
        }
    }

    private void swap(int first, int second) {
        metrics.recordStep();
        int value = elements[first];
        metrics.recordStep();
        elements[first] = elements[second];
        metrics.recordMove();
        elements[second] = value;
        metrics.recordMove();
    }

    private void ensureCapacityForOneMore() {
        if (size == elements.length) {
            int[] expanded = new int[elements.length * 2];
            for (int i = 0; i < size; i++) {
                metrics.recordStep();
                expanded[i] = elements[i];
                metrics.recordMove();
            }
            elements = expanded;
        }
    }
}
