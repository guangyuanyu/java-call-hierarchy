package org.joker.java.call.hierarchy.core;

import java.util.List;

public class FirstLevelHierarchy extends Hierarchy {

    private String pack;

    private String className;

    private String methodName;

    public FirstLevelHierarchy(String pack, String className, String methodName) {
        super(null);

        this.pack = pack;
        this.className = className;
        this.methodName = methodName;
        this.setQualifiedName(String.format("%s.%s.%s", pack, className, methodName));
    }

    @Override
    public List<String> toStringList() {
        String prefix = String.format("%s.%s.%s", pack, className, methodName);

        if (getCalls().isEmpty()) {
            if (this.getRequestMapping() != null && !this.getRequestMapping().equals("")) {
                String requestMapping = this.getRequestMapping();
                prefix = prefix + "(url: " + requestMapping + ")";
            }
            if (this.getComment() != null && !this.getComment().equals("")) {
                prefix = prefix + "【" + this.getComment() + "】";
            }
        }
        return toStringList(prefix, getCalls(), getFunction());
    }
}
