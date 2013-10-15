/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.geometry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author pepijn
 */
public class Node {
    public Node() {
    }

    public Node(Node parent) {
        this.parent = parent;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }
    
    public List<Node> getChildren() {
        return Collections.unmodifiableList(children);
    }
    
    public int getChildCount() {
        return children.size();
    }
    
    public Node getChild(int index) {
        return children.get(index);
    }
    
    public void addChild(Node child) {
        child.setParent(this);
        children.add(child);
    }
    
    public void removeChild(Node child) {
        children.remove(child);
    }
    
    public void removeChild(int index) {
        children.remove(index);
    }
    
    private Node parent;
    private final List<Node> children = new ArrayList<Node>();
}