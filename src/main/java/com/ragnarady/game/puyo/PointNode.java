package main.java.com.ragnarady.game.puyo;

/**
 * Provides way to capture puyos' combos to store their positions.
 */
class PointNode {
    int x, y;
    PointNode nextnode;
    PointNode prevnode;

    public PointNode(int x, int y) {
        this.x = x;
        this.y = y;
        nextnode = null;
        prevnode = null;
    }

    public void setNext(PointNode lnode) {
        nextnode = lnode;
    }

    public PointNode getNext() {
        return nextnode;
    }

    public void setPrev(PointNode lnode) {
        prevnode = lnode;
    }

    public PointNode getPrev() {
        return prevnode;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
