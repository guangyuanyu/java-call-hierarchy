package org.joker.java.call.hierarchy;

import org.apache.commons.io.IOUtils;
import org.joker.java.call.hierarchy.diff.DiffAdapter;
import org.joker.java.call.hierarchy.diff.FileDiff;
import org.joker.java.call.hierarchy.diff.SvnDiffAdapter;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

public class Main {

    private static final String ROOT_PATH = System.getProperty("user.dir");

    public static void main(String[] args) throws IOException {
        init();
//        parseMethodCall();
//        parseFieldAccess();

//        locateSvnDiff();

        parseControllerDiff();
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

    private static void init() {
        Config config = new Config();
        config.setProjectPath("/Users/yuguangyuan/code/csc/eagle-parent/zxjt-gmCrm");
        try {
            callHierarchy = new CallHierarchy(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static CallHierarchy callHierarchy;

    private static List<DiffLocator.DiffDesc> locateSvnDiff() throws IOException {
        String s = "Index: src/main/java/com/linkstec/raptor/eagle/frap/service/KidmService.java\n" +
                "===================================================================\n" +
                "--- src/main/java/com/linkstec/raptor/eagle/frap/service/KidmService.java\t(版本 347384)\n" +
                "+++ src/main/java/com/linkstec/raptor/eagle/frap/service/KidmService.java\t(版本 345922)\n" +
                "@@ -1217,8 +1217,8 @@\n" +
                "\n" +
                "         try {\n" +
                "             if (StringUtils.isNotBlank(ELE_SECRET_KEY)) {\n" +
                "-                String secretKey = ELE_SECRET_KEY.replace(\"{0}\", sdf.get().format(System.currentTimeMillis()));\n" +
                "-                DES secDes = new DES(secretKey);\n" +
                "+                ELE_SECRET_KEY = ELE_SECRET_KEY.replace(\"{0}\", sdf.get().format(System.currentTimeMillis()));\n" +
                "+                DES secDes = new DES(ELE_SECRET_KEY);\n" +
                "                 inModel.setPhoneNumber(secDes.decryptByMobile(inModel.getPhoneNumber()));    // 手机号按规则解密\n" +
                "             }\n" +
                "\n" +
                "@@ -1551,8 +1551,8 @@\n" +
                "\n" +
                "         try {\n" +
                "             if (StringUtils.isNotBlank(ELE_SECRET_KEY)) {\n" +
                "-                String secretKey = ELE_SECRET_KEY.replace(\"{0}\", sdf.get().format(System.currentTimeMillis()));\n" +
                "-                DES secDes = new DES(secretKey);\n" +
                "+                ELE_SECRET_KEY = ELE_SECRET_KEY.replace(\"{0}\", sdf.get().format(System.currentTimeMillis()));\n" +
                "+                DES secDes = new DES(ELE_SECRET_KEY);\n" +
                "                 inModel.setPhoneNumber(secDes.decryptByMobile(inModel.getPhoneNumber()));    // 手机号按规则解密\n" +
                "             }\n" +
                "             // 业务办理\n" +
                "Index: src/main/java/com/linkstec/raptor/eagle/frap/service/LDSFOnlineHall3rdService.java\n" +
                "===================================================================\n" +
                "--- src/main/java/com/linkstec/raptor/eagle/frap/service/LDSFOnlineHall3rdService.java\t(版本 347384)\n" +
                "+++ src/main/java/com/linkstec/raptor/eagle/frap/service/LDSFOnlineHall3rdService.java\t(版本 345922)\n" +
                "@@ -44,9 +44,9 @@\n" +
                "         String retPwd = pwd;\n" +
                "         if (StringUtils.isNotBlank(retPwd)) {\n" +
                "             if (StringUtils.isNotBlank(ELE_SECRET_KEY)) {\n" +
                "-                String secretKey = ELE_SECRET_KEY.replace(\"{0}\", EagleCommonUtil.getNextDay(EagleConstant.NUMBER_0));\n" +
                "+                ELE_SECRET_KEY = ELE_SECRET_KEY.replace(\"{0}\", EagleCommonUtil.getNextDay(EagleConstant.NUMBER_0));\n" +
                "                 try {\n" +
                "-                    DES secDes = new DES(secretKey);\n" +
                "+                    DES secDes = new DES(ELE_SECRET_KEY);\n" +
                "                     retPwd = secDes.decrypt(pwd);\n" +
                "                 } catch (Exception e) {\n" +
                "                     ;    // nothing do it\n" +
                "Index: src/main/java/com/linkstec/raptor/eagle/frap/service/PortfolioService.java\n" +
                "===================================================================\n" +
                "--- src/main/java/com/linkstec/raptor/eagle/frap/service/PortfolioService.java\t(版本 347384)\n" +
                "+++ src/main/java/com/linkstec/raptor/eagle/frap/service/PortfolioService.java\t(版本 345922)\n" +
                "@@ -195,22 +195,25 @@\n" +
                "      */\n" +
                "     // @Cacheable(value=\"userCache\", key=\"#inModel.id\")\n" +
                "     public Map<String, Object> portfolioDetail(ZhxqModel inModel, Map<String, String> userMap,HttpServletRequest request) throws Exception {\n" +
                "-        // 根据组合编码获取组合产品详情\n" +
                "-        return getPortfolioDetail(inModel, userMap, request);\n" +
                "+// update 20211214 by LKG 【需求-2021-1467】基金投顾增加周报、UI优化等需求 ---start\n" +
                "+        // 根据组合编码获取组合产品详情数据，返回数据不处理\n" +
                "+        return getPortfolioDetailData(inModel, userMap, request);\n" +
                "+// update 20211214 by LKG 【需求-2021-1467】基金投顾增加周报、UI优化等需求 ---end\n" +
                "     }\n" +
                "\n" +
                "     /**\n" +
                "-     * 根据组合编码获取组合产品详情\n" +
                "-     * @param inModel 入参\n" +
                "-     * @param userMap 用户情报\n" +
                "-     * @param request 请求\n" +
                "-     * @return Map<String, Object>\n" +
                "-     * @throws Exception 异常\n" +
                "+     * 根据组合编码获取组合产品详情数据\n" +
                "+     * @param inModel\n" +
                "+     * @param userMap\n" +
                "+     * @param request\n" +
                "+     * @return\n" +
                "+     * @throws Exception\n" +
                "      */\n" +
                "     @SuppressWarnings(\"unchecked\")\n" +
                "-    private Map<String, Object> getPortfolioDetail(ZhxqModel inModel, Map<String, String> userMap, HttpServletRequest request) throws Exception {\n" +
                "-        // 返回结果集\n" +
                "-        Map<String, Object> retMap = new HashMap<String ,Object>();\n" +
                "+    public Map<String,Object> getPortfolioDetailData(ZhxqModel inModel, Map<String, String> userMap, HttpServletRequest request) throws Exception{\n" +
                "+        // 返回数据\n" +
                "+        Map<String, Object> retMap = new HashMap<>();\n" +
                "+\n" +
                "         // 公共参数-致胜\n" +
                "         Map<String, String> params = GateControlUtil.setFinanceCommonParams(userMap, request);\n" +
                "         // 组合编码\n" +
                "@@ -227,13 +230,28 @@\n" +
                "                 throw e;\n" +
                "             }\n" +
                "         }\n" +
                "-        Map<String, Object> baseDataMap = new HashMap<>();\n" +
                "         if (null != retObj) {\n" +
                "             DataLst dataObj = (DataLst) retObj.get(\"resLst\");\n" +
                "             if (null != dataObj) {\n" +
                "-                baseDataMap = dataObj.getBaseData();\n" +
                "+                retMap = dataObj.getBaseData();\n" +
                "             }\n" +
                "         }\n" +
                "+        return retMap;\n" +
                "+    }\n" +
                "+\n" +
                "+    /**\n" +
                "+     * 根据组合编码获取组合产品详情\n" +
                "+     * @param inModel 入参\n" +
                "+     * @param userMap 用户情报\n" +
                "+     * @param request 请求\n" +
                "+     * @return Map<String, Object>\n" +
                "+     * @throws Exception 异常\n" +
                "+     */\n" +
                "+    @SuppressWarnings(\"unchecked\")\n" +
                "+    private Map<String, Object> getPortfolioDetail(ZhxqModel inModel, Map<String, String> userMap, HttpServletRequest request) throws Exception {\n" +
                "+        // 返回结果集\n" +
                "+        Map<String, Object> retMap = new HashMap<String ,Object>();\n" +
                "+        Map<String, Object> baseDataMap = this.getPortfolioDetailData(inModel, userMap, request);\n" +
                "         if (baseDataMap != null) {\n" +
                "             // 资产配置\n" +
                "             List<Map<String, String>>  assetsAllocationList = (List<Map<String, String>>) baseDataMap.get(\"assetsAllocation\");\n" +
                "@@ -1063,7 +1081,7 @@\n" +
                "             retMap.put(\"errmsg\", \"用户偏好保存失败！\");\n" +
                "             // 记录错误日志\n" +
                "             GLog.error(\"fundId: {}, AP interface id: {}, error: {}\", EagleSessionInfoManager.getClinetFundZTFundId(), request.getRequestURI(), EagleCommonUtil.getException(e));\n" +
                "-            }\n" +
                "+        }\n" +
                "\n" +
                "         // 返回结果\n" +
                "         return retMap;";

        DiffAdapter diffAdapter = new SvnDiffAdapter();
        List<String> lines = IOUtils.readLines(new StringReader(s));
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
                fieldAccessHierarchy.parseFieldAccessRecursion(diffDesc.fieldDesc.packageName,
                        diffDesc.fieldDesc.className, diffDesc.fieldDesc.fieldName);
            } else {
                System.out.println("==================Method: " + diffDesc.methodDesc.packageName + "." + diffDesc.methodDesc.className + ":" + diffDesc.methodDesc.methodName + "========================");
                callHierarchy.printParseMethodRecursion(diffDesc.methodDesc.packageName,
                        diffDesc.methodDesc.className, diffDesc.methodDesc.methodName);
            }
        }
    }
}
