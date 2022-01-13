package org.joker.java.call.hierarchy;

import java.io.IOException;

public class Main {

    private static final String ROOT_PATH = System.getProperty("user.dir");

    public static void main(String[] args) throws IOException {
        Config config = new Config();
        // set your project path
        config.setProjectPath("/Users/yuguangyuan/code/csc/eagle-parent");
        // add your project dependency project path
        // config.addDependencyProjectPath("");
        // add your project dependency jar path
        // config.addDependencyJarPath("");

        CallHierarchy callHierarchy = new CallHierarchy(config);

        String packageName = "com.linkstec.raptor.eagle.frap.service";
        String javaName = "DoubleRecordService";
        String method = "saveCustomSno";
        callHierarchy.printParseMethodRecursion(packageName, javaName, method);
    }

}
