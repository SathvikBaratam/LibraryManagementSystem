package members;

public class Student extends Member {
    Student(String id, String name) {
        super(id, name);
    }

    public int getMaxItems() {
        return 3;
    }

    public int getLoanPeriod() {
        return 14;
    }
}