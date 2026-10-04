package structures;

import org.junit.jupiter.api.Test;

import java.util.PriorityQueue;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinHeapTest {
    @Test
    void rejectsPeekAndExtractionWhenEmpty() {
        MinHeap heap = new MinHeap();

        assertThrows(IllegalStateException.class, heap::peekMin);
        assertThrows(IllegalStateException.class, heap::extractMin);
        assertTrue(heap.isEmpty());
    }

    @Test
    void maintainsHeapPropertyAfterEveryInsertAndExtraction() {
        MinHeap heap = new MinHeap();
        int[] values = {7, -3, 12, 0, -3, 5, 2, 19, -20, 8, 8, 1};

        for (int value : values) {
            heap.insert(value);
            assertTrue(heap.hasValidHeapProperty());
        }

        int previous = Integer.MIN_VALUE;
        while (!heap.isEmpty()) {
            int current = heap.extractMin();
            assertTrue(previous <= current);
            previous = current;
            assertTrue(heap.hasValidHeapProperty());
        }
        assertEquals(0, heap.size());
        assertTrue(heap.hasValidHeapProperty());
    }

    @Test
    void peekDoesNotRemoveMinimum() {
        MinHeap heap = new MinHeap();
        heap.insert(5);
        heap.insert(2);

        assertEquals(2, heap.peekMin());
        assertEquals(2, heap.peekMin());
        assertEquals(2, heap.size());
        assertTrue(heap.hasValidHeapProperty());
    }

    @Test
    void extractsSameValuesAsReferencePriorityQueue() {
        MinHeap actual = new MinHeap();
        PriorityQueue<Integer> expected = new PriorityQueue<>();
        Random random = new Random(42);

        for (int i = 0; i < 1_000; i++) {
            int value = random.nextInt(2_001) - 1_000;
            actual.insert(value);
            expected.add(value);
            assertTrue(actual.hasValidHeapProperty());
        }

        while (!expected.isEmpty()) {
            assertEquals(expected.remove().intValue(), actual.extractMin());
            assertTrue(actual.hasValidHeapProperty());
        }
        assertTrue(actual.isEmpty());
    }
}
