package org.joker.java.call.hierarchy;

import java.math.BigDecimal;
import java.util.*;

public class DuplicateTest {

    /**
     * ListMap去重复
     * @param list
     */
    public static void removeDuplicate(List<Map<String, String>> list){
        Set<Map<String, String>> set = new LinkedHashSet<Map<String, String>>(list);
        list.clear();
        list.addAll(set);
    }

    public static void main(String[] args) {
//        ArrayList<Map<String, String>> oldList = new ArrayList<>();
//        HashMap<String, String> a = new HashMap<>();
//        a.put("1", "a");
//
//        HashMap<String, String> b = new HashMap<>();
//        b.put("1", "a");
//
//        oldList.add(a);
//        oldList.add(b);
//
//        System.out.println(oldList.size());
//
////        CommonBaseBusinessService service = new CommonBaseBusinessService();
////        service.removeDuplicate(oldList);
//        removeDuplicate(oldList);
//
//        System.out.println(oldList.size());


//        BigDecimal total = new BigDecimal(2500000);
//
//        BigDecimal benjin = new BigDecimal(55250);
//
//        BigDecimal sum = new BigDecimal(0);
//
//        sum = sum.add(benjin);
//        for (int i = 0; i < 40; i++) {
//            sum = sum.add(sum.multiply(new BigDecimal("0.1")));
//        }
//
//        System.out.println(sum);
//        System.out.println(total);


//        BigDecimal benjin = new BigDecimal(10432700);
//
//        BigDecimal sum = new BigDecimal(0);
//
//        sum = sum.add(benjin);
//
//        for (int i = 0; i < 5; i++) {
//            sum = sum.add(sum.multiply(new BigDecimal(0.0500057))).add(new BigDecimal(-600000));
//        }
//
//        System.out.println(sum);


        double cost = 1200;

        double interest = 0.05;

        int duration = 10;

        double sum = 0;

        for (int i = duration; i > 0; i--) {
            sum += cost * Math.pow(1 + interest, i);
        }

        System.out.println(sum);

        System.out.println(Math.pow(1 + interest, 10));
    }
}
