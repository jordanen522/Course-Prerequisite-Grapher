/*
 * CourseInterface.java
 *
 * Personal Project - Spring 2026
 * CoursePrequisiteGrapher
 */
import java.util.Set;

/**
 * Defines the contract for the course node.
 * ANy class that implements this interface must represent a course that holds a name
 * and a set of direct successor courses.
 *
 * @author Jordan Eng
 * @version 4/20/2026
 */
public interface CourseInterface {

    /**
     * Returns the name of this course.
     *
     * @return the course name as a String.
     */
    String getName();

    /**
     * Returns a copy of the set of courses that directly follow this one.
     *
     * @return a new Set containing the direct successors of this course.
     */
    Set<CourseInterface> getNextCourses();
}
