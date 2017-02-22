package com.heyniu.auto;

import java.util.ArrayList;
import java.util.HashMap;

class ClickedSingleton {

    private static ClickedSingleton instance = new ClickedSingleton();

    public static ClickedSingleton getInstance() {
        return instance;
    }

    private ClickedSingleton() {}

    private HashMap<Integer, int[]> clickedForNative = new HashMap<>();

    void putForNative(Integer key, int[] value) {
        clickedForNative.put(key, value);
    }

    boolean containsKeyForNative(Integer key) {
        return clickedForNative.containsKey(key);
    }

    void clearForNative() {
        clickedForNative.clear();
    }

    private ArrayList<String> clickedForNode = new ArrayList<>();

    void addForNode(String bounds) {
        clickedForNode.add(bounds);
    }

    boolean containsForNode(String bounds) {
        return clickedForNode.contains(bounds);
    }

    void clearForNode() {
        clickedForNode.clear();
    }

}
