package structures;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MyLinkedListTest {
    @Test
    void supportsEmptySingleAndDuplicateValues() {
        MyLinkedList values = new MyLinkedList();

        assertTrue(values.isEmpty());
        assertEquals(0, values.size());
        assertFalse(values.contains(4));

        values.add(4);
        values.add(4);
        assertEquals(2, values.size());
        assertTrue(values.contains(4));
        assertEquals(4, values.remove(0));
        assertEquals(4, values.get(0));
    }

    @Test
    void insertsAndRemovesAtBothEndsAndInTheMiddle() {
        MyLinkedList values = new MyLinkedList();
        values.add(1);
        values.add(3);
        values.add(1, 2);
        values.add(0, 0);
        values.add(values.size(), 4);

        assertEquals(5, values.size());
        for (int i = 0; i < values.size(); i++) {
            assertEquals(i, values.get(i));
        }

        assertEquals(0, values.remove(0));
        assertEquals(4, values.remove(values.size() - 1));
        assertEquals(2, values.remove(1));
        assertEquals(2, values.size());
        assertEquals(1, values.get(0));
        assertEquals(3, values.get(1));
    }

    @Test
    void agreesWithReferenceListForDeterministicRandomOperations() {
        MyLinkedList actual = new MyLinkedList();
        ArrayList<Integer> expected = new ArrayList<>();
        Random random = new Random(42);

        for (int step = 0; step < 2_000; step++) {
            if (expected.isEmpty() || random.nextBoolean()) {
                int value = random.nextInt(101) - 50;
                int index = random.nextInt(expected.size() + 1);
                actual.add(index, value);
                expected.add(index, value);
            } else {
                int index = random.nextInt(expected.size());
                assertEquals(expected.remove(index).intValue(), actual.remove(index));
            }

            assertEquals(expected.size(), actual.size());
            for (int i = 0; i < expected.size(); i++) {
                assertEquals(expected.get(i).intValue(), actual.get(i));
            }
        }
    }

    @Test
    void rejectsInvalidElementAndInsertionIndexes() {
        MyLinkedList values = new MyLinkedList();
        assertThrows(IndexOutOfBoundsException.class, () -> values.get(0));
        assertThrows(IndexOutOfBoundsException.class, () -> values.remove(0));
        assertThrows(IndexOutOfBoundsException.class, () -> values.add(-1, 7));
        assertThrows(IndexOutOfBoundsException.class, () -> values.add(1, 7));

        values.add(7);
        assertThrows(IndexOutOfBoundsException.class, () -> values.get(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> values.get(1));
        assertThrows(IndexOutOfBoundsException.class, () -> values.remove(1));
        assertThrows(IndexOutOfBoundsException.class, () -> values.add(2, 8));
    }

    @Test
    void canReuseListAfterRemovingItsLastElement() {
        MyLinkedList values = new MyLinkedList();
        values.add(1);
        assertEquals(1, values.remove(0));
        assertTrue(values.isEmpty());

        values.add(2);
        values.add(3);
        assertEquals(2, values.get(0));
        assertEquals(3, values.get(1));
    }
}
