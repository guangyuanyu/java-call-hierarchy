package org.joker.java.call.hierarchy;

import org.apache.commons.io.IOUtils;
import org.joker.java.call.hierarchy.diff.DiffAdapter;
import org.joker.java.call.hierarchy.diff.FileDiff;
import org.joker.java.call.hierarchy.diff.GitDIffAdapter;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Main {

    private static final String ROOT_PATH = System.getProperty("user.dir");

//    private static String sourceDir = "/data/devops/workspace/app-mallcenter/csc108-etrade-licai-backend-cfzh";
//    private static String diffFileName = "/data/devops/workspace/app-mallcenter/csc108-etrade-licai-backend-cfzh/git_diff.txt";

    public static String sourceDir = "/Users/yuguangyuan/code/csc/csc-tdx-licai-cfzh";
    public static String diffFileName = "/Users/yuguangyuan/Downloads/2-1-5-git-diff.log";
    public static String oldVersion = "";
    public static String newVersion = "";


    public static void main(String[] args) throws IOException {
        init();
//        parseMethodCall();
//        parseFieldAccess();

//        locateSvnDiff();

        parseControllerDiff();
    }

    public static void analysis(String sourceDir, String diffFile) throws IOException {
        Main.sourceDir = sourceDir;
        Main.diffFileName = diffFile;
        init();

        parseControllerDiff();
    }


    private static void parseMethodCall() throws IOException {
            Config config = new Config();
            // set your project path
            config.setProjectPath(sourceDir);
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

            callHierarchy.printParseMethodRecursion("gmCrm", packageName, javaName, method);
    }

    private static void parseFieldAccess() throws IOException {
        Config config = new Config();
        // set your project path
        config.setProjectPath(diffFileName);
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

        fieldAccessHierarchy.printFieldAccessRecursion("gmCrm", packageName, javaName, field);
    }

    private static void init() {
        Config config = new Config();
        config.setProjectPath(sourceDir);
        try {
            callHierarchy = new CallHierarchy(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static CallHierarchy callHierarchy;

    private static List<DiffLocator.DiffDesc> locateSvnDiff() throws IOException {

        String filename = diffFileName;
        List<String> lines = IOUtils.readLines(new FileReader(filename));
//        DiffAdapter diffAdapter = new SvnDiffAdapter();
        DiffAdapter diffAdapter = new GitDIffAdapter();

        List<FileDiff> fileDiffs = diffAdapter.toDiff(lines);

        DiffLocator diffLocator = new DiffLocator(callHierarchy);
        List<DiffLocator.DiffDesc> diffDescs = diffLocator.locate(fileDiffs);
        System.out.println(diffDescs);

        return diffDescs;
    }

    private static void parseControllerDiff() throws IOException {
        List<DiffLocator.DiffDesc> diffDescs = locateSvnDiff();

        FieldAccessHierarchy fieldAccessHierarchy = new FieldAccessHierarchy(callHierarchy);
        for (DiffLocator.DiffDesc diffDesc : diffDescs) {
            if (diffDesc.isFieldDiff) {
                System.out.println("==================Field: " + diffDesc.fieldDesc.packageName + "." + diffDesc.fieldDesc.className + ":" + diffDesc.fieldDesc.fieldName + "========================");
                fieldAccessHierarchy.parseFieldAccessRecursion(diffDesc.module, diffDesc.fieldDesc.packageName,
                        diffDesc.fieldDesc.className, diffDesc.fieldDesc.fieldName);
            } else {
                System.out.println("==================Method: " + diffDesc.methodDesc.packageName + "." + diffDesc.methodDesc.className + ":" + diffDesc.methodDesc.methodName + "========================");
                callHierarchy.printParseMethodRecursion(diffDesc.module, diffDesc.methodDesc.packageName,
                        diffDesc.methodDesc.className, diffDesc.methodDesc.methodName);
            }
        }
    }
}
