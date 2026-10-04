package structures;

public final class MyLinkedList implements IntSequence {
    private Node head;
    private Node tail;
    private int size;

    public void add(int value) {
        Node node = new Node(value);
        if (tail == null) {
            head = node;
        } else {
            tail.next = node;
        }
        tail = node;
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
            head = node;
        } else {
            Node previous = nodeAt(index - 1);
            node.next = previous.next;
            previous.next = node;
        }
        size++;
    }

    public int remove(int index) {
        checkElementIndex(index);
        Node removed;
        if (index == 0) {
            removed = head;
            head = head.next;
            if (size == 1) {
                tail = null;
            }
        } else {
            Node previous = nodeAt(index - 1);
            removed = previous.next;
            previous.next = removed.next;
            if (removed == tail) {
                tail = previous;
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
            if (current.value == value) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private Node nodeAt(int index) {
        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
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
