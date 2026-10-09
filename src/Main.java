/*
 * Main.java
 *
 * Personal Project - Spring 2026
 * Course-Prerequisite-Grapher
 */
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * Entry point for the Course Prerequisite Grapher.
 * Reads a CSV file of course prerequisite relations, validates that data forms a Directed Acyclic Graph (DAG),
 * and saves a Mermaid diagram to OutputGraph.txt for visualization.
 *
 * @author Jordan Eng
 * @version 10/9/2026
 */
public final class Main {

    /**
     * Output file name for the generated Mermaid diagram.
     */
    private static final String OUTPUT_FILE = "OutputGraph.txt";
    /**
     * Color palette from Google's Material Design System, used for node styling.
     */
    private static final String[] COLORS = {"#e1f5fe", "#e8f5e9", "#fff3e0", "#f3e5f5", "#f1f8e9", "#fffde7"};
    /**
     * Stroke colors paired with COLORS for node borders.
     */
    private static final String[] STROKES = {"#01579b", "#2e7d32", "#e65100", "#7b1fa2", "#558b2f", "#fbc02d"};

    /**
     * The contents of a parsed CSV file.
     *
     * @param title the diagram title from the first line of the file.
     * @param courses the map of course names to their fully constructed Course objects.
     */
    public record CourseGraph(String title, Map<String, Course> courses) {
    }

    /**
     * Private constructor to prevent accidental initialization.
     */
    private Main() {
        super();
    }

    /**
     * Entry point of the program; Starts file validation, course loading,
     * DAG validation, and Mermaid diagram generation.
     *
     * @param theArgs command-line arguments (not used).
     */
    public static void main(final String[] theArgs) {
        try (final Scanner consoleScanner = new Scanner(System.in)) {
            run(consoleScanner);
        } catch (final Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Runs the whole pipeline: prompts the user for a valid file path, then loads the courses,
     * validates that they form a DAG, and saves the Mermaid diagram.
     *
     * @param theScanner the scanner used to read keyboard input.
     * @throws Exception if loading, validation, or file writing fails.
     */
    public static void run(final Scanner theScanner) throws Exception {
        File targetFile = null;

        // Keep prompting until the user provides a valid, existing file.
        while (targetFile == null || !targetFile.exists() || !targetFile.isFile()) {
            System.out.print("Enter File Name: ");
            final String userFileName = theScanner.nextLine();
            targetFile = new File(userFileName);

            if (!targetFile.exists()) {
                System.out.println("Error: File does not exist.\n");
            } else if (!targetFile.isFile()) {
                System.out.println("Error: Not a file.\n");
            }
        }

        // Proceed if the file is valid.
        final String path = targetFile.getPath();
        final CourseGraph graph = loadCourses(path);
        validateGraphStructure(graph.courses(), path);
        saveMermaidDiagram(graph.courses(), graph.title());

        System.out.println("Success: Valid DAG detected and Mermaid code saved to " + OUTPUT_FILE + ".");
    }

    /**
     * Parses a CSV file in a single pass. The first line is the diagram title and every later line is a
     * "prerequisite,successor" pair. Course compares by identity, so the course map is what guarantees
     * exactly one Course instance per name.
     *
     * @param thePath the path of the CSV file to be parsed; must not be null.
     * @return the diagram title and a map of course names to their fully constructed Course objects.
     * @throws Exception if the file cannot be read.
     */
    public static CourseGraph loadCourses(final String thePath) throws Exception {
        final Map<String, Course> courseMap = new HashMap<>();
        String title = "";

        try (final Scanner fileScanner = new Scanner(new File(thePath))) {
            // The first line is the diagram title, not a prerequisite pair.
            if (fileScanner.hasNextLine()) {
                title = fileScanner.nextLine().trim();
            }
            while (fileScanner.hasNextLine()) {
                final String line = fileScanner.nextLine().trim();

                if (!line.isEmpty()) {
                    final String[] parts = line.split(",");

                    final String courseName = parts.length >= 2 ? parts[0].trim() : "";
                    final String successorName = parts.length >= 2 ? parts[1].trim() : "";

                    // Skip malformed rows: fewer than two columns or a blank course name.
                    if (!courseName.isEmpty() && !successorName.isEmpty()) {
                        // Reuse the existing Course for each name, or create one only if it is missing.
                        final Course course = courseMap.computeIfAbsent(courseName, Course::new);
                        final Course successor = courseMap.computeIfAbsent(successorName, Course::new);

                        // Duplicate rows are ignored since successors are stored in a Set.
                        course.addNextCourse(successor);
                    }
                }
            }
        }
        return new CourseGraph(title, courseMap);
    }

    /**
     * Validates that the course data forms a Directed Acyclic Graph (DAG).
     * Uses Depth-First Search across all nodes to detect any cycles.
     *
     * @param theCourseMap the map of course names to Course objects.
     * @param thePath the path of the CSV file to be parsed.
     * @throws RuntimeException if the map is empty or a cycle is found.
     */
    public static void validateGraphStructure(final Map<String, Course> theCourseMap,
                                              final String thePath) {

        if (theCourseMap.isEmpty()) {
            throw new RuntimeException(thePath + " not found or empty.");
        }
        final Set<Course> visited = new HashSet<>();
        final Set<Course> onPath = new HashSet<>();

        for (final Course current : theCourseMap.values()) {
            if (checkCycle(current, visited, onPath)) {
                throw new RuntimeException("Data is not a DAG.");
            }
        }
    }

    /**
     * Uses Depth-First Search to detect cycles in the course graph.
     *
     * @param theCurrent the course currently being viewed.
     * @param theVisited the set of all courses already visited.
     * @param theOnPath the set of courses on the current DFS path.
     * @return true if a cycle is detected; false otherwise.
     */
    public static boolean checkCycle(final Course theCurrent,
                                     final Set<Course> theVisited,
                                     final Set<Course> theOnPath) {

        if (theOnPath.contains(theCurrent)) {
            return true; // Found a loop because this course is already on the current path.
        }
        if (theVisited.contains(theCurrent)) {
            return false; // Stop checking, already fully explored with no cycle.
        }

        theVisited.add(theCurrent);
        theOnPath.add(theCurrent);

        /*
         * Check each successor in the set of successors.
         */
        for (final Course next : theCurrent.getNextCourses()) {
            if (checkCycle(next, theVisited, theOnPath)) {
                return true;
            }
        }

        theOnPath.remove(theCurrent); // Leaving this course; every path through it is cycle free.
        return false;
    }

    /**
     * Converts the course map data into Mermaid diagram syntax and saves it into a text file.
     *
     * @param theCourseMap the map of course names to Course objects.
     * @param theTitle the diagram title read from the CSV file.
     * @throws IOException if the output file cannot be written.
     */
    public static void saveMermaidDiagram(final Map<String, Course> theCourseMap,
                                          final String theTitle) throws IOException {

        final Map<String, String> nodeIds = buildNodeIds(theCourseMap);

        try (final PrintWriter writer = new PrintWriter(OUTPUT_FILE)) {
            writer.println("---");
            // Mermaid title, quoted so characters like ':' do not break the YAML front matter.
            writer.println("title: \"" + theTitle.replace("\\", "\\\\").replace("\"", "\\\"") + "\"");
            writer.println("---");
            writer.println("graph TD"); // Top-down graph instead of left-right (Just replace TD with LR)

            // Print each course with successors.
            for (final Course parent : theCourseMap.values()) {
                final String pNode = nodeIds.get(parent.getName()) + "[\"" + toLabel(parent.getName()) + "\"]";

                for (final Course child : parent.getNextCourses()) {
                    final String cNode = nodeIds.get(child.getName()) + "[\"" + toLabel(child.getName()) + "\"]";
                    writer.println("    " + pNode + " --> " + cNode);
                }
            }

            final Map<String, Integer> prefixMap = new HashMap<>();
            int colorIndex = 0;

            writer.println("\n    %% Dynamic Styling");
            for (final String name : theCourseMap.keySet()) {
                if (!"None".equalsIgnoreCase(name)) {
                    final String styleClass = "style" + sanitize(name.split(" ")[0]);

                    // Increment if new color.
                    if (!prefixMap.containsKey(styleClass)) {
                        final int slot = colorIndex % COLORS.length;
                        writer.println("    classDef " + styleClass + " fill:" + COLORS[slot] +
                                ",stroke:" + STROKES[slot] + ",stroke-width:2px;");
                        prefixMap.put(styleClass, slot);
                        colorIndex++;
                    }
                    writer.println("    class " + nodeIds.get(name) + " " + styleClass);
                }
            }

            // Apply a distinct dashed-border style for any node representing the major.
            writer.println("\n    classDef majorNode fill:#fff,stroke:#333,stroke-width:4px,stroke-dasharray: 5 5;");
            for (final String name : theCourseMap.keySet()) {
                if (name.toLowerCase().contains("major")) {
                    writer.println("    class " + nodeIds.get(name) + " majorNode");
                }
            }
        }
    }

    /**
     * Assigns every course name, including successors that are not map keys, a unique Mermaid node ID.
     * Names that sanitize to the same ID (e.g. "TCSS-101" and "TCSS 101") get a numeric suffix.
     *
     * @param theCourseMap the map of course names to Course objects.
     * @return a map of course names to their Mermaid node IDs.
     */
    public static Map<String, String> buildNodeIds(final Map<String, Course> theCourseMap) {
        final Map<String, String> nodeIds = new HashMap<>();
        final Set<String> usedIds = new HashSet<>();

        for (final Course course : theCourseMap.values()) {
            assignNodeId(course.getName(), nodeIds, usedIds);

            for (final Course next : course.getNextCourses()) {
                assignNodeId(next.getName(), nodeIds, usedIds);
            }
        }
        return nodeIds;
    }

    /**
     * Gives a course name a Mermaid node ID if it does not have one yet.
     *
     * @param theName the course name.
     * @param theNodeIds the map of course names to node IDs assigned so far.
     * @param theUsedIds the set of node IDs already taken.
     */
    private static void assignNodeId(final String theName,
                                     final Map<String, String> theNodeIds,
                                     final Set<String> theUsedIds) {
        if (theNodeIds.containsKey(theName)) {
            return;
        }

        String baseId = sanitize(theName);

        // Mermaid reserves "end", and an empty ID is not a valid node.
        if (baseId.isEmpty() || "end".equalsIgnoreCase(baseId)) {
            baseId = "n_" + baseId;
        }

        String id = baseId;
        int suffix = 2;
        while (!theUsedIds.add(id)) {
            id = baseId + "_" + suffix;
            suffix++;
        }
        theNodeIds.put(theName, id);
    }

    /**
     * Replaces every character Mermaid does not allow in IDs and class names with an underscore.
     *
     * @param theText the text to sanitize.
     * @return the text using only [A-Za-z0-9_].
     */
    private static String sanitize(final String theText) {
        return theText.replaceAll("[^A-Za-z0-9_]", "_");
    }

    /**
     * Escapes a course name for use inside a quoted Mermaid node label.
     *
     * @param theName the course name.
     * @return the name with double quotes escaped.
     */
    private static String toLabel(final String theName) {
        return theName.replace("\"", "#quot;");
    }
}
