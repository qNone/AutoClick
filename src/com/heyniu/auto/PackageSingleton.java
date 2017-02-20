package com.heyniu.auto;

class PackageSingleton {

    private static PackageSingleton instance;
    private String pkg;

    private PackageSingleton() {}

    static PackageSingleton getInstance() {
        if (instance == null) {
            synchronized (PackageSingleton.class) {
                if (instance == null) {
                    instance = new PackageSingleton();
                }
            }
        }
        return instance;
    }

    String getPkg() {
        return pkg;
    }

    void setPkg(String pkg) {
        this.pkg = pkg;
    }

}
