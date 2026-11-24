package core;

import items.*;
import members.*;

public class Loan {
    private LibraryItem item;
    private Member member;
    private int borrowedDay;
    private int dueDay;

    public Loan(LibraryItem item, Member member, int borrowedDay) {
        this.item = item;
        this.member = member;
        this.borrowedDay = borrowedDay;
    }

    public int getDueDay() {
        return borrowedDay + member.getLoanPeriod();
    }

    public LibraryItem getItem() {
        return item;
    }

    public int getBorrowedDay() {
        return borrowedDay;
    }

    public Member getMember() {
        return member;
    }

    public int daysLate(int today) {
        return Math.max(0, today - dueDay);
    }

    public int fine(int today) {
        if (item instanceof Borrowable) {
            Borrowable b = (Borrowable) item;
            return daysLate(today) * (b.getFinePerDay());
        }
        return 0;
    }

    public boolean isOverDue(int today) {
        if (today > dueDay) {
            return true;
        }
        return false;
    }

}