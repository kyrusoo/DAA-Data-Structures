package structures;

public interface InstrumentedStructure {
    OperationMetrics metrics();

    void resetMetrics();
}
