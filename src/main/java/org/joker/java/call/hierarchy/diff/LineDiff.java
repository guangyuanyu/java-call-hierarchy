package org.joker.java.call.hierarchy.diff;

public class LineDiff {
    public DiffType type;

    public String line;

    public int lineNum;

    public String filename;

    /**
     * 下面4个值是分析之后的结果
     */
    public String packageName;

    public String clazzName;

    public String methodName;

    public String fieldName;

    public boolean needProcess() {
        return methodName != null || fieldName != null;
    }

    static enum DiffType {
        ADD(0, "add"),
//        MODIFY(1, "modify"),
        DELETE(2, "delete")
        ;


        DiffType(int index, String desc) {
            this.index = index;
            this.desc = desc;
        }

        int index;
        String desc;
    }
}
