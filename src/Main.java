/*
 * Main.java
 *
 * Personal Project - Spring 2026
 * CoursePrequisiteGrapher
 */
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for the Course Prerequisite Grapher.
 * Reads a CSV file of course prerequisite relations, validates that data forms a Directed Acyclic Graph (DAG),
 * and saves a Mermaid diagram to OutputGraph.txt for visualization.
 *
 * @author Jordan Eng
 * @version 5/11/2026
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
            validateUserInput(consoleScanner);
        } catch (final Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Prompts the user for a valid file path, then loads, validates, and calls diagram generation.
     *
     * @param theScanner the scanner used to read keyboard input.
     * @throws Exception if loading, validation, or file writing fails.
     */
    public static void validateUserInput(final Scanner theScanner) throws Exception {
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
        final String title = readTitle(path);
        final Map<String, Course> courseMap = loadCourses(path);
        validGraphStructure(courseMap, path);
        saveMermaidDiagram(courseMap, title);

        System.out.println("Success: Valid DAG detected and Mermaid code saved to " + OUTPUT_FILE + ".");
    }

    /**
     * Reads the first line of the CSV file to find the title used for the Mermaid diagram.
     * If empty return a default title.
     *
     * @param thePath the path of the CSV file; must not be null.
     * @return the first line of the file as a string or a default title if the file is empty.
     * @throws Exception if the file cannot be read.
     */
    public static String readTitle(final String thePath) throws Exception {
        try (final Scanner fileScanner = new Scanner(new File(thePath))) {
            if (fileScanner.hasNextLine()) {
                return fileScanner.nextLine();
            }
        }
        return "Course Prerequisite Model using DAG";
    }

    /**
     * Parses a CSV file into a map of fully constructed Course objects with their successors wired up.
     *
     * @param thePath the path of the CSV file to be parsed; must not be null.
     * @return a map of course names to their fully constructed Course objects.
     * @throws Exception if the file cannot be read.
     */
    public static Map<String, Course> loadCourses(final String thePath) throws Exception {
        final Map<String, Course> courseMap = new HashMap<>();

        try (final Scanner fileScanner = new Scanner(new File(thePath))) {
            // Skip the header row
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine();
            }
            while (fileScanner.hasNextLine()) {
                final String line = fileScanner.nextLine().trim();

                if (!line.isEmpty()) {
                    final String[] parts = line.split(",");

                    // Skip malformed rows.
                    if (parts.length >= 2) {
                        final String courseName = parts[0].trim();
                        final String successorName = parts[1].trim();

                        // Creates a new objects if it does not exist in the map already.
                        courseMap.putIfAbsent(courseName, new Course(courseName));
                        courseMap.putIfAbsent(successorName, new Course(successorName));

                        /*
                         * courseMap.get(courseName) returns the Course object to the courseName key,
                         * .addNextCourse(courseMap.get(successorName)) adds the successor Course
                         * to the ArrayList of the first course.
                         */
                        courseMap.get(courseName).addNextCourse(courseMap.get(successorName));
                    }
                }
            }
        }
        return courseMap;
    }

    /**
     * Validates that the course data forms a Directed Acyclic Graph (DAG).
     * Uses Depth-First Search across all nodes to detect any cycles.
     *
     * @param theCourseMap the map of course names to Course objects.
     * @param thePath the path of the CSV file to be parsed.
     * @throws RuntimeException if the map is empty or a cycle is found.
     */
    public static void validGraphStructure(final Map<String, Course> theCourseMap,
                                           final String thePath) {

        if (theCourseMap.isEmpty()) {
            throw new RuntimeException(thePath + " not found or empty.");
        }
        final List<Course> visited = new ArrayList<>();
        final List<Course> stack = new ArrayList<>();

        for (final Course current : theCourseMap.values()) {
            if (checkCycle(current, visited, stack)) {
                throw new RuntimeException("Data is not a DAG.");
            }
        }
    }

    /**
     * Uses Depth-First Search to detect cycles in the course graph.
     *
     * @param theCurrent the course currently being viewed.
     * @param theVisited the set of all courses already visited.
     * @param theStack the set of courses in current path.
     * @return true if a cycle is detected; false otherwise.
     */
    public static boolean checkCycle(final Course theCurrent,
                                     final List<Course> theVisited,
                                     final List<Course> theStack) {

        if (theStack.contains(theCurrent)) {
            return true; // Found a loop because we have seen this course already this search.
        }
        if (theVisited.contains(theCurrent)) {
            return false; // Stop checking, already checked.
        }

        theVisited.add(theCurrent);
        theStack.add(theCurrent);

        /*
         * Check each successor in the set of successors.
         */
        for (final Course next : theCurrent.getNextCourses()) {
            if (checkCycle(next, theVisited, theStack)) {
                return true;
            }
        }

        theStack.remove(theCurrent); // Remove this Course since it has already been checked and is safe.
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

        try (final PrintWriter writer = new PrintWriter(OUTPUT_FILE)) {
            writer.println("---");
            writer.println("title: " + theTitle); // Mermaid title
            writer.println("---");
            writer.println("graph TD"); // Top-down graph instead of left-right (Just replace TD with LR)

            // Print each course with successors.
            for (final Course parent : theCourseMap.values()) {
                for (final Course child : parent.getNextCourses()) {
                    final String pId = parent.getName().replace(" ", "_");
                    final String cId = child.getName().replace(" ", "_");
                    writer.println("    " + pId + "[\"" + parent.getName()
                            + "\"] --> " + cId + "[\"" + child.getName() + "\"]");
                }
            }

            final Map<String, Integer> prefixMap = new HashMap<>();
            int colorIndex = 0;

            writer.println("\n    %% Dynamic Styling");
            for (final String name : theCourseMap.keySet()) {
                if (!"None".equalsIgnoreCase(name)) {
                    final String prefix = name.split(" ")[0];
                    final String id = name.replace(" ", "_");

                    // Increment if new color.
                    if (!prefixMap.containsKey(prefix)) {
                        final int slot = colorIndex % COLORS.length;
                        writer.println("    classDef style" + prefix + " fill:" + COLORS[slot] +
                                ",stroke:" + STROKES[slot] + ",stroke-width:2px;");
                        prefixMap.put(prefix, slot);
                        colorIndex++;
                    }
                    writer.println("    class " + id + " style" + prefix);
                }
            }

            // Apply a distinct dashed-border style for any node representing the major.
            writer.println("\n    classDef majorNode fill:#fff,stroke:#333,stroke-width:4px,stroke-dasharray: 5 5;");
            for (final String name : theCourseMap.keySet()) {
                if (name.toLowerCase().contains("major")) {
                    writer.println("    class " + name.replace(" ", "_") + " majorNode");
                }
            }
        }
    }
}