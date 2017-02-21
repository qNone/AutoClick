package com.heyniu.auto;

import android.os.Bundle;

import java.util.HashMap;

class BundleSingleton {

    private static BundleSingleton instance;

    private HashMap<String, Bundle> arguments = new HashMap<>();

    private BundleSingleton() {}

    static BundleSingleton getInstance() {
        if (instance == null) {
            synchronized (BundleSingleton.class) {
                if (instance == null) {
                    instance = new BundleSingleton();
                }
            }
        }
        return instance;
    }

    void put(String key, Bundle value) {
        if (arguments == null) {
            synchronized (BundleSingleton.class) {
                if (arguments == null) {
                    arguments = new HashMap<>();
                }
            }
        }
        Log.e(Solo.LOG_TAG, "arguments size:" + arguments.size());
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
