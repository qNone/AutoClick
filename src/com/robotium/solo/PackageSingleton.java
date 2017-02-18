package com.robotium.solo;

class PackageSingleton {

    private static PackageSingleton instance;
    private String pkg;

    private PackageSingleton() {}

    public static PackageSingleton getInstance() {
        if (instance == null) {
            synchronized (PackageSingleton.class) {
                if (instance == null) {
                    instance = new PackageSingleton();
                }
            }
        }
        return instance;
    }

    public String getPkg() {
        return pkg;
    }

    public void setPkg(String pkg) {
        this.pkg = pkg;
    }

}
