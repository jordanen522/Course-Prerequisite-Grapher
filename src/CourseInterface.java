/*
 * CourseInterface.java
 *
 * Personal Project - Spring 2026
 * CoursePrequisiteGrapher
 */
import java.util.List;

/**
 * Defines the contract for the course node.
 * Any class that implements this interface must represent a course that holds a name
 * and a list of direct successor courses.
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
     * Returns a copy of the list of courses that directly follow this one.
     *
     * @return a new list containing the direct successors of this course.
     */
    List<CourseInterface> getNextCourses();
}
