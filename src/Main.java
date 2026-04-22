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
import java.util.Set;
import java.util.HashSet;

/**
 * Entry point for the Course Prerequisite Grapher.
 * Reads a CSV file of course prerequisite relations, validates that data forms a Directed Acyclic Graph (DAG),
 * and saves a Mermaid diagram to graph.txt for visualization.
 *
 * @author Jordan Eng
 * @version 4/20/2026
 */
public class Main {

    /**
     * Output file name for the generated Mermaid diagram.
     */
    private static final String OUTPUT_FILE = "graph.txt";
    /**
     * Color palette from Google's Material Design System, used for node styling.
     */
    private static final String[] COLORS = {"#e1f5fe", "#e8f5e9", "#fff3e0", "#f3e5f5", "#f1f8e9", "#fffde7"};
    /**
     * Stroke colors paired with COLORS for node borders.
     */
    private static final String[] STROKES = {"#01579b", "#2e7d32", "#e65100", "#7b1fa2", "#558b2f", "#fbc02d"};

    /**
     * Entry point of the program; Starts file validation, course loading,
     * DAG validation, and Mermaid diagram generation.
     *
     * @param theArgs command-line arguments (not used).
     */
    public static void main(String[] theArgs) {
        try (Scanner consoleScanner = new Scanner(System.in)) {
            validateUserInput(consoleScanner);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * Prompts the user for a valid file path, then loads, validates, and calls diagram generation.
     *
     * @param theScanner the scanner used to read keyboard input.
     * @throws Exception if loading, validation, or file writing fails.
     */
    public static void validateUserInput(Scanner theScanner) throws Exception {
        File targetFile = null;
        String userFileName = "";

        // Keep prompting until the user provides a valid, existing file.
        while (targetFile == null || !targetFile.exists() || !targetFile.isFile()) {
            System.out.print("Enter File Name: ");
            userFileName = theScanner.nextLine();
            targetFile = new File(userFileName);

            if (!targetFile.exists()) {
                System.out.println("Error: File does not exist.\n");
            } else if (!targetFile.isFile()) {
                System.out.println("Error: Not a file.\n");
            }
        }

        // Proceed if the file is valid.
        String title = readTitle(userFileName);
        Map<String, Course> courseMap = loadCourses(userFileName);
        validGraphStructure(courseMap, userFileName);
        saveMermaidDiagram(courseMap, title);

        System.out.println("Success: Valid DAG detected and Mermaid code saved to " + OUTPUT_FILE + ".");
    }

    public static String readTitle(String theUserFileName) throws Exception {
        try (Scanner fileScanner = new Scanner(new File(theUserFileName))) {
            if (fileScanner.hasNextLine()) {
                return fileScanner.nextLine();
            }
        }
        return "Course Prerequisite Model using DAG";
    }
    /**
     * Parses a CSV file into a map of fully constructed, immutable Course objects.
     *
     * @param theFileName the path of the CSV file to be parsed; must nto be null.
     * @return a map of course names to Course objects.
     * @throws Exception if the file cannot be read.
     */
    public static Map<String, Course> loadCourses(final String theFileName) throws Exception {
        Map<String, Set<String>> courseObjectDataMap = new HashMap<>();

        try (Scanner fileScanner = new Scanner(new File(theFileName))) {
            // Skip the header row
            if (fileScanner.hasNextLine()) {
                fileScanner.nextLine();
            }
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();

                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");

                    // Skip malformed rows.
                    if (parts.length >= 2) {
                        String courseName = parts[0].trim();
                        String successorName = parts[1].trim();

                        // Creates a new objects if it does not exist in the map already.
                        courseObjectDataMap.putIfAbsent(courseName, new HashSet<>());
                        courseObjectDataMap.putIfAbsent(successorName, new HashSet<>());

                        /*
                         * Add the direct successor to the set of the course.
                         * get(courseName) returns the HashSet value to the courseName key,
                         * add(successorName) adds the successorName to the returned HashSet.
                         */
                        courseObjectDataMap.get(courseName).add(successorName);
                    }
                }
            }
        }

        Map<String, Course> resultCourseMap = new HashMap<>();
        Set<String> stack = new HashSet<>();
        /*
         * keySet returns a set of all keys.
         * For each key in the set of all keys.
         */
        for (String courseObjectData : courseObjectDataMap.keySet()) {
            buildCourse(courseObjectData, courseObjectDataMap, resultCourseMap, stack);
        }
        return resultCourseMap;
    }

    // Return statement is only used in recursive calls.
    /**
     * Recursively constructs a Course object and all of its successors.
     * Direct successor sets are built before constructing the Course object
     * to prevent mutability.
     *
     * @param theCourseName the name of the course being built; must not be null.
     * @param theCourseObjectDataMap the string data of course objects to be built.
     * @param theResultCourseMap the map of constructed courses.
     * @return the fully constructed Course.
     */
    private static Course buildCourse(final String theCourseName,
                                      final Map<String, Set<String>> theCourseObjectDataMap,
                                      final Map<String, Course> theResultCourseMap,
                                      final Set<String> theStack) {

        // If it's in the map return we are done.
        if (theResultCourseMap.containsKey(theCourseName)) {
            return theResultCourseMap.get(theCourseName);
        }
        if (theStack.contains(theCourseName)) {
            throw new IllegalArgumentException("Cycle detected in CSV data: " + theCourseName);
        }
        theStack.add(theCourseName);

        /*
         * If not in the map recursively build each successor Course before building this one.
         * Each course object after this node needs to be created to add it to this node.
         *
         * For each String successor in the String successor set, build the Course object and add it to the
         * Course Successor set for this object.
         */
        Set<Course> successors = new HashSet<>();
        for (String successorName : theCourseObjectDataMap.get(theCourseName)) {
            if (successorName.equals(theCourseName)) {
                throw new IllegalArgumentException("Self-loop detected: " + theCourseName + " cannot be its own prerequisite.");
            }
            successors.add(buildCourse(successorName, theCourseObjectDataMap, theResultCourseMap, theStack));
        }
        theStack.remove(theCourseName);

        // Create the new Course after you have made the Course object set of all successors.
        Course course = new Course (theCourseName, successors);
        theResultCourseMap.put(theCourseName, course);

        return course;
    }

    /**
     * Validates that the course data forms a Directed Acyclic Graph (DAG).
     * Uses Depth-First Search across all nodes to detect any cycles.
     *
     * @param theCourseMap the map of course names to Course objects.
     * @param theFileName the name of the source file, used for error messages.
     * @throws RuntimeException if the map is empty or a cycle is found.
     */
    public static void validGraphStructure(final Map<String, Course> theCourseMap, final String theFileName) {
        if (theCourseMap.isEmpty()) {
            throw new RuntimeException(theFileName + " not found or empty.");
        }
        Set<Course> visited = new HashSet<>();
        Set<Course> stack = new HashSet<>();

        for (Course current : theCourseMap.values()) {
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
    public static boolean checkCycle(Course theCurrent, Set<Course> theVisited, Set<Course> theStack) {
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
        for (Course next : theCurrent.getNextCourses()) {
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
     * @param theCourseMap the map of course names to course objects.
     * @throws IOException if the output file cannot be written.
     */
    public static void saveMermaidDiagram(Map<String, Course> theCourseMap, String theTitle) throws IOException {
        try (PrintWriter writer = new PrintWriter(OUTPUT_FILE)) {
            writer.println("---");
            writer.println("title: " + theTitle); // Mermaid title
            writer.println("---");
            writer.println("graph TD"); // Top-down graph instead of left-right (Just replace TD with LR)

            // Print each course with successors.
            for (Course parent : theCourseMap.values()) {
                for (Course child : parent.getNextCourses()) {
                    String pId = parent.getName().replace(" ", "_");
                    String cId = child.getName().replace(" ", "_");
                    writer.println("    " + pId + "[\"" + parent.getName() + "\"] --> " +
                                   cId + "[\"" + child.getName() + "\"]");
                }
            }

            Map<String, Integer> prefixMap = new HashMap<>();
            int colorIndex = 0;

            writer.println("\n    %% Dynamic Styling");
            for (String name : theCourseMap.keySet()) {
                if (!name.equalsIgnoreCase("None")) {
                    String prefix = name.split(" ")[0];
                    String id = name.replace(" ", "_");

                    // Increment if new color.
                    if (!prefixMap.containsKey(prefix)) {
                        int slot = colorIndex % COLORS.length;
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
            for (String name : theCourseMap.keySet()) {
                if (name.toLowerCase().contains("major")) {
                    writer.println("    class " + name.replace(" ", "_") + " majorNode");
                }
            }
        }
    }
}