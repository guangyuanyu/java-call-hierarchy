package org.joker.java.call.hierarchy;

import java.io.IOException;

public class Main {

    private static final String ROOT_PATH = System.getProperty("user.dir");

    public static void main(String[] args) throws IOException {
//        parseMethodCall();
        parseFieldAccess();
    }

    private static void parseMethodCall() throws IOException {
            Config config = new Config();
            // set your project path
            config.setProjectPath("/Users/yuguangyuan/code/csc/eagle-parent/zxjt-smjj");
            // add your project dependency project path
            // config.addDependencyProjectPath("");
            // add your project dependency jar path
            // config.addDependencyJarPath("");

            CallHierarchy callHierarchy = new CallHierarchy(config);

//        String packageName = "com.linkstec.raptor.eagle.frap.service";
//        String javaName = "DoubleRecordService";
//        String method = "saveCustomSno";

            String packageName = "com.linkstec.raptor.eagle.frap.support";
            String javaName = "GateControlUtil";
            String method = "getGateCategory";

            callHierarchy.printParseMethodRecursion(packageName, javaName, method);
    }

    private static void parseFieldAccess() throws IOException {
        Config config = new Config();
        // set your project path
        config.setProjectPath("/Users/yuguangyuan/code/csc/eagle-parent/zxjt-smjj");
        // add your project dependency project path
        // config.addDependencyProjectPath("");
        // add your project dependency jar path
        // config.addDependencyJarPath("");

        CallHierarchy callHierarchy = new CallHierarchy(config);
        FieldAccessHierarchy fieldAccessHierarchy = new FieldAccessHierarchy(callHierarchy);

//        String packageName = "com.linkstec.raptor.eagle.frap.service";
//        String javaName = "DoubleRecordService";
//        String method = "saveCustomSno";

        String packageName = "com.linkstec.raptor.eagle.frap.constant";
        String javaName = "GAPConstant";
        String field = "BUSITYPE_DOUBLE_RECORD";

        fieldAccessHierarchy.printFieldAccessRecursion(packageName, javaName, field);
    }

    private static void locateSvnDiff() {
        String s = "";
    }
}
