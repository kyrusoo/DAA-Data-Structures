package structures;

public final class DynamicArray {
    private static final int DEFAULT_CAPACITY = 8;

    private int[] elements;
    private int size;

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
            elements[i] = elements[i - 1];
        }
        elements[index] = value;
        size++;
    }

    public int remove(int index) {
        checkElementIndex(index);
        int removed = elements[index];

        for (int i = index; i < size - 1; i++) {
            elements[i] = elements[i + 1];
        }
        size--;
        return removed;
    }

    public int get(int index) {
        checkElementIndex(index);
        return elements[index];
    }

    public boolean contains(int value) {
        for (int i = 0; i < size; i++) {
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

    private void ensureCapacityForOneMore() {
        if (size == elements.length) {
            int[] expanded = new int[elements.length * 2];
            for (int i = 0; i < size; i++) {
                expanded[i] = elements[i];
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
