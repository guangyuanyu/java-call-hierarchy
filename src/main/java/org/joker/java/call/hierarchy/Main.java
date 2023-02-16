package org.joker.java.call.hierarchy;

import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.maven.shared.utils.StringUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.joker.java.call.hierarchy.core.Hierarchy;
import org.joker.java.call.hierarchy.diff.DiffAdapter;
import org.joker.java.call.hierarchy.diff.FileDiff;
import org.joker.java.call.hierarchy.diff.GitDIffAdapter;
import org.joker.java.call.hierarchy.utils.LambdaUtils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class Main {

    private static final String ROOT_PATH = System.getProperty("user.dir");

//    private static String sourceDir = "/data/devops/workspace/app-mallcenter/csc108-etrade-licai-backend-cfzh";
//    private static String diffFileName = "/data/devops/workspace/app-mallcenter/csc108-etrade-licai-backend-cfzh/git_diff.txt";
//    public static String outputFile = "/data/devops/workspace/output.txt";
//    public static String oldVersion = "V3.9.0";

    // 调试用的，本地配置
//    public static String sourceDir = "/Users/yuguangyuan/code/csc/migrate/git/new/csc108-etrade-licai-backend";
//    public static String diffFileName = "/Users/yuguangyuan/Downloads/2-1-5-git-diff.log";
//    public static String oldVersion = "V3.9.0";

    // 多module代码测试
    public static String sourceDir = "/Users/yuguangyuan/code/csc/migrate/git/new/eagle-maven-online/eagle-parent";
    public static String diffFileName = "/Users/yuguangyuan/code/csc/migrate/git/new/eagle-maven-online/diff.log";
    public static String oldVersion = "master";

    public static String outputFile = "/Users/yuguangyuan/code/github/java-call-hierarchy/target/output.txt";

    public static void main(String[] args) throws IOException, GitAPIException {
        long startTime = System.currentTimeMillis();
        init();
//        parseMethodCall();
//        parseFieldAccess();

//        locateSvnDiff();

//        printControllerDiff();

        print2file(outputFile);

        System.out.println("analyze use time: " + (System.currentTimeMillis() - startTime) + " ms");
    }


    public static void analysis(String sourceDir, String diffFile, String outputFile, String oldVersion) throws IOException, GitAPIException {
        Main.sourceDir = sourceDir;
        Main.diffFileName = diffFile;
        Main.outputFile = outputFile;
        Main.oldVersion = oldVersion;
        init();

        print2file(outputFile);
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
        System.out.println("diff size:" + diffDescs.size());
        System.out.println(diffDescs);

        return diffDescs;
    }

    private static void printControllerDiff() throws IOException, GitAPIException {
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

    private static void print2file(String filename) throws IOException, GitAPIException {
        List<DiffLocator.DiffDesc> diffDescs = locateDiff();

        FieldAccessHierarchy fieldAccessHierarchy = new FieldAccessHierarchy(callHierarchy);

        Set<String> outputs = Sets.newHashSet();
        // 先处理fields
        System.out.println("------ start batch print field call recursion ------");
        List<Hierarchy<ResolvedMethodDeclaration>> hierarchies =
                fieldAccessHierarchy.batchParseFieldsRecursion(LambdaUtils.filter(diffDescs, d -> d.isFieldDiff));
        hierarchies.stream()
                .map(Hierarchy::toStringList)
                .flatMap(Collection::stream)
                .sorted()
                .distinct()
                .forEach(h -> {
                    outputHierarchy(outputs, h);
                });
        System.out.println("------ end batch print field call recursion ------");


        // 再处理method
        System.out.println("------ start batch print method call recursion ------");
        List<Hierarchy<ResolvedMethodDeclaration>> methodHierarchies
                = callHierarchy.batchParseMethodsRecursion(LambdaUtils.filter(diffDescs, d -> !d.isFieldDiff));
        methodHierarchies.stream()
                .map(Hierarchy::toStringList)
                .flatMap(Collection::stream)
                .sorted()
                .distinct()
                .forEach(h -> {
                    // 先 打印完整调用链
                    outputHierarchy(outputs, h);
                });
        System.out.println("------ end batch print field call recursion ------");

        // 文件输出
        try(FileWriter writer = new FileWriter(filename);) {
            IOUtils.writeLines(outputs, "\n", writer);
        } catch (Exception ex) {
            System.err.println("analyze error");
            ex.printStackTrace();
        }
    }

    private static void outputHierarchy(Set<String> outputs, String h) {
        // 先 打印完整调用链
        System.out.println(h);
        String controllerUrl = "";
        if (h.contains(" -> ") && h.toLowerCase().contains("controller")) {
            int i = h.lastIndexOf(" -> ") + 4;
            controllerUrl = h.substring(i);
        }

        if (StringUtils.isBlank(controllerUrl) && h.contains("(url:")) {
            controllerUrl = h;
        }

        if (StringUtils.isNotBlank(controllerUrl)) {
            outputs.add(controllerUrl);
        }
    }
}
