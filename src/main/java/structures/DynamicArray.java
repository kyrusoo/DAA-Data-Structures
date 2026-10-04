package structures;

public final class DynamicArray implements IntSequence {
    private static final int DEFAULT_CAPACITY = 8;

    private int[] elements;
    private int size;
    private final OperationMetrics metrics = new OperationMetrics();

    public DynamicArray() {
        elements = new int[DEFAULT_CAPACITY];
    }

    public void add(int value) {
        ensureCapacityForOneMore();
        elements[size] = value;
        size++;
    }

    public void add(int index, int value) {
        checkPositionIndex(index);
        ensureCapacityForOneMore();

        for (int i = size; i > index; i--) {
            metrics.recordStep();
            elements[i] = elements[i - 1];
            metrics.recordMove();
        }
        elements[index] = value;
        size++;
    }

    public int remove(int index) {
        checkElementIndex(index);
        metrics.recordStep();
        int removed = elements[index];

        for (int i = index; i < size - 1; i++) {
            metrics.recordStep();
            elements[i] = elements[i + 1];
            metrics.recordMove();
        }
        size--;
        return removed;
    }

    public int get(int index) {
        checkElementIndex(index);
        metrics.recordStep();
        return elements[index];
    }

    public boolean contains(int value) {
        for (int i = 0; i < size; i++) {
            metrics.recordStep();
            metrics.recordComparison();
            if (elements[i] == value) {
                return true;
            }
        }
        return false;
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

    private void checkElementIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
        }
    }

    private void checkPositionIndex(int index) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("index: " + index + ", size: " + size);
        }
    }
}
