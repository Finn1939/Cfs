package com.finn1939.cfs.ui;

import java.util.ArrayDeque;
import java.util.Deque;

public class NavigationManager {
    private Deque<String> stack = new ArrayDeque<>();

    public void push(String screenId) {
        stack.push(screenId);
    }

    public String pop() {
        return stack.isEmpty() ? null : stack.pop();
    }

    public String current() {
        return stack.isEmpty() ? null : stack.peek();
    }

    public boolean canGoBack() {
        return stack.size() > 1;
    }

    public void back() {
        if (canGoBack()) pop();
    }
}