package org.joker.java.call.hierarchy;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ZhishengInterfaceAnalysis {

    public static void main(String[] args) throws Exception {
        String cashProductQuery =
                "SELECT "
                        + "RTrim(ISNULL(detail.pro_code,'')) as proCode,"                       //产品代码
                        + "RTrim(ISNULL(detail.pro_category,'')) as proCategory,"               //产品类别
                        + "detail.pro_intro as proIntro,"                                       //产品说明
                        + "detail.pro_intro_short as proIntroShort,"                            //产品说明-缩略
                        + "detail.pro_intro_oth as proIntro2,"                                  //产品说明2
                        + "detail.pro_intro_oth_short as proIntro2Short,"                       //产品说明2-缩略
                        + "detail.pro_trade_info_short as proTradeInfoShort,"                   //买入规则-缩略
                        + "detail.pro_trade_info as proTradeInfo,"                              //买入规则
                        + "detail.pro_limit_info_short as proLimitInfoShort,"                   //起购金额规则-缩略
                        + "detail.pro_limit_info as proLimitInfo,"                              //起购金额规则
                        + "detail.pro_pre_withdraw_info_short as proPreWithdrawInfoShort,"      //预约取款规则-缩略
                        + "detail.pro_pre_withdraw_info as proPreWithdrawInfo,"                 //预约取款规则
                        + "detail.pro_urg_withdraw_info_short as proUrgWithdrawInfoShort,"      //应急取款规则-缩略
                        + "detail.pro_urg_withdraw_info as proUrgWithdrawInfo,"                 //应急取款规则
                        + "detail.pro_dividend_info_short as proDividendInfoShort,"             //分红方式说明-缩略
                        + "detail.pro_dividend_info as proDividendInfo,"                        //分红方式说明
                        + "detail.pro_trade_fee_info_short as proTradeFeeInfoShort,"            //交易费用说明-缩略
                        + "RTrim(ISNULL(detail.pro_trade_fee_info,'')) as proTradeFeeInfo,"     //交易费用说明
                        + "RTrim(ISNULL(detail.pro_mgr_info,'')) as proMgrInfo,"                //产品管理人
                        + "RTrim(ISNULL(detail.pro_custodian_info,'')) as proCustodianInfo,"    //产品托管人
                        + "RTrim(ISNULL(fix.risklevel,'')) as proRiskLevel,"                    //产品风险级别
                        + "RTrim(ISNULL(fix.proname,'')) as proName,"                           //产品名称
                        + "RTrim(ISNULL(fix.proshortname,'')) as proShortName "                 //产品简称
                        + "FROM "
                        + "t_cash_mgr_product_details detail, "                                 //现金理财扩展表
                        + "t_pro_info_fix fix "                                                 //固定类产品表
                        + "WHERE "
                        + "detail.id = fix.tableid AND fix.procode = ? ";

        System.out.println(cashProductQuery);

        String srcPath = "/Users/yuguangyuan/code/csc/eagle-parent/zxjt-gmCrm";
        String zhishengInterface = "L[0-9]{7,7}"; //otc接口
//        String zhishengInterface = "99[0-9]{6,6}";

        Set<String> set = new HashSet<>();

        Pattern p = Pattern.compile(zhishengInterface);

        if (args != null && args.length > 0) {
            srcPath = args[0];
        }

        File src = new File(srcPath);
        if (src.isDirectory()) {
            processDir(set, p, src);
        } else {
            processFile(set, p, src);
        }

        for (String s : set) {
            System.out.println(s);
        }
    }

    private static void processDir(Set<String> set, Pattern p, File src) throws IOException {
        File[] files = src.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                processDir(set, p, file);
            } else {
                processFile(set, p, file);
            }
        }
    }

    private static void processFile(Set<String> set, Pattern p, File src) throws IOException {
        if (!src.getName().endsWith(".java")) {
            return;
        }

        List<String> lines = IOUtils.readLines(new FileInputStream(src), Charsets.UTF_8);

        for (String line : lines) {
            Matcher matcher = p.matcher(line);

            if (matcher.find()) {
                set.add(matcher.group());
            }
        }
    }
}
