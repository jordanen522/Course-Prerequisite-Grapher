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
                "The validator failed to detect the cycle.");

        assertEquals("Data is not a DAG.", exception.getMessage());
    }

    /**
     * Verifies the error when empty map is passed.
     */
    @Test
    void testEmptyMapValidation() {
        Map<String, Course> emptyMap = new HashMap<>();
        assertThrows(RuntimeException.class, () -> Main.validGraphStructure(emptyMap, "emptyMap.csv"),
        "Should throw RuntimeException for empty map.");
    }

    /**
     * Normal sequence should not throw error
     */
    @Test
    void testValidDAG() {
        Course math101 = new Course("MATH 101");
        Course math102 = new Course("MATH 102");

        math101.addNext(math102);

        Map<String, Course> testMap = new HashMap<>();
        testMap.put(math101.getName(), math101);
        testMap.put(math102.getName(), math102);

        assertDoesNotThrow(() -> Main.validGraphStructure(testMap, "testMap.csv"));
    }

    /**
     * Verifies course can not have itself as a prerequisite.
     */
    @Test
    void testSelfLoop() {
        Course tcss101 = new Course("TCSS 101");

        tcss101.addNext(tcss101);

        Map<String, Course> testMap = new HashMap<>();
        testMap.put(tcss101.getName(), tcss101);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> Main.validGraphStructure(testMap, "testMap.csv"));

        assertEquals("Data is not a DAG.", exception.getMessage());
    }

    /**
     * Verifies the DFS correctly accepts acyclic graphs.
     */
    @Test
    void testMultiplePathsToSameNode() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss201 = new Course("TCSS 201");
        Course tcss202 = new Course("TCSS 202");
        Course tcss301 = new Course("TCSS 301");

        tcss101.addNext(tcss201);
        tcss101.addNext(tcss202);
        tcss201.addNext(tcss301);
        tcss202.addNext(tcss301);

        Map<String, Course> testMap = new HashMap<>();
        testMap.put(tcss101.getName(), tcss101);
        testMap.put(tcss201.getName(), tcss201);
        testMap.put(tcss202.getName(), tcss202);
        testMap.put(tcss301.getName(), tcss301);

        assertDoesNotThrow(() -> Main.validGraphStructure(testMap, "testMap.csv"),
                "A diamond structure is valid and should not be flagged as a cycle.");
    }
}