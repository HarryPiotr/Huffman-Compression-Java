package com;

import java.util.Iterator;

public class ReplacementNode extends Node {

    private Node left;
    private Node right;

    public ReplacementNode(Node n1, Node n2, byte c) {
        super(c);
        this.setOccurrences(n1.getOccurrences() + n2.getOccurrences());
        this.setSymbol(c);
        this.left = n1;
        this.right = n2;
        n1.setParent(this);
        n2.setParent(this);
    }

    public Node getLeft() {
        return left;
    }

    public void setLeft(Node left) {
        this.left = left;
    }

    public Node getRight() {
        return right;
    }

    public void setRight(Node right) {
        this.right = right;
    }

    public void print(String prefix, String childrenPrefix) {
        System.out.println(prefix + "" + this.getSymbol() + "(" + this.getOccurrences() + ")");
        if(right instanceof ReplacementNode) {
            ((ReplacementNode)right).print(childrenPrefix + "├── ", childrenPrefix + "│   ");
        }
        else right.print(childrenPrefix + "├──");
        if(left instanceof ReplacementNode) {
            ((ReplacementNode)left).print(childrenPrefix + "└── ", childrenPrefix + "    ");
        }
        else left.print(childrenPrefix + "└──");
    }
}
