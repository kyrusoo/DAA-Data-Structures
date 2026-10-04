package structures;

public final class MinHeap {
    private static final int DEFAULT_CAPACITY = 8;

    private int[] elements = new int[DEFAULT_CAPACITY];
    private int size;

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
        return elements[0];
    }

    public int extractMin() {
        if (size == 0) {
            throw new IllegalStateException("heap is empty");
        }

        int minimum = elements[0];
        size--;
        if (size > 0) {
            elements[0] = elements[size];
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
            if (right < size && elements[right] < elements[left]) {
                smallerChild = right;
            }
            if (elements[parent] <= elements[smallerChild]) {
                return;
            }

            swap(parent, smallerChild);
            parent = smallerChild;
        }
    }

    private void swap(int first, int second) {
        int value = elements[first];
        elements[first] = elements[second];
        elements[second] = value;
    }

    private void ensureCapacityForOneMore() {
        if (size == elements.length) {
            int[] expanded = new int[elements.length * 2];
            for (int i = 0; i < size; i++) {
                expanded[i] = elements[i];
            }
            elements = expanded;
        }
    }
}
