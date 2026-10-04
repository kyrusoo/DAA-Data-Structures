package structures;

public interface IntSequence extends InstrumentedStructure {
    void add(int value);

    void add(int index, int value);

    int remove(int index);

    int get(int index);

    boolean contains(int value);
}
