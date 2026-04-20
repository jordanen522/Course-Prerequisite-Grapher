/*
 * Course.java
 *
 * Personal Project - Spring 2026
 * CoursePrequisiteModel
 */


import java.util.HashSet;
import java.util.Set;

/**
 * Represents a single course node in a Directed Acyclic Graph (DAG).
 * Each course holds its name and set of courses that directly follow it.
 *
 * @author Jordan Eng
 * @version 4/20/2026
 */
public class Course {

    /**
     * The name of this course.
     */
    private final String myName;
    /**
     * The immutable set of courses that have this course as a direct prerequisite.
     */
    // Set used to avoid duplicates.
    private final Set<Course> myNextCourses;

    /**
     * Constructs a new Course with the given name and its complete set of direct successors.
     *
     * @param theName the display name of this course; must not be null.
     * @param theNextCourses a set of the courses that follow this one; must not be null.
     * @throws IllegalArgumentException if theName or theNextCourses is null.
     */
    public Course(String theName, Set<Course> theNextCourses) {
        if (theName == null) {
            throw new IllegalArgumentException("Course name must not be null.");
        }
        if (theNextCourses == null) {
            throw new IllegalArgumentException("Direct successor set must not be null.");
        }

        myName = theName;
        myNextCourses = new HashSet<>(theNextCourses);
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
     * Returns a copy of the set of courses that directly follow.
     *
     * @return a new Set containing the direct successors of this course.
     */
    public Set<Course> getNextCourses() {
        // Returns a copy to preserve encapsulation.
        return new HashSet<>(myNextCourses);
    }

    /**
     * Returns a human-readable representation of this course and its direct successors.
     *
     * @return a formatted string showing this course and its direct successors.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(myName).append(" -> [");

        // Iterate over the set and join with commas.
        int i = 0;
        for (Course course : myNextCourses) {
            sb.append(course.getName());

            // Append a comma after every element except the last.
            if (i < myNextCourses.size() - 1) {
                sb.append(", ");
            }
            i++;
        }

        sb.append("]");
        return sb.toString();
    }

    /**
     * Compares this course to another object for equality.
     * Two courses are equal if and only if their names and successors sets are both equal.
     *
     * @param theObj the object to compare against.
     * @return true if theObj is a course with the same name and successors; false otherwise.
     */
    @Override
    public boolean equals(Object theObj) {
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
     * Returns a hash code based on its name and successor set.
     *
     * @return the hash code derived from this course's name and successors.
     */
    @Override
    public int hashCode() {
        return java.util.Objects.hash(myName, myNextCourses);
    }
}