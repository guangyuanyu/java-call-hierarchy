package org.joker.java.call.hierarchy;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.joker.java.call.hierarchy.diff.DiffAdapter;
import org.joker.java.call.hierarchy.diff.FileDiff;
import org.joker.java.call.hierarchy.diff.GitDIffAdapter;
import org.joker.java.call.hierarchy.utils.LambdaUtils;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class Main {

    private static final String ROOT_PATH = System.getProperty("user.dir");

//    private static String sourceDir = "/data/devops/workspace/app-mallcenter/csc108-etrade-licai-backend-cfzh";
//    private static String diffFileName = "/data/devops/workspace/app-mallcenter/csc108-etrade-licai-backend-cfzh/git_diff.txt";

    public static String sourceDir = "/Users/yuguangyuan/code/csc/migrate/git/new/csc108-etrade-licai-backend";
    public static String diffFileName = "/Users/yuguangyuan/Downloads/2-1-5-git-diff.log";
    public static String oldVersion = "V3.9.0";
    public static String newVersion = "";


    public static void main(String[] args) throws IOException, GitAPIException {
        init();
//        parseMethodCall();
//        parseFieldAccess();

//        locateSvnDiff();

        parseControllerDiff();
    }

    public static void analysis(String sourceDir, String diffFile) throws IOException, GitAPIException {
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

    /**
     * javaparser初始化，主要是设置源代码地址
     */
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

    /**
     * 从diff文件，分析代码改动行数，
     * @return
     * @throws IOException
     */
    private static List<DiffLocator.DiffDesc> locateDiff() throws IOException, GitAPIException {

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

    private static void parseControllerDiff() throws IOException, GitAPIException {
        List<DiffLocator.DiffDesc> diffDescs = locateDiff();

        FieldAccessHierarchy fieldAccessHierarchy = new FieldAccessHierarchy(callHierarchy);
        fieldAccessHierarchy.batchPrintFieldsRecursion(LambdaUtils.filter(diffDescs, d -> d.isFieldDiff));
        callHierarchy.batchPrintParseMethodsRecursion(LambdaUtils.filter(diffDescs, d -> !d.isFieldDiff));


//        for (DiffLocator.DiffDesc diffDesc : diffDescs) {
//            if (diffDesc.isFieldDiff) {
//                System.out.println("==================Field: " + diffDesc.fieldDesc.packageName + "." + diffDesc.fieldDesc.className + ":" + diffDesc.fieldDesc.fieldName + "========================");
//                fieldAccessHierarchy.parseFieldAccessRecursion(diffDesc.module, diffDesc.fieldDesc.packageName,
//                        diffDesc.fieldDesc.className, diffDesc.fieldDesc.fieldName);
//            } else {
//                System.out.println("==================Method: " + diffDesc.methodDesc.packageName + "." + diffDesc.methodDesc.className + ":" + diffDesc.methodDesc.methodName + "========================");
//                callHierarchy.printParseMethodRecursion(diffDesc.module, diffDesc.methodDesc.packageName,
//                        diffDesc.methodDesc.className, diffDesc.methodDesc.methodName);

//                callHierarchy.batchParseMethodsRecursion(diffDescs);
//            }
//        }
    }
}
