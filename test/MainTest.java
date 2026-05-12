/*
 * CourseGraphTest.java
 *
 * Personal Project - Spring 2026
 * CoursePrequisiteGrapher
 */
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashMap;
import java.util.Map;

class MainTest {

    /**
     * Verifies cycle detection.
     */
    @Test
    void testCycleDetection() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss102 = new Course("TCSS 102");
        Course tcss103 = new Course("TCSS 103");

        tcss101.addNextCourse(tcss102);
        tcss102.addNextCourse(tcss103);
        tcss103.addNextCourse(tcss101);

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
        assertThrows(RuntimeException.class,
                () -> Main.validGraphStructure(emptyMap, "emptyMap.csv"),
                "Should throw RuntimeException for empty map.");
    }

    /**
     * Normal sequence should pass.
     */
    @Test
    void testValidDAG() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss201 = new Course("TCSS 201");

        tcss101.addNextCourse(tcss201);

        Map<String, Course> testMap = new HashMap<>();
        testMap.put(tcss101.getName(), tcss101);
        testMap.put(tcss201.getName(), tcss201);
        assertDoesNotThrow(() -> Main.validGraphStructure(testMap, "testMap.csv"));
    }

    /**
     * Verifies the course can not have itself as a prerequisite.
     */
    @Test
    void testSelfLoop() {
        Course tcss101 = new Course("TCSS 101");

        tcss101.addNextCourse(tcss101);

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
    void testDiamondStructure() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss201 = new Course("TCSS 201");
        Course tcss202 = new Course("TCSS 202");
        Course tcss301 = new Course("TCSS 301");

        tcss101.addNextCourse(tcss201);
        tcss101.addNextCourse(tcss202);
        tcss201.addNextCourse(tcss301);
        tcss202.addNextCourse(tcss301);

        Map<String, Course> testMap = new HashMap<>();
        testMap.put(tcss101.getName(), tcss101);
        testMap.put(tcss201.getName(), tcss201);
        testMap.put(tcss202.getName(), tcss202);
        testMap.put(tcss301.getName(), tcss301);
        assertDoesNotThrow(() -> Main.validGraphStructure(testMap, "testMap.csv"),
                "A diamond structure is valid and should not be flagged as a cycle.");
    }

    /**
     * Verifies a two node cycle throws an error.
     */
    @Test
    void testTwoNodeCycle() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss201 = new Course("TCSS 201");

        tcss101.addNextCourse(tcss201);
        tcss201.addNextCourse(tcss101);

        Map<String, Course> testMap = new HashMap<>();
        testMap.put(tcss101.getName(), tcss101);
        testMap.put(tcss201.getName(), tcss201);
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> Main.validGraphStructure(testMap, "testMap.csv"));
        assertEquals("Data is not a DAG.", exception.getMessage());
    }

    /**
     * Verifies seperate acyclic graphs pass.
     */
    @Test
    void testDisconnectedComponents() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss102 = new Course("TCSS 102");
        Course math101 = new Course("MATH 101");
        Course math102 = new Course("MATH 102");

        tcss101.addNextCourse(tcss102);
        math101.addNextCourse(math102);

        Map<String, Course> testMap = new HashMap<>();
        testMap.put(tcss101.getName(), tcss101);
        testMap.put(tcss102.getName(), tcss102);
        testMap.put(math101.getName(), math101);
        testMap.put(math102.getName(), math102);
        assertDoesNotThrow(() -> Main.validGraphStructure(testMap, "testMap.csv"),
                "Disconnected acyclic components should pass validation.");
    }

    /**
     * Verifies Course object rejects adding a null successor course.
     */
    @Test
    void testAddNextCourseNullThrows() {
        Course tcss101 = new Course("TCSS 101");
        assertThrows(IllegalArgumentException.class, () -> tcss101.addNextCourse(null),
                "addNextCourse should throw IllegalArgumentException for null input.");
    }

    /**
     * Verifies successor course list is property initialized.
     */
    @Test
    void testAddNextCourseDuplicateIgnored() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss201 = new Course("TCSS 201");

        tcss101.addNextCourse(tcss201);
        tcss101.addNextCourse(tcss201);

        assertEquals(1, tcss101.getNextCourses().size(),
                "Duplicate successor should not be added.");
    }

    /**
     * Verifies Course constructor cannot have a null name.
     */
    @Test
    void testCourseConstructorNullThrows() {
        assertThrows(IllegalArgumentException.class, () -> new Course(null),
                "Course constructor should throw IllegalArgumentException for null name.");
    }

    /**
     * Verifies accessor method works.
     */
    @Test
    void testCourseGetName() {
        Course tcss101 = new Course("TCSS 101");
        assertEquals("TCSS 101", tcss101.getName(),
                "getName should return the name passed to the constructor.");
    }
}