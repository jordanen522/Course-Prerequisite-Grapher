/*
 * MainTest.java
 *
 * Personal Project - Spring 2026
 * CoursePrequisiteGrapher
 */
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tests for Course and the DAG validation in Main. Course uses identity equality and
 * identity hashing, so cyclic course graphs can be built and hashed without recursion.
 *
 * @author Jordan Eng
 * @version 10/9/2026
 */
class MainTest {

    /* --------------------CYCLE DETECTION-------------------- */

    /**
     * Verifies cycle detection on a three node indirect cycle.
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
     * Verifies a longer indirect cycle (101 -> 102 -> 103 -> 104 -> 105 -> 101) is detected.
     */
    @Test
    void testLongIndirectCycle() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss102 = new Course("TCSS 102");
        Course tcss103 = new Course("TCSS 103");
        Course tcss104 = new Course("TCSS 104");
        Course tcss105 = new Course("TCSS 105");

        tcss101.addNextCourse(tcss102);
        tcss102.addNextCourse(tcss103);
        tcss103.addNextCourse(tcss104);
        tcss104.addNextCourse(tcss105);
        tcss105.addNextCourse(tcss101);

        Map<String, Course> testMap = new HashMap<>();
        testMap.put(tcss101.getName(), tcss101);
        testMap.put(tcss102.getName(), tcss102);
        testMap.put(tcss103.getName(), tcss103);
        testMap.put(tcss104.getName(), tcss104);
        testMap.put(tcss105.getName(), tcss105);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> Main.validGraphStructure(testMap, "testMap.csv"));
        assertEquals("Data is not a DAG.", exception.getMessage());
    }

    /**
     * Verifies a cycle is found when it is only reachable through an acyclic entry course
     * (100 -> 101 -> 102 -> 103 -> 101).
     */
    @Test
    void testCycleReachableFromAcyclicEntry() {
        Course tcss100 = new Course("TCSS 100");
        Course tcss101 = new Course("TCSS 101");
        Course tcss102 = new Course("TCSS 102");
        Course tcss103 = new Course("TCSS 103");

        tcss100.addNextCourse(tcss101);
        tcss101.addNextCourse(tcss102);
        tcss102.addNextCourse(tcss103);
        tcss103.addNextCourse(tcss101);

        Map<String, Course> testMap = new HashMap<>();
        testMap.put(tcss100.getName(), tcss100);
        testMap.put(tcss101.getName(), tcss101);
        testMap.put(tcss102.getName(), tcss102);
        testMap.put(tcss103.getName(), tcss103);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> Main.validGraphStructure(testMap, "testMap.csv"));
        assertEquals("Data is not a DAG.", exception.getMessage());
    }

    /**
     * Verifies the DFS follows successors that are not keys in the map and still finds the cycle.
     */
    @Test
    void testCycleOnlyReachableThroughSuccessors() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss102 = new Course("TCSS 102");
        Course tcss103 = new Course("TCSS 103");

        tcss101.addNextCourse(tcss102);
        tcss102.addNextCourse(tcss103);
        tcss103.addNextCourse(tcss102);

        Map<String, Course> testMap = new HashMap<>();
        testMap.put(tcss101.getName(), tcss101);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> Main.validGraphStructure(testMap, "testMap.csv"));
        assertEquals("Data is not a DAG.", exception.getMessage());
    }

    /**
     * Verifies a diamond whose bottom course points back to the top is detected as a cycle.
     */
    @Test
    void testCycleThroughDiamond() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss201 = new Course("TCSS 201");
        Course tcss202 = new Course("TCSS 202");
        Course tcss301 = new Course("TCSS 301");

        tcss101.addNextCourse(tcss201);
        tcss101.addNextCourse(tcss202);
        tcss201.addNextCourse(tcss301);
        tcss202.addNextCourse(tcss301);
        tcss301.addNextCourse(tcss101);

        Map<String, Course> testMap = new HashMap<>();
        testMap.put(tcss101.getName(), tcss101);
        testMap.put(tcss201.getName(), tcss201);
        testMap.put(tcss202.getName(), tcss202);
        testMap.put(tcss301.getName(), tcss301);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> Main.validGraphStructure(testMap, "testMap.csv"));
        assertEquals("Data is not a DAG.", exception.getMessage());
    }

    /**
     * Verifies one cyclic component fails the whole graph even when another component is acyclic.
     */
    @Test
    void testCycleInOneOfTwoComponents() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss102 = new Course("TCSS 102");
        Course math101 = new Course("MATH 101");
        Course math102 = new Course("MATH 102");
        Course math103 = new Course("MATH 103");

        tcss101.addNextCourse(tcss102);
        math101.addNextCourse(math102);
        math102.addNextCourse(math103);
        math103.addNextCourse(math101);

        Map<String, Course> testMap = new HashMap<>();
        testMap.put(tcss101.getName(), tcss101);
        testMap.put(tcss102.getName(), tcss102);
        testMap.put(math101.getName(), math101);
        testMap.put(math102.getName(), math102);
        testMap.put(math103.getName(), math103);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> Main.validGraphStructure(testMap, "testMap.csv"));
        assertEquals("Data is not a DAG.", exception.getMessage());
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
     * Verifies removing one edge of an indirect cycle makes the graph a valid DAG.
     */
    @Test
    void testRemovingEdgeBreaksCycle() {
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

        assertThrows(RuntimeException.class,
                () -> Main.validGraphStructure(testMap, "testMap.csv"));

        assertTrue(tcss103.removeNextCourse(tcss101));
        assertDoesNotThrow(() -> Main.validGraphStructure(testMap, "testMap.csv"),
                "Graph should be a DAG once the back edge is removed.");
    }

    /* --------------------VALID DAGS-------------------- */

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
     * Verifies a shortcut edge (101 -> 301 alongside 101 -> 201 -> 301) is not mistaken for a cycle.
     */
    @Test
    void testForwardEdgeNotCycle() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss201 = new Course("TCSS 201");
        Course tcss301 = new Course("TCSS 301");

        tcss101.addNextCourse(tcss201);
        tcss201.addNextCourse(tcss301);
        tcss101.addNextCourse(tcss301);

        Map<String, Course> testMap = new HashMap<>();
        testMap.put(tcss101.getName(), tcss101);
        testMap.put(tcss201.getName(), tcss201);
        testMap.put(tcss301.getName(), tcss301);
        assertDoesNotThrow(() -> Main.validGraphStructure(testMap, "testMap.csv"));
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

    /* --------------------LOADING FROM CSV-------------------- */

    /**
     * Verifies loadCourses reuses one Course instance per name, so an indirect cycle written
     * across separate CSV rows is wired up and detected.
     *
     * @param theTempDir a temporary directory provided by JUnit.
     * @throws Exception if the test file cannot be written or read.
     */
    @Test
    void testLoadCoursesIndirectCycleFromCsv(@TempDir Path theTempDir) throws Exception {
        Path csv = writeCsv(theTempDir, "Loop\nTCSS 101,TCSS 102\nTCSS 102,TCSS 103\nTCSS 103,TCSS 101\n");

        Map<String, Course> courseMap = Main.loadCourses(csv.toString());

        assertEquals(3, courseMap.size());
        Course tcss101 = courseMap.get("TCSS 101");
        Course tcss103 = courseMap.get("TCSS 103");
        assertTrue(tcss103.getNextCourses().contains(tcss101),
                "TCSS 103 should point to the same TCSS 101 instance stored in the map.");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> Main.validGraphStructure(courseMap, csv.toString()));
        assertEquals("Data is not a DAG.", exception.getMessage());
    }

    /**
     * Verifies duplicate CSV rows produce a single successor edge.
     *
     * @param theTempDir a temporary directory provided by JUnit.
     * @throws Exception if the test file cannot be written or read.
     */
    @Test
    void testLoadCoursesDuplicateRows(@TempDir Path theTempDir) throws Exception {
        Path csv = writeCsv(theTempDir, "Title\nTCSS 101,TCSS 201\nTCSS 101,TCSS 201\n");

        Map<String, Course> courseMap = Main.loadCourses(csv.toString());

        assertEquals(2, courseMap.size());
        assertEquals(1, courseMap.get("TCSS 101").getNextCourses().size());
    }

    /* --------------------COURSE-------------------- */

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
     * Verifies a duplicate successor is not added twice.
     */
    @Test
    void testAddNextCourseDuplicateIgnored() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss201 = new Course("TCSS 201");

        assertTrue(tcss101.addNextCourse(tcss201));
        assertFalse(tcss101.addNextCourse(tcss201));

        assertEquals(1, tcss101.getNextCourses().size(),
                "Duplicate successor should not be added.");
    }

    /**
     * Verifies removeNextCourse reports whether the course was a successor.
     */
    @Test
    void testRemoveNextCourse() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss201 = new Course("TCSS 201");

        tcss101.addNextCourse(tcss201);

        assertTrue(tcss101.removeNextCourse(tcss201));
        assertFalse(tcss101.removeNextCourse(tcss201));
        assertTrue(tcss101.getNextCourses().isEmpty());
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

    /**
     * Verifies the successor view can not be modified from outside.
     */
    @Test
    void testGetNextCoursesUnmodifiable() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss201 = new Course("TCSS 201");

        assertThrows(UnsupportedOperationException.class,
                () -> tcss101.getNextCourses().add(tcss201));
    }

    /**
     * Verifies the successor view reflects later changes to the course.
     */
    @Test
    void testGetNextCoursesLiveView() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss201 = new Course("TCSS 201");
        Set<Course> view = tcss101.getNextCourses();

        tcss101.addNextCourse(tcss201);

        assertTrue(view.contains(tcss201), "The view should see successors added after it was returned.");
    }

    /**
     * Verifies two courses with the same name are still different objects under identity equality.
     */
    @Test
    void testEqualsIsIdentity() {
        Course first = new Course("TCSS 101");
        Course second = new Course("TCSS 101");

        assertEquals(first, first);
        assertNotEquals(first, second);
        assertTrue(Course.isSameName(first, second));
    }

    /**
     * Verifies hashCode, equals, and HashSet membership terminate on courses in a cycle.
     */
    @Test
    void testHashingCyclicCourses() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss102 = new Course("TCSS 102");
        Course tcss103 = new Course("TCSS 103");

        tcss101.addNextCourse(tcss102);
        tcss102.addNextCourse(tcss103);
        tcss103.addNextCourse(tcss101);

        assertDoesNotThrow(tcss101::hashCode);

        Set<Course> set = new HashSet<>();
        set.add(tcss101);
        set.add(tcss102);
        set.add(tcss103);

        assertEquals(3, set.size());
        assertTrue(set.contains(tcss101));
    }

    /**
     * Verifies toString only lists successor names, so it terminates on a cycle.
     */
    @Test
    void testToStringOnCycle() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss102 = new Course("TCSS 102");

        tcss101.addNextCourse(tcss102);
        tcss102.addNextCourse(tcss101);

        assertEquals("TCSS 101 -> [TCSS 102]", tcss101.toString());
        assertEquals("TCSS 102 -> [TCSS 101]", tcss102.toString());
    }

    /**
     * Verifies toString on a course with no successors.
     */
    @Test
    void testToStringNoSuccessors() {
        Course tcss101 = new Course("TCSS 101");
        assertEquals("TCSS 101 -> []", tcss101.toString());
    }

    /* --------------------STATIC HELPERS-------------------- */

    /**
     * Verifies the null and same reference cases of the static comparison helpers.
     */
    @Test
    void testStaticHelpersNullHandling() {
        Course tcss101 = new Course("TCSS 101");

        assertTrue(Course.isSameName(null, null));
        assertFalse(Course.isSameName(tcss101, null));
        assertFalse(Course.isSameName(null, tcss101));
        assertTrue(Course.isSameSuccessors(null, null));
        assertFalse(Course.isSameSuccessors(tcss101, null));
        assertTrue(Course.isSameNameAndSuccessors(tcss101, tcss101));
    }

    /**
     * Verifies isSameSuccessors compares successor instances and terminates on cyclic successors.
     */
    @Test
    void testIsSameSuccessorsOnCycle() {
        Course tcss101 = new Course("TCSS 101");
        Course tcss102 = new Course("TCSS 102");
        Course copy101 = new Course("TCSS 101");

        tcss101.addNextCourse(tcss102);
        tcss102.addNextCourse(tcss101);
        copy101.addNextCourse(tcss102);

        assertTrue(Course.isSameSuccessors(tcss101, copy101));
        assertTrue(Course.isSameNameAndSuccessors(tcss101, copy101));

        copy101.removeNextCourse(tcss102);
        copy101.addNextCourse(new Course("TCSS 102"));

        assertFalse(Course.isSameSuccessors(tcss101, copy101),
                "A different TCSS 102 instance is not the same successor.");
        assertFalse(Course.isSameNameAndSuccessors(tcss101, copy101));
    }

    /**
     * Writes the given text to a CSV file in the temporary directory.
     *
     * @param theDir the directory to write into.
     * @param theContents the CSV contents.
     * @return the path of the written file.
     * @throws IOException if the file cannot be written.
     */
    private static Path writeCsv(final Path theDir, final String theContents) throws IOException {
        final Path csv = theDir.resolve("test.csv");
        Files.writeString(csv, theContents);
        return csv;
    }
}