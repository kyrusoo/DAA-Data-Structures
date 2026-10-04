package structures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperationMetricsTest {
    @Test
    void countsArrayReadsShiftsAndValueComparisons() {
        DynamicArray values = new DynamicArray();
        values.add(10);
        values.add(20);
        values.add(30);
        values.resetMetrics();

        values.get(1);
        assertEquals(1, values.metrics().steps());

        values.add(1, 15);
        assertEquals(3, values.metrics().steps());
        assertEquals(2, values.metrics().moves());

        values.contains(99);
        assertEquals(7, values.metrics().steps());
        assertEquals(4, values.metrics().comparisons());
    }

    @Test
    void countsListLinksAndNodeTraversals() {
        MyLinkedList values = new MyLinkedList();
        values.add(0);
        values.add(1);
        values.add(2);
        values.add(3);
        values.resetMetrics();

        values.get(3);
        assertEquals(3, values.metrics().steps());

        values.add(2, 20);
        assertEquals(5, values.metrics().steps());
        assertEquals(2, values.metrics().moves());

        values.resetMetrics();
        values.contains(99);
        assertEquals(5, values.metrics().steps());
        assertEquals(5, values.metrics().comparisons());
    }

    @Test
    void countsHeapReadsMovesAndComparisons() {
        MinHeap heap = new MinHeap();
        heap.insert(1);
        heap.insert(2);
        heap.insert(3);

        heap.resetMetrics();
        assertEquals(1, heap.peekMin());
        assertEquals(1, heap.metrics().steps());

        heap.resetMetrics();
        assertEquals(1, heap.extractMin());
        assertEquals(6, heap.metrics().steps());
        assertEquals(3, heap.metrics().moves());
        assertEquals(1, heap.metrics().comparisons());
    }
}
