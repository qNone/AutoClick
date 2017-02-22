package com.heyniu.auto;

class PackageSingleton {
    private static PackageSingleton instance = new PackageSingleton();

    static PackageSingleton getInstance() {
        return instance;
    }

    private PackageSingleton() {
    }

    private String pkg;

    String getPkg() {
        return pkg;
    }

    void setPkg(String pkg) {
        this.pkg = pkg;
    }

}
