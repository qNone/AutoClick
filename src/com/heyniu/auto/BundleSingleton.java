package com.heyniu.auto;

import android.os.Bundle;

import java.util.HashMap;

class BundleSingleton {

    private static BundleSingleton instance = new BundleSingleton();

    static BundleSingleton getInstance() {
        return instance;
    }

    private BundleSingleton() {
    }

    private HashMap<String, Bundle> arguments = new HashMap<>();

    void put(String key, Bundle value) {
        arguments.put(key, value);
    }

    boolean containsKey(String key) {
        return arguments.containsKey(key);
    }

    Bundle getBundle(String key) {
        return arguments.get(key);
    }

    void clear() {
        arguments.clear();
    }

}
