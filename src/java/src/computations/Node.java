package computations;

public class Node {
    private int nodeValue;//value of node
    private int next;// the next value pointed by the node for exit nodes
    private final Node[] sides;//the next nodes on each side of the hexagon
    private boolean isExit;//set to true if the node is an exit node
    private boolean atom;//set to true if the node contains an atom

    public Node(int value, int s) {//constructor
        this.nodeValue = value;
        this.sides = new Node[s];//creates an array list with specified number of sides
        this.atom = false;//set default
    }

    public int getNodeValue() {
        return nodeValue;
    }

    public void setNodeValue(int nodeValue) {
        this.nodeValue = nodeValue;
    }

    public void setSides(int i, Node v) { // stores the value of the next side from 0-6 to v which is used for deflecting and reflecting
        this.sides[i] = v;
    }

    public Node getSides(int i) {
        return this.sides[i];
    }//returns the next node stored by the array

    public void setexit(boolean b) {
        this.isExit = b;
    }//sets if the node is a exit node or a regular node

    public boolean isexit() {
        return this.isExit;
    }//checks if the node is an exit node or not.

    public void setnext(int n) {
        this.next = n;
    }//sets the next value for exit nodes to n

    public int getnext() {
        return this.next;
    }//returns the next value of exit nodes

    public void setatom(boolean b) {
        this.atom = b;
    }//sets true if the node contains the atom

    public boolean hasatom() {
        return this.atom;
    }//returns if the node has the atom or not


}
