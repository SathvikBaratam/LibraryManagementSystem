package library.core;

import java.util.*;
import library.exceptions.*;
import library.items.*;
import library.members.*;

public class Library {
    private int today;
    private Map<String, LibraryItem> items;
    private Map<String, Member> members;
    private Map<String, Loan> loans; // item ID to loan
    private Map<String, Queue<Member>> reservation; // item ID to members waiting, in FIFO order

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
        LibraryItem item = findItem(itemId);
        Member member = findMember(memberId);

        if (!item.isBorrowable())
            throw new ItemNotAvailableException(itemId + " is not borrowable");
        if (loans.containsKey(itemId))
            throw new ItemNotAvailableException(itemId + " is on loan");
        if (countLoans(memberId) >= member.getMaxItems()) {
            String message = memberId + " has reached the limit of " + member.getMaxItems();
            throw new BorrowLimitExceededException(message);
        }

        // A waiting member has priority when the item becomes available.
        Queue<Member> waiting = reservation.get(itemId);
        if (waiting != null && !waiting.isEmpty()
                && !waiting.peek().getId().equals(memberId)) {
            throw new ItemNotAvailableException(itemId + " is reserved for " + waiting.peek().getId());
        }

        Loan loan = new Loan(item, member, today);
        loans.put(itemId, loan);
        if (waiting != null && !waiting.isEmpty()) {
            waiting.remove();
            if (waiting.isEmpty())
                reservation.remove(itemId);
        }
        return loan.getDueDay();
    }

    public void reserve(String memberId, String itemId) throws LibraryException {
        LibraryItem item = findItem(itemId);
        Member member = findMember(memberId);

        if (!item.isBorrowable())
            throw new ReservationNotAllowedException(itemId + " cannot be reserved");
        if (!loans.containsKey(itemId))
            throw new ReservationNotAllowedException(itemId + " is available to borrow");
        if (loans.get(itemId).getMember().getId().equals(memberId))
            throw new ReservationNotAllowedException(memberId + " already has " + itemId);

        Queue<Member> waiting = reservation.computeIfAbsent(itemId, key -> new ArrayDeque<>());
        boolean alreadyWaiting = waiting.stream().anyMatch(m -> m.getId().equals(memberId));
        if (alreadyWaiting)
            throw new ReservationNotAllowedException(memberId + " already reserved " + itemId);
        waiting.add(member);
    }

    public String returnItem(String itemId) throws LibraryException {
        LibraryItem item = findItem(itemId);
        Loan loan = loans.remove(itemId);
        if (loan == null)
            throw new ItemNotOnLoanException(item.getId() + " is not on loan");

        // Keep the reservation queue; borrow() gives its first member priority.
        return "OK fine " + loan.fine(today);
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
