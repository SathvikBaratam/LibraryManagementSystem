package members;

public class Faculty extends Member {
    Faculty(String id, String name) {
        super(id, name);
    }

    public int getMaxItems() {
        return 5;
    }

    public int getLoanPeriod() {
        return 30;
    }
}