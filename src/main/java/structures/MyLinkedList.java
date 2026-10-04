package structures;

public final class MyLinkedList implements IntSequence {
    private Node head;
    private Node tail;
    private int size;
    private final OperationMetrics metrics = new OperationMetrics();

    public void add(int value) {
        Node node = new Node(value);
        if (tail == null) {
            head = node;
            metrics.recordMove();
        } else {
            tail.next = node;
            metrics.recordMove();
        }
        tail = node;
        metrics.recordMove();
        size++;
    }

    public void add(int index, int value) {
        checkPositionIndex(index);
        if (index == size) {
            add(value);
            return;
        }

        Node node = new Node(value);
        if (index == 0) {
            node.next = head;
            metrics.recordMove();
            head = node;
            metrics.recordMove();
        } else {
            Node previous = nodeAt(index - 1);
            node.next = previous.next;
            metrics.recordStep();
            metrics.recordMove();
            previous.next = node;
            metrics.recordMove();
        }
        size++;
    }

    public int remove(int index) {
        checkElementIndex(index);
        Node removed;
        if (index == 0) {
            removed = head;
            head = head.next;
            metrics.recordStep();
            metrics.recordMove();
            if (size == 1) {
                tail = null;
                metrics.recordMove();
            }
        } else {
            Node previous = nodeAt(index - 1);
            removed = previous.next;
            metrics.recordStep();
            previous.next = removed.next;
            metrics.recordMove();
            if (removed == tail) {
                tail = previous;
                metrics.recordMove();
            }
        }
        size--;
        return removed.value;
    }

    public int get(int index) {
        checkElementIndex(index);
        return nodeAt(index).value;
    }

    public boolean contains(int value) {
        Node current = head;
        while (current != null) {
            metrics.recordComparison();
            if (current.value == value) {
                return true;
            }
            current = current.next;
            metrics.recordStep();
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

    private Node nodeAt(int index) {
        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
            metrics.recordStep();
        }
        return current;
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

    private static final class Node {
        private final int value;
        private Node next;

        private Node(int value) {
            this.value = value;
        }
    }
}
