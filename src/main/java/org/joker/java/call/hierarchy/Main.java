package org.joker.java.call.hierarchy;

import java.io.IOException;

public class Main {

    private static final String ROOT_PATH = System.getProperty("user.dir");

    public static void main(String[] args) throws IOException {
        Config config = new Config();
        // set your project path
        config.setProjectPath(ROOT_PATH);
        // add your project dependency project path
        // config.addDependencyProjectPath("");
        // add your project dependency jar path
        // config.addDependencyJarPath("");

        CallHierarchy callHierarchy = new CallHierarchy(config);

        String packageName = "org.joker.java.call.hierarchy.test";
        String javaName = "A";
        String method = "methodA1";
        callHierarchy.printParseMethodRecursion(packageName, javaName, method);
    }

}
