import java.util.ArrayList;
import java.util.List;

public class Course {
    private final String myName;
    private final List<Course> myNextCourses;

    public Course(String theName) {
        myName = theName;
        myNextCourses = new ArrayList<>();
    }

    public void addNext(Course theNext) {
        myNextCourses.add(theNext);
    }

    public String getName() {
        return myName;
    }

    public List<Course> getNextCourses() {
        return new ArrayList<>(myNextCourses);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
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

    @Override
    public boolean equals(Object theObj) {
        if (this == theObj) {
            return true;
        }
        if (theObj == null || getClass() != theObj.getClass()) {
            return false;
        }
        final Course other = (Course) theObj;
        return java.util.Objects.equals(myName, other.myName);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(myName);
    }
}