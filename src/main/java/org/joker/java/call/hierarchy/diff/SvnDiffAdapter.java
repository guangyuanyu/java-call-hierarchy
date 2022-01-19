package org.joker.java.call.hierarchy.diff;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class SvnDiffAdapter implements DiffAdapter {

    private String processingFile;

    private String processingClass;

    private String packageName;

    private String className;

    private int startLine;

    private int newFileLineNum;

    private int oldFileLineNum;

//    private List<Long> deletedLineNums = new ArrayList<>();

    @Override
    public List<FileDiff> toDiff(List<String> diff) {
        List<FileDiff> ret = new ArrayList<>();
        int i = 0;
        int size = diff.size();
        FileDiff fileDiff = null;
        while (i < size){
            String line = diff.get(i);
            if (line.startsWith("Index: ")) {
                if (!line.endsWith(".java")) {
                    continue; //非java文件不处理
                }
                fileDiff = new FileDiff();
                ret.add(fileDiff);
                processingFile = line.substring(7);
                processingClass = extractProcessingFile();
                className = extractClassName();
                packageName = extractPackageName();
                fileDiff.filename = processingFile;

                // 跨过3行
                i += 3;
                line = diff.get(i+1);
                if (!line.startsWith("@@")) {
                    throw new IllegalArgumentException("diff input illegal");
                }
            } else if (line.startsWith("@@")) {
                line = line.replaceAll("@@", "").trim();
                String[] array = line.split("\\W");
                startLine = Math.abs(Integer.parseInt(array[4].trim()));
                oldFileLineNum = startLine;
                newFileLineNum = startLine;
            } else {
                if (line.startsWith("+")) {
                    LineDiff lineDiff = new LineDiff();
                    lineDiff.lineNum = newFileLineNum;
                    lineDiff.line = line;
                    lineDiff.type = LineDiff.DiffType.ADD;
                    lineDiff.filename = processingFile;
                    lineDiff.packageName = packageName;
                    lineDiff.clazzName = className;

                    fileDiff.diffSet.add(lineDiff);
                    newFileLineNum++;
                } else if (line.startsWith("-")) {
                    int tempIndex = i;
                    String tempLine = diff.get(tempIndex);
                    while (tempLine.startsWith("-") && tempIndex < size) {
                        tempLine = diff.get(++tempIndex);
                    }
                    if ((tempIndex < size) && !tempLine.startsWith("+")
                        || (tempIndex == size)) {
                        for (int minusIndex = i; minusIndex < tempIndex; minusIndex++) {
                            LineDiff lineDiff = new LineDiff();
                            lineDiff.lineNum = oldFileLineNum++;
                            lineDiff.type = LineDiff.DiffType.DELETE;
                            lineDiff.filename = processingFile;
                            lineDiff.packageName = packageName;
                            lineDiff.clazzName = className;
                            lineDiff.line = diff.get(minusIndex);
                            fileDiff.diffSet.add(lineDiff);
                        }
                    }
                    i = tempIndex - 1;
                } else {
                    newFileLineNum++;
                    oldFileLineNum++;
                }
            }
            i++;
        }

        return ret;
    }

    private String extractProcessingFile() {
        int i = processingFile.lastIndexOf("/java/");
        String name = processingFile.substring(i + 6);
        name = name.substring(0, name.length() - 5);
        name = name.replaceAll("/", ".");
        return name;
    }

    private String extractClassName() {
        int i = processingClass.lastIndexOf(".");
        return processingClass.substring(i+1);
    }

    private String extractPackageName() {
        int i = processingClass.lastIndexOf(".");
        return processingClass.substring(0, i);
    }

    public static void main(String[] args) throws IOException {
        String s = "Index: src/main/java/com/linkstec/raptor/eagle/frap/service/PortfolioService.java\n" +
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

        SvnDiffAdapter diffAdapter = new SvnDiffAdapter();
        List<String> lines = IOUtils.readLines(new StringReader(s));
        List<FileDiff> diffs = diffAdapter.toDiff(lines);
        for (FileDiff diff : diffs) {
            System.out.println(diff.toString());
            System.out.println("==========================");
        }
    }
}
