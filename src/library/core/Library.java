package core;

import java.util.*;
import items.*;
import members.*;

public class Library {
    private int today;
    private Map<String, LibraryItem> items;
    private Map<String, Member> member;
    private Map<String, Loan> loans;
    private Map<String, Queue<Member>> reservation;

    public Library() {

    }
}