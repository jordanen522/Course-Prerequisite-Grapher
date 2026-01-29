import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;

class MainTest {

    /**
     * Verifies cycle detection with correct error message.
     */
    @Test
    void testCycleDetection() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss102 = new Course("TCSS 102");
        Course tcss103 = new Course("TCSS 103");

        tcss101.addNext(tcss102);
        tcss102.addNext(tcss103);
        tcss103.addNext(tcss101);

        Map<String, Course> testMap = new HashMap<>();
        testMap.put(tcss101.getName(), tcss101);
        testMap.put(tcss102.getName(), tcss102);
        testMap.put(tcss103.getName(), tcss103);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> Main.validGraphStructure(testMap, "testMap.csv"),
                "The validator failed to detect the 101-102-103 cycle.");

        assertEquals("Data is not a DAG.", exception.getMessage());
    }

    /**
     * Verifies the error when empty map is passed.
     */
    @Test
    void testEmptyMapValidation() {
        Map<String, Course> emptyMap = new HashMap<>();
        assertThrows(RuntimeException.class, () -> Main.validGraphStructure(emptyMap, "empty_test.csv"));
    }
}