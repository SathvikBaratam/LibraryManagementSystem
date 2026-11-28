import java.io.IOException;
import java.util.*;
import library.items.*;
import library.members.*;
import library.core.*;
import library.exceptions.*;

public class Main {
    public static void main(String[] args) throws IOException {
        Library lib = new Library();
        Scanner sc = new Scanner(System.in);
        while (sc.hasNextLine()) {
            String line = sc.nextLine().trim();
            if (line.isEmpty())
                continue;
            try {
                process(lib, line);

            } catch (LibraryException e) {
                System.out.println("ERROR " + e.getMessage());
            } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
                System.out.println("ERROR invalid input");
            }

        }
        sc.close();
    }

    private static void process(Library lib, String line) throws LibraryException {
        String[] f = line.split("\\|", -1);
        switch (f[0]) {
            case "ADD_BOOK":
                lib.addItem(new Book(f[1], f[2], f[3]));
                System.out.println("OK");
                break;
            case "ADD_REFERENCE":
                lib.addItem(new ReferenceBook(f[1], f[2], f[3]));
                System.out.println("OK");
                break;
            case "ADD_MAGAZINE":
                lib.addItem(new Magazine(f[1], f[2], f[3]));
                System.out.println("OK");
                break;
            case "ADD_MEMBER":
                switch (f[3]) {
                    case "STUDENT":
                        lib.addMember(new Student(f[1], f[2]));
                        break;
                    case "STAFF":
                        lib.addMember(new Staff(f[1], f[2]));
                        break;
                    case "FACULTY":
                        lib.addMember(new Faculty(f[1], f[2]));
                        break;
                    default:
                        throw new LibraryException("unknown member type " + f[3]);
                }
                System.out.println("OK");
                break;
            case "BORROW":
                System.out.println("OK due on day " + lib.borrow(f[1], f[2]));
                break;

            case "RETURN":
                System.out.println(lib.returnItem(f[1]));
                break;

            case "ADVANCE":
                int days = Integer.parseInt(f[1]);
                if (days < 0)
                    throw new IllegalArgumentException();
                System.out.println("OK today is day " + lib.advance(days));
                break;
            case "SEARCH": {
                List<String> rows = new ArrayList<>();
                for (LibraryItem it : lib.search(f[1]))
                    rows.add(it.getId() + "|" + it.getType() + "|" + it.getTitle());
                print(rows);
                break;
            }
            case "LOANS": {
                List<String> rows = new ArrayList<>();
                for (Loan l : lib.loansOf(f[1]))
                    rows.add(l.getItem().getId() + "|" + l.getItem().getTitle() + "|" + l.getDueDay());
                print(rows);
                break;
            }
            case "OVERDUE": {
                List<String> rows = new ArrayList<>();
                for (Loan l : lib.overdue())
                    rows.add(l.getItem().getId() + "|" + l.getMember().getId() + "|"
                            + l.daysLate(lib.getToday()) + "|" + l.fine(lib.getToday()));
                print(rows);
                break;
            }
            case "INVENTORY": {
                List<String> rows = new ArrayList<>();
                for (LibraryItem it : lib.inventory())
                    rows.add(it.getId() + "|" + it.getType() + "|" + lib.statusOf(it));
                print(rows);
                break;
            }
            default:
                throw new LibraryException("unknown command " + f[0]);

        }
    }

    private static void print(List<String> rows) {
        if (rows.isEmpty())
            System.out.println("NONE");
        else
            for (String r : rows)
                System.out.println(r);
    }
}
