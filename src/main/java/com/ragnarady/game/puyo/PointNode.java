package com.ragnarady.game.puyo;

/**
 * Provides way to capture Puyos' combos to store their positions.
 */
class PointNode {
    int x, y;
    PointNode nextnode;
    PointNode prevnode;

    public PointNode(int nX, int nY) {
        x = nX;
        y = nY;
        prevnode = null;
        nextnode = null;
        
    }
    
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPrev(PointNode nNode) {
        prevnode = nNode;
    }

    public PointNode getPrev() {
        return prevnode;
    }
    
    public void setNext(PointNode nNode) {
        nextnode = nNode;
    }

    public PointNode getNext() {
        return nextnode;
    }
}
