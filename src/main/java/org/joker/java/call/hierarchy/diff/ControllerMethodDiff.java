package org.joker.java.call.hierarchy.diff;

public class ControllerMethodDiff {

    public String className;

    public String method;

    public String requestMapping;

    public String comment;

    public ControllerMethodDiff(String className, String method, String requestMapping, String comment) {
        this.className = className;
        this.method = method;
        this.requestMapping = requestMapping;
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "ControllerMethodDiff{" +
                "className='" + className + '\'' +
                ", method='" + method + '\'' +
                ", requestMapping='" + requestMapping + '\'' +
                ", comment='" + comment + '\'' +
                '}';
    }
}
