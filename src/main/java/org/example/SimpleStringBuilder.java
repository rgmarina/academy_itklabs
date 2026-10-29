package org.example;

import java.util.ArrayDeque;
import java.util.Deque;

public class SimpleStringBuilder {

    private StringBuilder text = new StringBuilder();

    private final Deque<String> history = new ArrayDeque<>();

    public void append(String s) {

        history.push(text.toString());
        text.append(s);
    }

    public void undo() {

        if (!history.isEmpty()) {
            text = new StringBuilder(history.pop());
        }
    }

    @Override
    public String toString() {
        return text.toString();
    }

    public static void main(String[] args) {

        SimpleStringBuilder b = new SimpleStringBuilder();

        b.append("Hello");
        System.out.println(b);

        b.append(", ");
        System.out.println(b);

        b.append("world");
        System.out.println(b);

        b.undo();
        System.out.println(b);

        b.append("AQA!");
        System.out.println(b);

        b.undo();
        System.out.println(b);

        b.undo();
        System.out.println(b);
    }
}
