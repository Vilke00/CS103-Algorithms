package com.metropolitan.cs103pznemanjavilic4050;

import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

// Node class for the doubly linked list
public class CircleNode {
    private Circle circle;
    private Text text;
    private CircleNode prev;
    private CircleNode next;

    public CircleNode(Circle circle, Text text, CircleNode prev) {
        this.circle = circle;
        this.text = text;
        this.prev = prev;
        next = null;
    }

    public CircleNode() {
    }

    public Circle getCircle() {
        return circle;
    }

    public void setCircle(Circle circle) {
        this.circle = circle;
    }

    public Text getText() {
        return text;
    }

    public void setText(Text text) {
        this.text = text;
    }

    public CircleNode getPrev() {
        return prev;
    }

    public void setPrev(CircleNode prev) {
        this.prev = prev;
    }

    public CircleNode getNext() {
        return next;
    }

    public void setNext(CircleNode next) {
        this.next = next;
    }
}