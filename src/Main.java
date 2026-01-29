void main() {
    Scanner scanner = new Scanner(System.in);
    String fileName = "";
    File targetFile = null;

    while (targetFile == null || !targetFile.exists() || !targetFile.isFile()) {
        System.out.print("Enter File Name: ");
        fileName = scanner.nextLine();
        targetFile = new File(fileName);

        if (!targetFile.exists()) {
            System.out.println("Error: File does not exist.\n");
        } else if (!targetFile.isFile()) {
            System.out.println("Error: Not a file.\n");
        }
    }

    // Create a HashMap using the loadCourses method.
    Map<String, Course> courseMap = loadCourses(fileName);

    try {
        if (courseMap.isEmpty()) {
            throw new RuntimeException(fileName + " not found or empty.");
        }

        /*
        Used to check is DAG is valid.
        */
        Set<Course> visited = new HashSet<>();
        Set<Course> stack = new HashSet<>();

        // Check to make sure the data follows the DAG.
        for (Course current : courseMap.values()) {
            if (checkCycle(current, visited, stack)) {
                throw new RuntimeException("Data is not a DAG.");
            }
        }

        // 4. Generate output only if valid
        saveMermaidDiagram(courseMap);
        System.out.println("Success: Valid DAG detected and Mermaid code saved to graph.txt.");
    } catch (RuntimeException e) {
        System.out.println("Validation Error: " + e.getMessage());
    }
}

/**
 * Check if the given course is a DAG.
 */
private boolean checkCycle(Course theCurrent, Set<Course> theVisited, Set<Course> theStack) {
    if (theStack.contains(theCurrent)) {
        return true; // Found a loop because we have seen this course already this search.
    }
    if (theVisited.contains(theCurrent)) {
        return false; // Stop checking, already checked.
    }

    theVisited.add(theCurrent);
    theStack.add(theCurrent);

    /*
     * Checks the prereq's next courses to make sure there are no loops.
     */
    for (Course next : theCurrent.getNextCourses()) {
        if (checkCycle(next, theVisited, theStack)) return true;
    }

    theStack.remove(theCurrent); // Remove this Course since it has already been checked and is safe.
    return false;
}

/**
 * Loads courses from CSV.
 */
private Map<String, Course> loadCourses(final String theFileName) {
    Map<String, Course> map = new HashMap<>();

    try (Scanner sc = new Scanner(new File(theFileName))) {
        // Skip the header row
        if (sc.hasNextLine()) {
            sc.nextLine();
        }

        while (sc.hasNextLine()) {
            String line = sc.nextLine();

            // If line not empty split it at the comma.
            if (!line.trim().isEmpty()) {
                String[] parts = line.split(",");

                // If properly split into Prereq, Course.
                if (parts.length >= 2) {
                    String prereqName = parts[0].trim();
                    String courseName = parts[1].trim();

                    // Creates a new  objects if prereq/course if it does not exist in the map.
                    map.putIfAbsent(prereqName, new Course(prereqName));
                    map.putIfAbsent(courseName, new Course(courseName));

                    /*
                     * The map takes the given string prereqName and looks for the
                     * prerequisite Course object and then uses that object's add method.
                     * The prerequisite Course object adds the courseName Course object
                     * (found by searching the courseName string) to its internal
                     * myNextCourses ArrayList.
                     */
                    // Add the course to the prereq's list of classes after it.
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
 * Saves the course map as a Mermaid-compatible graph.
 */
public void saveMermaidDiagram(Map<String, Course> theCourseMap) {
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