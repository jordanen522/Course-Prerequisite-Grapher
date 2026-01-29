import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.HashSet;

/**
 * This program takes user data and outputs it in Mermaid code which can be copy and pasted into a viewable graph.
 *
 * @author Jordan Eng
 * @version 1/28/2026
 */
public class Main {
    public static void main(String[] theArgs) {
        try (Scanner scanner = new Scanner(System.in)) {

            String fileName = validateUserInput(scanner);
            Map<String, Course> courseMap = loadCourses(fileName);

            try {
                validGraphStructure(courseMap, fileName);
                saveMermaidDiagram(courseMap);
                System.out.println("Success: Valid DAG detected and Mermaid code saved to graph.txt.");
            } catch (RuntimeException e) {
                System.out.println("Validation Error: " + e.getMessage());
            }
        } catch (RuntimeException e) {
            System.out.println("Validation Error: " + e.getMessage());
        }
    }

    /**
     * Prompts the user for a file name and ensures the file exists and is valid.
     *
     * @param theScanner the scanner used to read keyboard input.
     * @return the file path as a string.
     */
    public static String validateUserInput(Scanner theScanner) {
        File targetFile = null;
        String userFileName = "";

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
        return userFileName;
    }

    /**
     * Parses the CSV file to populate the map.
     *
     * @param theFileName the name of the file to be parsed.
     * @return a map with course names and Course objects.
     */
    public static Map<String, Course> loadCourses(final String theFileName) {
        Map<String, Course> map = new HashMap<>();

        try (Scanner sc = new Scanner(new File(theFileName))) {
            // Skip the header row
            if (sc.hasNextLine()) {
                sc.nextLine();
            }
            while (sc.hasNextLine()) {
                String line = sc.nextLine();

                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(",");

                    if (parts.length >= 2) {
                        String prereqName = parts[0].trim();
                        String courseName = parts[1].trim();

                        // Creates a new  objects if it does not exist in the map already.
                        map.putIfAbsent(prereqName, new Course(prereqName));
                        map.putIfAbsent(courseName, new Course(courseName));

                        // Add the course to the list of classes after the prereq.
                        map.get(prereqName).addNext(map.get(courseName));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("File Error: " + e.getMessage());
        }
        return map;
    }

    /**
     * Validates that course data forms a Directed Acyclic Graph.
     *
     * @param theCourseMap the map containing course names and Course objects.
     * @param theFileName the name of the source file.
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
     * Uses Depth-First Search to detect cycles in the course structure.
     *
     * @param theCurrent the course currently being viewed.
     * @param theVisited the set of all courses already visited.
     * @param theStack the set of courses in current path.
     * @return False is no loops.
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
         * Checks the courses after are safe.
         */
        for (Course next : theCurrent.getNextCourses()) {
            if (checkCycle(next, theVisited, theStack)) return true;
        }

        theStack.remove(theCurrent); // Remove this Course since it has already been checked and is safe.
        return false;
    }

    /**
     * Converts the hashMap data into Mermaid syntax and saves it into a text file.
     *
     * @param theCourseMap the map containing the course hierarchy to be visualized.
     */
    public static void saveMermaidDiagram(Map<String, Course> theCourseMap) {
        try (PrintWriter writer = new PrintWriter("graph.txt")) {
            writer.println("---");
            writer.println("title: Course Prerequisite Model using DAG"); // Mermaid title
            writer.println("---");
            writer.println("graph TD"); // Top-down graph instead of left-right (Just replace TD with LR)

            // Print each prereq and the courses that come after.
            for (Course parent : theCourseMap.values()) {
                for (Course child : parent.getNextCourses()) {
                    String pId = parent.getName().replace(" ", "_");
                    String cId = child.getName().replace(" ", "_");
                    writer.println("    " + pId + "[\"" + parent.getName() + "\"] --> " +
                            cId + "[\"" + child.getName() + "\"]");
                }
            }

            // Color library from Google's Material Design System.
            String[] colors = {"#e1f5fe", "#e8f5e9", "#fff3e0", "#f3e5f5", "#f1f8e9", "#fffde7"};
            String[] strokes = {"#01579b", "#2e7d32", "#e65100", "#7b1fa2", "#558b2f", "#fbc02d"};

            Map<String, Integer> prefixMap = new HashMap<>();
            int colorIndex = 0;

            writer.println("\n    %% Dynamic Styling");
            for (String name : theCourseMap.keySet()) {
                if (name.equalsIgnoreCase("None")) continue;

                String prefix = name.split(" ")[0];
                String id = name.replace(" ", "_");

                // Increment if new color.
                if (!prefixMap.containsKey(prefix)) {
                    int slot = colorIndex % colors.length;
                    writer.println("    classDef style" + prefix + " fill:" + colors[slot] +
                            ",stroke:" + strokes[slot] + ",stroke-width:2px;");
                    prefixMap.put(prefix, slot);
                    colorIndex++;
                }
                writer.println("    class " + id + " style" + prefix);
            }

            // Different styling for major
            writer.println("\n    classDef majorNode fill:#fff,stroke:#333,stroke-width:4px,stroke-dasharray: 5 5;");
            for (String name : theCourseMap.keySet()) {
                if (name.toLowerCase().contains("major")) {
                    writer.println("    class " + name.replace(" ", "_") + " majorNode");
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }
}