/*
 * Course.java
 *
 * Personal Project - Spring 2026
 * Course-Prerequisite-Grapher
 */
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

/**
 * A node in a course prerequisite graph. Each course has an immutable name and a
 * mutable set of direct successor courses.
 *
 * @author Jordan Eng
 * @version 10/7/2026
 */
public class Course {

    /* --------------------STATIC ZONE-------------------- */

    /**
     * Checks whether two courses have the same name.
     *
     * @param theFirst the first course.
     * @param theSecond the second course.
     * @return true if both are the same reference or have equal names.
     */
    public static boolean isSameName(final Course theFirst, final Course theSecond) {
        if (theFirst == theSecond) {
            return true;
        }

        if (theFirst == null || theSecond == null) {
            return false;
        }

        return theFirst.myName.equals(theSecond.myName);
    }

    /**
     * Checks whether two courses have the same set of successors.
     *
     * @param theFirst the first course.
     * @param theSecond the second course.
     * @return true if both are the same reference or both of their successors are equal.
     */
    public static boolean isSameSuccessors(final Course theFirst, final Course theSecond) {
        if (theFirst == theSecond) {
            return true;
        }

        if (theFirst == null || theSecond == null) {
            return false;
        }

        return theFirst.myNextCourses.equals(theSecond.myNextCourses);
    }

    /**
     * Checks whether two courses have both the same name and same set of successors.
     *
     * @param theFirst the first course.
     * @param theSecond the second course.
     * @return true if both are the same reference or both have the same name and set of successors.
     */
    public static boolean isSameNameAndSuccessors(final Course theFirst, final Course theSecond) {
        return isSameName(theFirst, theSecond) && isSameSuccessors(theFirst, theSecond);
    }

    /* --------------------STATIC ZONE-------------------- */

    /** The name of this course */
    private final String myName;
    /** The direct successors of this course. */
    private final Set<Course> myNextCourses;

    /**
     * Constructs a course with the given name.
     *
     * @param theName the course name; must not be null.
     * @throws IllegalArgumentException if theName is null.
     */
    public Course(final String theName) throws IllegalArgumentException {
        super();

        if (theName == null) {
            throw new IllegalArgumentException("Course name cannot be null.");
        }

        myName = theName;
        myNextCourses = new HashSet<>();
    }

    /**
     * Returns the name of this course.
     *
     * @return the course name.
     */
    public String getName() {
        return myName;
    }

    /**
     * Returns an unmodifiable live view of the course's direct successors.
     *
     * @return an unmodifiable live view of the course's direct successors.
     */
    public Set<Course> getNextCourses() {
        return Collections.unmodifiableSet(myNextCourses);
    }

    /**
     * Adds a direct successor to this course if it is not already present.
     *
     * @param theCourse the course to add as a direct successor; must not be null.
     * @return true if the course was added.
     * @throws IllegalArgumentException if theCourse is null.
     */
    public boolean addNextCourse(final Course theCourse) throws IllegalArgumentException {
        if (theCourse == null) {
            throw new IllegalArgumentException("Course cannot be null.");
        }

        return myNextCourses.add(theCourse);
    }

    /**
     * Removes a direct successor from this course.
     *
     * @param theCourse the course to remove.
     * @return true if the course was a direct successor.
     */
    public boolean removeNextCourse(final Course theCourse) {
        return myNextCourses.remove(theCourse);
    }

    /**
     * Returns a human-readable representation of this course and its direct successors.
     *
     * @return a formatted string showing this course and its direct successors.
     */
    @Override
    public String toString() {
        final StringJoiner joiner = new StringJoiner(", ", myName + " -> [", "]");

        for (final Course next : myNextCourses) {
            joiner.add(next.getName());
        }

        return joiner.toString();
    }

    /**
     * Compares this course to another object by identity.
     *
     * @param theObj the object to compare against.
     * @return true if theObj is this exact instance.
     */
    @Override
    public boolean equals(final Object theObj) {
        return this == theObj;
    }

    /**
     * Returns the identity hash code of this course.
     *
     * @return the identity hash code of this course.
     */
    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }
}
