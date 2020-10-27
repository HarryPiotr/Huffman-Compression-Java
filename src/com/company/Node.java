package com.company;

import java.util.Comparator;

public class Node {

    private Node parent;
    private long occurrences = 0;
    private byte symbol;
    private String codingSequence;

    public Node(byte c) {
        setOccurrences(1);
        setSymbol(c);
    }

    public long getOccurrences() {
        return occurrences;
    }

    public void setOccurrences(long occurences) {
        this.occurrences = occurences;
    }

    public void incrementOccurrences() {
        occurrences++;
    }

    public byte getSymbol() {
        return symbol;
    }

    public void setSymbol(byte symbol) {
        this.symbol = symbol;
    }

    public String toString() {
        return "" + symbol + " (" + occurrences + ") [" + this.getCodingSequence() + "]";
    }

    public static Comparator<Node> NodeOccurancesComparator = new Comparator<Node>() {

        public int compare(Node n1, Node n2) {
            return n1.compareTo(n2);
        }
    };

    public static Comparator<Node> NodeOccurancesComparatorDescending = new Comparator<Node>() {

        public int compare(Node n1, Node n2) {
            return n1.compareTo(n2);
        }
    };

    public int compareTo(Node n) {
        return this.occurrences < n.occurrences ? -1 : this.occurrences == n.occurrences ? 0 : 1;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public void print(String prefix) {
        System.out.println(prefix + "" + this.getSymbol() + "(" + this.getOccurrences() + ")");
    }

    public String getCodingSequence() {
        return codingSequence;
    }

    public void setCodingSequence(String codingSequence) {
        this.codingSequence = codingSequence;
    }
}
