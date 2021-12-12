package org.joker.java.call.hierarchy.core;

public enum DefaultProperties {

    OS_NAME(System.getProperty("os.name")),
    JAVA_HOME(System.getProperty("java.home")),
    USER_HOME(System.getProperty("user.home")),
    FILE_SEPARATOR(System.getProperty("file.separator")),
    ;

    private String env;

    DefaultProperties(String env) {
        this.env = env;
    }

    public String getEnv() {
        return env;
    }

    @Override
    public String toString() {
        return getEnv();
    }

}
