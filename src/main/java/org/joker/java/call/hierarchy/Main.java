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

            callHierarchy.printParseMethodRecursion("gmCrm", packageName, javaName, method);
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

        fieldAccessHierarchy.printFieldAccessRecursion("gmCrm", packageName, javaName, field);
    }

    private static void init() {
        Config config = new Config();
        config.setProjectPath("/Users/yuguangyuan/code/csc/eagle-parent/zxjt-smjj");
        try {
            callHierarchy = new CallHierarchy(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static CallHierarchy callHierarchy;

    private static List<DiffLocator.DiffDesc> locateSvnDiff() throws IOException {
        String s = "Index: zxjt-smjj/src/main/java/com/linkstec/raptor/eagle/frap/service/OtcFundService.java\n" +
                "===================================================================\n" +
                "--- zxjt-smjj/src/main/java/com/linkstec/raptor/eagle/frap/service/OtcFundService.java\t(版本 348988)\n" +
                "+++ zxjt-smjj/src/main/java/com/linkstec/raptor/eagle/frap/service/OtcFundService.java\t(版本 348960)\n" +
                "@@ -111,10 +111,6 @@\n" +
                "\n" +
                "         // 产品名称\n" +
                "         retMap.put(\"ofName\", fundMap.get(\"inst_fname\"));\n" +
                "-// update 20220125 by LKG 【需求】私募产品单向保存产品信息，单向话术用简称，不是全称  --start\n" +
                "-        // 产品简称 新增\n" +
                "-        retMap.put(\"ofShortName\", fundMap.get(\"productname\"));\n" +
                "-//update 20220125 by LKG 【需求】私募产品单向保存产品信息，单向话术用简称，不是全称  --end\n" +
                "         // 产品编码\n" +
                "         retMap.put(\"productId\", fundMap.get(\"productid\"));\n" +
                "         // 发行人代码\n" +
                "Index: zxjt-smjj/src/main/java/com/linkstec/raptor/eagle/frap/service/LDSFOnlineHall3rdService.java\n" +
                "===================================================================\n" +
                "--- zxjt-smjj/src/main/java/com/linkstec/raptor/eagle/frap/service/LDSFOnlineHall3rdService.java\t(版本 348988)\n" +
                "+++ zxjt-smjj/src/main/java/com/linkstec/raptor/eagle/frap/service/LDSFOnlineHall3rdService.java\t(版本 348960)\n" +
                "@@ -44,12 +44,9 @@\n" +
                "         String retPwd = pwd;\n" +
                "         if (StringUtils.isNotBlank(retPwd)) {\n" +
                "             if (StringUtils.isNotBlank(ELE_SECRET_KEY)) {\n" +
                "-// update 20220113 by LKG 手机号加密【秘钥问题修复】 ---start\n" +
                "-                String secretKey = ELE_SECRET_KEY.replace(\"{0}\", EagleCommonUtil.getNextDay(EagleConstant.NUMBER_0));\n" +
                "+                ELE_SECRET_KEY = ELE_SECRET_KEY.replace(\"{0}\", EagleCommonUtil.getNextDay(EagleConstant.NUMBER_0));\n" +
                "                 try {\n" +
                "-                    // 秘钥只取前8位\n" +
                "-                    DES secDes = new DES(secretKey);\n" +
                "-// update 20220113 by LKG 手机号加密【秘钥问题修复】 ---end\n" +
                "+                    DES secDes = new DES(ELE_SECRET_KEY);\n" +
                "                     retPwd = secDes.decrypt(pwd);\n" +
                "                 } catch (Exception e) {\n" +
                "                     ;    // nothing do it\n" +
                "Index: zxjt-smjj/src/main/java/com/linkstec/raptor/eagle/frap/service/KidmService.java\n" +
                "===================================================================\n" +
                "--- zxjt-smjj/src/main/java/com/linkstec/raptor/eagle/frap/service/KidmService.java\t(版本 348988)\n" +
                "+++ zxjt-smjj/src/main/java/com/linkstec/raptor/eagle/frap/service/KidmService.java\t(版本 348960)\n" +
                "@@ -231,11 +231,8 @@\n" +
                "                     !(phone.length() == EagleConstant.NUMBER_11 && isNumeric(phone))\n" +
                "                     && StringUtils.isNotBlank(secretKey)) {\n" +
                "                 try {\n" +
                "-// update 20220113 by LKG 手机号加密【秘钥问题修复】 ---start\n" +
                "                     String key = secretKey.replace(\"{0}\", sdf.get().format(System.currentTimeMillis()));\n" +
                "-                    // 秘钥只取前8位\n" +
                "                     DES des = new DES(key);\n" +
                "-// update 20220113 by LKG 手机号加密【秘钥问题修复】 ---end\n" +
                "                     phone = des.decrypt(phone);\n" +
                "                 } catch (Exception e) {\n" +
                "                     GLog.error(\"fundId: {}, call AP interface id: {}, svrId: {}, error={}\", EagleSessionInfoManager.getClinetFundZTFundId(), request.getRequestURI(), \"KidmService#getRemark phone DES decrypt phone = \" + phone, EagleCommonUtil.getException(e));\n" +
                "@@ -362,11 +359,8 @@\n" +
                "                             !(phone.length() == EagleConstant.NUMBER_11 && isNumeric(phone))\n" +
                "                             && StringUtils.isNotBlank(secretKey)) {\n" +
                "                         try {\n" +
                "-// update 20220113 by LKG 手机号加密【秘钥问题修复】 ---start\n" +
                "                             String key = secretKey.replace(\"{0}\", sdf.get().format(System.currentTimeMillis()));\n" +
                "-                            // 秘钥只取前8位\n" +
                "                             DES des = new DES(key);\n" +
                "-// update 20220113 by LKG 手机号加密【秘钥问题修复】 ---end\n" +
                "                             phone = des.decrypt(phone);\n" +
                "                         } catch (Exception e) {\n" +
                "                             GLog.error(\"fundId: {}, call AP interface id: {}, svrId: {}, error={}\", EagleSessionInfoManager.getClinetFundZTFundId(), request.getRequestURI(), \"KidmService#getRemark phone DES decrypt phone = \" + phone, EagleCommonUtil.getException(e));\n" +
                "@@ -1253,14 +1247,12 @@\n" +
                "             String errorCode = e.getErrors().get(EagleConstant.NUMBER_0).getCode();\n" +
                "             String errorMsg = e.getErrors().get(EagleConstant.NUMBER_0).getMessage();\n" +
                "             GLog.error(\"fundId: {}, call AP interface id：{}, svrId: {}, error: {}\", userMap.get(\"fundid\"), request.getRequestURI(), \"KidmService#getCaSign\", errorCode + EagleConstant.SPACE + errorMsg);\n" +
                "-            retMap.put(\"flag\", EagleConstant.STR_0);\n" +
                "-            // 校验手机验证码失败，flag返回2\n" +
                "             if (StringUtils.equals(EagleConstant.ERROR_CODE_I0001, errorCode)) {\n" +
                "                 retMap.put(\"errMsg\", errorMsg);\n" +
                "-                retMap.put(\"flag\", EagleConstant.STR_2); // ca校验失败\n" +
                "             } else {\n" +
                "                 retMap.put(\"errMsg\", errorCode + EagleConstant.SPACE + errorMsg);\n" +
                "             }\n" +
                "+            retMap.put(\"flag\", EagleConstant.STR_0);\n" +
                "         } catch (Exception e) {\n" +
                "             GLog.error(\"fundId: {}, call AP interface id：{}, svrId: {}, error: {}\", userMap.get(\"fundid\"), request.getRequestURI(), \"KidmService#getCaSign\", e);\n" +
                "             retMap.put(\"errMsg\", EagleConstant.ERROR_CODE_E0001 + EagleConstant.SPACE + \"预想外错误发生，CA签约失败！\");\n" +
                "Index: zxjt-smjj/src/main/resources/readme/Readme.md\n" +
                "===================================================================\n" +
                "--- zxjt-smjj/src/main/resources/readme/Readme.md\t(版本 348988)\n" +
                "+++ zxjt-smjj/src/main/resources/readme/Readme.md\t(版本 348960)\n" +
                "@@ -1,14 +1,3 @@\n" +
                "-时间：20220126\n" +
                "-部署版本记录：V.1.2.2.RELEASE\n" +
                "-改修内容：\n" +
                "-私募基金V3.9.0\n" +
                "-1、【需求】私募产品单向保存产品信息，单向话术用简称，不是全称\n" +
                "-2、【需求】私募双录默认展示人工和单向入口\n" +
                "-3、redis验密改造\n" +
                "-4、手机号加密【秘钥问题修复】\n" +
                "-5、【技术优化】致胜调用增加apikey\n" +
                "-6、私募基金 - 电子签约 - 兼容性优化\n" +
                "-\n" +
                " 时间：20211210\n" +
                " 部署版本记录：V.1.2.1.RELEASE\n" +
                " 改修内容：\n" +
                "Index: zxjt-smjj/src/main/resources/smjj.properties\n" +
                "===================================================================\n" +
                "--- zxjt-smjj/src/main/resources/smjj.properties\t(版本 348988)\n" +
                "+++ zxjt-smjj/src/main/resources/smjj.properties\t(版本 348960)\n" +
                "@@ -36,8 +36,7 @@\n" +
                " # Redis\\u670d\\u52a1\\u5668\\u5730\\u5740\n" +
                " redisUser.server = 192.168.9.57:6379\n" +
                " #redisUser.server = usermaster,192.168.9.25:26379,192.168.9.26:26380,192.168.9.26:26381\n" +
                "-# redis\\u9A8C\\u5BC6\\u6539\\u9020,\\u589E\\u52A0\\u5BC6\\u7801,\\u9ED8\\u8BA4\\u4E3A\\u7A7A\n" +
                "-redisUser.passWord =\n" +
                "+\n" +
                " # Redis\\u8fde\\u63a5\\u6570\\u914d\\u7f6e\\uff08\\u524d\\u7aef\\uff09\n" +
                " frontap.redisUser.maxIdle = 5\n" +
                " frontap.redisUser.minIdle = 5\n" +
                "@@ -156,7 +155,4 @@\n" +
                " eagle.sensors.eventTracking.server = https://track-test.csc.com.cn:4443/sa?project=default\n" +
                "\n" +
                " #\\u4ea4\\u6613\\u65f6\\u95f4\n" +
                "-eagle.smjj.trade.time=09:00-16:00\n" +
                "-\n" +
                "-#\\u7f51\\u5385\\u7cfb\\u7edf\\uff1a\\u81f4\\u80dc\\u8c03\\u7528\\u589e\\u52a0apikey\n" +
                "-eagle.system.inner.apikey = csc-ecomm-web-inner-test\n" +
                "\\ No newline at end of file\n" +
                "+eagle.smjj.trade.time=09:00-16:00\n" +
                "\\ No newline at end of file\n" +
                "Index: zxjt-smjj/src/main/resources/application-context.xml\n" +
                "===================================================================\n" +
                "--- zxjt-smjj/src/main/resources/application-context.xml\t(版本 348988)\n" +
                "+++ zxjt-smjj/src/main/resources/application-context.xml\t(版本 348960)\n" +
                "@@ -84,7 +84,6 @@\n" +
                "                 <prop key=\"sessionTimeout\">${eagle.session.timeout:7200}</prop>\n" +
                "                 <!-- 券商标识 -->\n" +
                "                 <prop key=\"eagle.system.mark\">${eagle.system.mark:zxjt}</prop>\n" +
                "-                <prop key=\"eagle.subSystem.mark\">${eagle.subSystem.mark:smjj}</prop>\n" +
                "                 <!-- 中信建投电商平台交易秘钥 -->\n" +
                "                 <prop key=\"eagle.ldsf.secret.key\">${eagle.ldsf.secret.key:lmspABCDE12345}</prop>\n" +
                "                 <!-- 中投手机短信验证码接口参数 -->\n" +
                "@@ -132,8 +131,6 @@\n" +
                "     </bean>\n" +
                "     <bean id=\"jedisPoolUser\" class=\"com.linkstec.raptor.eagle.common.config.SpringJedisPoolFactoryBean\">\n" +
                "         <property name=\"server\" value=\"${redisUser.server:192.168.9.61:6379}\" />\n" +
                "-        <!-- redis验密改造,增加密码,默认为空 -->\n" +
                "-        <property name=\"password\" value=\"${redisUser.passWord:}\" />\n" +
                "         <property name=\"poolConfig\" ref=\"jedisPoolUserConfig\" />\n" +
                "     </bean>\n" +
                "     <bean id=\"jedisResourceUser\" class=\"com.linkstec.raptor.eagle.common.ds.internal.JedisPoolResource\">\n" +
                "(base) yuguangyuan@yuguangyuandeMacBook-Pro [15时43分20秒] [~/code/csc/eagle-parent]\n" +
                "-> %\n" +
                "(base) yuguangyuan@yuguangyuandeMacBook-Pro [15时43分28秒] [~/code/csc/eagle-parent]\n" +
                "-> %\n" +
                "(base) yuguangyuan@yuguangyuandeMacBook-Pro [15时43分28秒] [~/code/csc/eagle-parent]\n" +
                "-> %\n" +
                "(base) yuguangyuan@yuguangyuandeMacBook-Pro [15时43分28秒] [~/code/csc/eagle-parent]\n" +
                "-> %\n" +
                "(base) yuguangyuan@yuguangyuandeMacBook-Pro [15时43分28秒] [~/code/csc/eagle-parent]\n" +
                "-> %\n" +
                "(base) yuguangyuan@yuguangyuandeMacBook-Pro [15时43分28秒] [~/code/csc/eagle-parent]\n" +
                "-> %\n" +
                "(base) yuguangyuan@yuguangyuandeMacBook-Pro [15时43分28秒] [~/code/csc/eagle-parent]\n" +
                "-> %\n" +
                "(base) yuguangyuan@yuguangyuandeMacBook-Pro [15时43分28秒] [~/code/csc/eagle-parent]\n" +
                "-> %\n" +
                "(base) yuguangyuan@yuguangyuandeMacBook-Pro [15时43分29秒] [~/code/csc/eagle-parent]\n" +
                "-> %\n" +
                "(base) yuguangyuan@yuguangyuandeMacBook-Pro [15时43分29秒] [~/code/csc/eagle-parent]\n" +
                "-> %\n" +
                "(base) yuguangyuan@yuguangyuandeMacBook-Pro [15时43分29秒] [~/code/csc/eagle-parent]\n" +
                "-> %\n" +
                "(base) yuguangyuan@yuguangyuandeMacBook-Pro [15时43分29秒] [~/code/csc/eagle-parent]\n" +
                "-> % svn diff -r 348988:348960\n" +
                "Index: zxjt-smjj/src/main/java/com/linkstec/raptor/eagle/frap/service/KidmService.java\n" +
                "===================================================================\n" +
                "--- zxjt-smjj/src/main/java/com/linkstec/raptor/eagle/frap/service/KidmService.java\t(版本 348988)\n" +
                "+++ zxjt-smjj/src/main/java/com/linkstec/raptor/eagle/frap/service/KidmService.java\t(版本 348960)\n" +
                "@@ -231,11 +231,8 @@\n" +
                "                     !(phone.length() == EagleConstant.NUMBER_11 && isNumeric(phone))\n" +
                "                     && StringUtils.isNotBlank(secretKey)) {\n" +
                "                 try {\n" +
                "-// update 20220113 by LKG 手机号加密【秘钥问题修复】 ---start\n" +
                "                     String key = secretKey.replace(\"{0}\", sdf.get().format(System.currentTimeMillis()));\n" +
                "-                    // 秘钥只取前8位\n" +
                "                     DES des = new DES(key);\n" +
                "-// update 20220113 by LKG 手机号加密【秘钥问题修复】 ---end\n" +
                "                     phone = des.decrypt(phone);\n" +
                "                 } catch (Exception e) {\n" +
                "                     GLog.error(\"fundId: {}, call AP interface id: {}, svrId: {}, error={}\", EagleSessionInfoManager.getClinetFundZTFundId(), request.getRequestURI(), \"KidmService#getRemark phone DES decrypt phone = \" + phone, EagleCommonUtil.getException(e));\n" +
                "@@ -362,11 +359,8 @@\n" +
                "                             !(phone.length() == EagleConstant.NUMBER_11 && isNumeric(phone))\n" +
                "                             && StringUtils.isNotBlank(secretKey)) {\n" +
                "                         try {\n" +
                "-// update 20220113 by LKG 手机号加密【秘钥问题修复】 ---start\n" +
                "                             String key = secretKey.replace(\"{0}\", sdf.get().format(System.currentTimeMillis()));\n" +
                "-                            // 秘钥只取前8位\n" +
                "                             DES des = new DES(key);\n" +
                "-// update 20220113 by LKG 手机号加密【秘钥问题修复】 ---end\n" +
                "                             phone = des.decrypt(phone);\n" +
                "                         } catch (Exception e) {\n" +
                "                             GLog.error(\"fundId: {}, call AP interface id: {}, svrId: {}, error={}\", EagleSessionInfoManager.getClinetFundZTFundId(), request.getRequestURI(), \"KidmService#getRemark phone DES decrypt phone = \" + phone, EagleCommonUtil.getException(e));\n" +
                "@@ -1253,14 +1247,12 @@\n" +
                "             String errorCode = e.getErrors().get(EagleConstant.NUMBER_0).getCode();\n" +
                "             String errorMsg = e.getErrors().get(EagleConstant.NUMBER_0).getMessage();\n" +
                "             GLog.error(\"fundId: {}, call AP interface id：{}, svrId: {}, error: {}\", userMap.get(\"fundid\"), request.getRequestURI(), \"KidmService#getCaSign\", errorCode + EagleConstant.SPACE + errorMsg);\n" +
                "-            retMap.put(\"flag\", EagleConstant.STR_0);\n" +
                "-            // 校验手机验证码失败，flag返回2\n" +
                "             if (StringUtils.equals(EagleConstant.ERROR_CODE_I0001, errorCode)) {\n" +
                "                 retMap.put(\"errMsg\", errorMsg);\n" +
                "-                retMap.put(\"flag\", EagleConstant.STR_2); // ca校验失败\n" +
                "             } else {\n" +
                "                 retMap.put(\"errMsg\", errorCode + EagleConstant.SPACE + errorMsg);\n" +
                "             }\n" +
                "+            retMap.put(\"flag\", EagleConstant.STR_0);\n" +
                "         } catch (Exception e) {\n" +
                "             GLog.error(\"fundId: {}, call AP interface id：{}, svrId: {}, error: {}\", userMap.get(\"fundid\"), request.getRequestURI(), \"KidmService#getCaSign\", e);\n" +
                "             retMap.put(\"errMsg\", EagleConstant.ERROR_CODE_E0001 + EagleConstant.SPACE + \"预想外错误发生，CA签约失败！\");\n" +
                "Index: zxjt-smjj/src/main/java/com/linkstec/raptor/eagle/frap/service/OtcFundService.java\n" +
                "===================================================================\n" +
                "--- zxjt-smjj/src/main/java/com/linkstec/raptor/eagle/frap/service/OtcFundService.java\t(版本 348988)\n" +
                "+++ zxjt-smjj/src/main/java/com/linkstec/raptor/eagle/frap/service/OtcFundService.java\t(版本 348960)\n" +
                "@@ -111,10 +111,6 @@\n" +
                "\n" +
                "         // 产品名称\n" +
                "         retMap.put(\"ofName\", fundMap.get(\"inst_fname\"));\n" +
                "-// update 20220125 by LKG 【需求】私募产品单向保存产品信息，单向话术用简称，不是全称  --start\n" +
                "-        // 产品简称 新增\n" +
                "-        retMap.put(\"ofShortName\", fundMap.get(\"productname\"));\n" +
                "-//update 20220125 by LKG 【需求】私募产品单向保存产品信息，单向话术用简称，不是全称  --end\n" +
                "         // 产品编码\n" +
                "         retMap.put(\"productId\", fundMap.get(\"productid\"));\n" +
                "         // 发行人代码\n" +
                "Index: zxjt-smjj/src/main/java/com/linkstec/raptor/eagle/frap/service/LDSFOnlineHall3rdService.java\n" +
                "===================================================================\n" +
                "--- zxjt-smjj/src/main/java/com/linkstec/raptor/eagle/frap/service/LDSFOnlineHall3rdService.java\t(版本 348988)\n" +
                "+++ zxjt-smjj/src/main/java/com/linkstec/raptor/eagle/frap/service/LDSFOnlineHall3rdService.java\t(版本 348960)\n" +
                "@@ -44,12 +44,9 @@\n" +
                "         String retPwd = pwd;\n" +
                "         if (StringUtils.isNotBlank(retPwd)) {\n" +
                "             if (StringUtils.isNotBlank(ELE_SECRET_KEY)) {\n" +
                "-// update 20220113 by LKG 手机号加密【秘钥问题修复】 ---start\n" +
                "-                String secretKey = ELE_SECRET_KEY.replace(\"{0}\", EagleCommonUtil.getNextDay(EagleConstant.NUMBER_0));\n" +
                "+                ELE_SECRET_KEY = ELE_SECRET_KEY.replace(\"{0}\", EagleCommonUtil.getNextDay(EagleConstant.NUMBER_0));\n" +
                "                 try {\n" +
                "-                    // 秘钥只取前8位\n" +
                "-                    DES secDes = new DES(secretKey);\n" +
                "-// update 20220113 by LKG 手机号加密【秘钥问题修复】 ---end\n" +
                "+                    DES secDes = new DES(ELE_SECRET_KEY);\n" +
                "                     retPwd = secDes.decrypt(pwd);\n" +
                "                 } catch (Exception e) {\n" +
                "                     ;    // nothing do it\n" +
                "Index: zxjt-smjj/src/main/resources/application-context.xml\n" +
                "===================================================================\n" +
                "--- zxjt-smjj/src/main/resources/application-context.xml\t(版本 348988)\n" +
                "+++ zxjt-smjj/src/main/resources/application-context.xml\t(版本 348960)\n" +
                "@@ -84,7 +84,6 @@\n" +
                "                 <prop key=\"sessionTimeout\">${eagle.session.timeout:7200}</prop>\n" +
                "                 <!-- 券商标识 -->\n" +
                "                 <prop key=\"eagle.system.mark\">${eagle.system.mark:zxjt}</prop>\n" +
                "-                <prop key=\"eagle.subSystem.mark\">${eagle.subSystem.mark:smjj}</prop>\n" +
                "                 <!-- 中信建投电商平台交易秘钥 -->\n" +
                "                 <prop key=\"eagle.ldsf.secret.key\">${eagle.ldsf.secret.key:lmspABCDE12345}</prop>\n" +
                "                 <!-- 中投手机短信验证码接口参数 -->\n" +
                "@@ -132,8 +131,6 @@\n" +
                "     </bean>\n" +
                "     <bean id=\"jedisPoolUser\" class=\"com.linkstec.raptor.eagle.common.config.SpringJedisPoolFactoryBean\">\n" +
                "         <property name=\"server\" value=\"${redisUser.server:192.168.9.61:6379}\" />\n" +
                "-        <!-- redis验密改造,增加密码,默认为空 -->\n" +
                "-        <property name=\"password\" value=\"${redisUser.passWord:}\" />\n" +
                "         <property name=\"poolConfig\" ref=\"jedisPoolUserConfig\" />\n" +
                "     </bean>\n" +
                "     <bean id=\"jedisResourceUser\" class=\"com.linkstec.raptor.eagle.common.ds.internal.JedisPoolResource\">\n" +
                "Index: zxjt-smjj/src/main/resources/readme/Readme.md\n" +
                "===================================================================\n" +
                "--- zxjt-smjj/src/main/resources/readme/Readme.md\t(版本 348988)\n" +
                "+++ zxjt-smjj/src/main/resources/readme/Readme.md\t(版本 348960)\n" +
                "@@ -1,14 +1,3 @@\n" +
                "-时间：20220126\n" +
                "-部署版本记录：V.1.2.2.RELEASE\n" +
                "-改修内容：\n" +
                "-私募基金V3.9.0\n" +
                "-1、【需求】私募产品单向保存产品信息，单向话术用简称，不是全称\n" +
                "-2、【需求】私募双录默认展示人工和单向入口\n" +
                "-3、redis验密改造\n" +
                "-4、手机号加密【秘钥问题修复】\n" +
                "-5、【技术优化】致胜调用增加apikey\n" +
                "-6、私募基金 - 电子签约 - 兼容性优化\n" +
                "-\n" +
                " 时间：20211210\n" +
                " 部署版本记录：V.1.2.1.RELEASE\n" +
                " 改修内容：\n" +
                "Index: zxjt-smjj/src/main/resources/smjj.properties\n" +
                "===================================================================\n" +
                "--- zxjt-smjj/src/main/resources/smjj.properties\t(版本 348988)\n" +
                "+++ zxjt-smjj/src/main/resources/smjj.properties\t(版本 348960)\n" +
                "@@ -36,8 +36,7 @@\n" +
                " # Redis\\u670d\\u52a1\\u5668\\u5730\\u5740\n" +
                " redisUser.server = 192.168.9.57:6379\n" +
                " #redisUser.server = usermaster,192.168.9.25:26379,192.168.9.26:26380,192.168.9.26:26381\n" +
                "-# redis\\u9A8C\\u5BC6\\u6539\\u9020,\\u589E\\u52A0\\u5BC6\\u7801,\\u9ED8\\u8BA4\\u4E3A\\u7A7A\n" +
                "-redisUser.passWord =\n" +
                "+\n" +
                " # Redis\\u8fde\\u63a5\\u6570\\u914d\\u7f6e\\uff08\\u524d\\u7aef\\uff09\n" +
                " frontap.redisUser.maxIdle = 5\n" +
                " frontap.redisUser.minIdle = 5\n" +
                "@@ -156,7 +155,4 @@\n" +
                " eagle.sensors.eventTracking.server = https://track-test.csc.com.cn:4443/sa?project=default\n" +
                "\n" +
                " #\\u4ea4\\u6613\\u65f6\\u95f4\n" +
                "-eagle.smjj.trade.time=09:00-16:00\n" +
                "-\n" +
                "-#\\u7f51\\u5385\\u7cfb\\u7edf\\uff1a\\u81f4\\u80dc\\u8c03\\u7528\\u589e\\u52a0apikey\n" +
                "-eagle.system.inner.apikey = csc-ecomm-web-inner-test\n" +
                "\\ No newline at end of file\n" +
                "+eagle.smjj.trade.time=09:00-16:00";

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
