package library.items;

public class Book extends LibraryItem implements Borrowable {
    private final String author;

    public Book(String id, String title, String author) {
        super(id, title);
        this.author = author;
    }

    @Override
    public String getType() {
        return "BOOK";
    }

    @Override
    public boolean isBorrowable() {
        return true;
    }

    @Override
    public int getFinePerDay() {
        return 2;
    }

    public String getAuther() {
        return author;
    }

}
