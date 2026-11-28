package library.core;

import java.util.*;

import library.exceptions.BorrowLimitExceededException;
import library.exceptions.ItemNotAvailableException;
import library.exceptions.ItemNotFoundException;
import library.exceptions.ItemNotOnLoanException;
import library.exceptions.LibraryException;
import library.exceptions.MemberNotFoundException;
import library.items.*;
import library.members.*;

public class Library {
    private int today;
    private Map<String, LibraryItem> items;
    private Map<String, Member> members;
    private Map<String, Loan> loans;
    private Map<String, Queue<Member>> reservation;

    public Library() {
        items = new HashMap<>();
        members = new HashMap<>();
        loans = new HashMap<>();
        reservation = new HashMap<>();
        today = 0;
    }

    public void addItem(LibraryItem item) {
        items.put(item.getId(), item);
    }

    public void addMember(Member member) {
        members.put(member.getId(), member);
    }

    public LibraryItem findItem(String id) throws ItemNotFoundException {
        LibraryItem it = items.get(id);
        if (it == null) {
            String s = "item " + id + " not found";
            throw new ItemNotFoundException(s);
        }
        return it;
    }

    public Member findMember(String id) throws MemberNotFoundException {
        Member m = members.get(id);
        if (m == null) {
            String s = "member " + id + " not found";
            throw new MemberNotFoundException(s);
        }
        return m;
    }

    public boolean isItemBorrowed(LibraryItem item) {
        return loans.containsKey(item.getId());
    }

    private int countLoans(String memberId) {
        int n = 0;
        for (Loan l : loans.values())
            if (l.getMember().getId().equals(memberId))
                n++;
        return n;
    }

    public int borrow(String memberId, String itemId) throws LibraryException {
        try {
            LibraryItem item = findItem(itemId);
            Member member = findMember(memberId);
            if (!item.isBorrowable())
                throw new ItemNotAvailableException(itemId + " is not borrowable");
            if (loans.containsKey(itemId))
                throw new ItemNotAvailableException(itemId + " is on loan");
            if (countLoans(memberId) >= member.getMaxItems()) {
                String s = memberId + " has reached the limit of " + member.getMaxItems();
                throw new BorrowLimitExceededException(s);
            }

            Loan ln = new Loan(item, member, today);
            loans.put(item.getId(), ln);
            int due = today + member.getLoanPeriod();
            return due;

        } catch (MemberNotFoundException e) {
            throw e;
        } catch (ItemNotFoundException e) {
            throw e;
        } catch (BorrowLimitExceededException e) {
            throw e;
        } catch (ItemNotAvailableException e) {
            throw e;
        }

    }

    public String returnItem(String itemId) throws LibraryException {
        try {
            LibraryItem it = findItem(itemId);
            Loan loan = loans.remove(itemId);
            if (loan == null) {
                String s = it.getId() + " is not on loan";
                throw new ItemNotOnLoanException(s);
            }
            int fine = loan.fine(today);
            return "OK fine " + fine;

        } catch (ItemNotFoundException e) {
            throw e;
        } catch (ItemNotOnLoanException e) {
            throw e;
        }
    }

    public List<Loan> overdue() {
        List<Loan> ovdue = new ArrayList<>();
        for (Loan e : loans.values()) {
            if (e.isOverDue(today)) {
                ovdue.add(e);
            }
        }
        ovdue.sort(Comparator
                .comparing((Loan l) -> l.daysLate(today)).reversed()
                .thenComparing(l -> l.getItem().getId()));
        return ovdue;
    }

    public List<LibraryItem> inventory() {
        List<LibraryItem> inv = new ArrayList<>(items.values());
        inv.sort(Comparator.comparing(LibraryItem::getId));
        return inv;
    }

    public List<Loan> loansOf(String memberId) {
        List<Loan> result = new ArrayList<>();
        for (Loan l : loans.values())
            if (l.getMember().getId().equals(memberId)) {
                result.add(l);
            }
        result.sort(Comparator.comparingInt(Loan::getDueDay)
                .thenComparing(l -> l.getItem().getId()));
        return result;
    }

    public List<LibraryItem> search(String text) {
        String needle = text.toLowerCase();
        List<LibraryItem> result = new ArrayList<>();
        for (LibraryItem it : items.values())
            if (it.getTitle().toLowerCase().contains(needle))
                result.add(it);
        result.sort(Comparator.comparing(LibraryItem::getTitle)
                .thenComparing(LibraryItem::getId));
        return result;
    }

    public int advance(int days) {
        today += days;
        return today;
    }

    public int getToday() {
        return today;
    }

    public String statusOf(LibraryItem item) {
        if (!item.isBorrowable())
            return "NOT_BORROWABLE";

        Loan loan = loans.get(item.getId());
        if (loan == null)
            return "AVAILABLE";

        return "ON_LOAN|" + loan.getMember().getId();
    }

}
