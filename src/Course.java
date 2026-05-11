/*
 * Course.java
 *
 * Personal Project - Spring 2026
 * CoursePrequisiteGrapher
 */
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single course node in a Directed Acyclic Graph (DAG).
 * Each course holds its name and set of courses that directly follow it.
 * This class is a mutable. Inserting a Course object into a hash-based collection
 * will corrupt the collection's invariants, changing the hashing of the object.
 * That responsibility belongs to the caller.
 *
 * @author Jordan Eng
 * @version 5/11/2026
 */
public class Course implements CourseInterface {

    /**
     * The name of this course.
     */
    private final String myName;
    /**
     * The mutable list of courses that have this course as a direct prerequisite.
     */
    // Set used to avoid duplicates.
    private final List<CourseInterface> myNextCourses;

    /**
     * Constructs a new Course with the given name and no direct successors.
     *
     * @param theName the display name of this course; must not be null.
     * @throws IllegalArgumentException if theName is null.
     */
    public Course(final String theName) throws IllegalArgumentException{
        super();

        if (theName == null) {
            throw new IllegalArgumentException("Course name must not be null.");
        }

        myName = theName;
        myNextCourses = new ArrayList<>();
    }

    /**
     * Returns the name of this course.
     *
     * @return the course name as a string.
     */
    public String getName() {
        return myName;
    }

    /**
     * Returns a copy of the list of courses that directly follow.
     *
     * @return a new List containing the direct successors of this course.
     */
    public List<CourseInterface> getNextCourses() {
        // Returns a copy to preserve encapsulation.
        return new ArrayList<CourseInterface>(myNextCourses);
    }

    /**
     * Adds a direct successor to this course if it is not already present.
     *
     * @param theCourse the course to add as a direct successor; mut not be null.
     * @throws IllegalArgumentException if theCourse is null.
     */
    public void addNextCourse(final CourseInterface theCourse) throws IllegalArgumentException {
        if (theCourse == null) {
            throw new IllegalArgumentException("Course must not be bull.");
        }
        if (!myNextCourses.contains(theCourse)) {
            myNextCourses.add(theCourse);
        }
    }
    /**
     * Returns a human-readable representation of this course and its direct successors.
     *
     * @return a formatted string showing this course and its direct successors.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(myName).append(" -> [");

        for (int i = 0; i < myNextCourses.size(); i++) {
            sb.append(myNextCourses.get(i).getName());
            if (i < myNextCourses.size() - 1) {
                sb.append(", ");
            }
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Compares this course to another object for equality.
     * Two courses are equal if and only if their names and successors lists are both equal.
     *
     * @param theObj the object to compare against.
     * @return true if theObj is a course with the same name and successors; false otherwise.
     */
    @Override
    public boolean equals(final Object theObj) {
        if (this == theObj) {
            return true;
        }
        if (theObj == null || getClass() != theObj.getClass()) {
            return false;
        }
        final Course other = (Course) theObj;
        return java.util.Objects.equals(myName, other.myName)
                && java.util.Objects.equals(myNextCourses, other.myNextCourses);
    }

    /**
     * Returns a hash code based on its name and successor list.
     *
     * @return the hash code derived from this course's name and successors.
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(myName, myNextCourses);
    }
}