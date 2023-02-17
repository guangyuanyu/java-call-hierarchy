package org.joker.java.call.hierarchy.core;

import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import org.joker.java.call.hierarchy.DiffLocator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Hierarchy<T> {

    private T target;
    private List<Hierarchy<T>> calls;
    private String requestMapping = null;
    private String comment;
    private String module;
    /**
     * 如果是method 则是qualifiedMethodName
     * 如果是field 则是qualifiedFieldName
     */
    private String qualifiedName;

    private String qualifiedClassName;

    private String methodName;

    public Hierarchy(T target) {
        this(target, new ArrayList<>());
    }

    public Hierarchy(String module, T target) {
        this(module, target, new ArrayList<>());
    }

    public Hierarchy(T target, List<Hierarchy<T>> calls) {
        this("", target, calls);
    }

    public Hierarchy(String module, T target, List<Hierarchy<T>> calls) {
        this.target = target;
        this.calls = calls;
        this.module = module;
        if (target != null && target instanceof ResolvedMethodDeclaration ) {
            ResolvedMethodDeclaration methodDeclaration = (ResolvedMethodDeclaration) target;
            String packageName = methodDeclaration.getPackageName();
            String className = methodDeclaration.getClassName();
            String name = methodDeclaration.getName();
            this.qualifiedName = String.format("%s.%s.%s",
                    packageName, className, name);
            this.qualifiedClassName = String.format("%s.%s",
                    packageName, className);
            this.methodName = name;
        }
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getQualifiedClassName() {
        return qualifiedClassName;
    }

    public void setQualifiedClassName(String qualifiedClassName) {
        this.qualifiedClassName = qualifiedClassName;
    }

    public T getTarget() {
        return target;
    }

    public void setTarget(T target) {
        this.target = target;
    }

    public List<Hierarchy<T>> getCalls() {
        return calls;
    }

    public void setCalls(List<Hierarchy<T>> calls) {
        this.calls = calls;
    }

    public String getRequestMapping() {
        return requestMapping;
    }

    public void setRequestMapping(String requestMapping) {
        this.requestMapping = requestMapping;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void addCall(Hierarchy<T> call) {
        Function<T, String> function = getFunction();
        String s1 = function.apply(call.getTarget());
        for (Hierarchy<T> hierarchy : calls) {
            String s2 = function.apply(hierarchy.target);
            if (s1.equals(s2)) {
                return;
            }
        }
        calls.add(call);
    }

    public void addCalls(List<Hierarchy<T>> calls) {
        Function<T, String> function = getFunction();
        for (Hierarchy<T> call : calls) {
            String s1 = function.apply(call.getTarget());
            for (Hierarchy<T> hierarchy : calls) {
                String s2 = function.apply(hierarchy.target);
                if (s1.equals(s2)) {
                    break;
                }
            }
            this.calls.add(call);
        }
    }

    public List<String> toStringList() {
        Function<T, String> function = getFunction();
        String prefix = function.apply(target);
        if (calls.isEmpty()) {
            if (this.getRequestMapping() != null && !this.getRequestMapping().equals("")) {
                String requestMapping = this.getRequestMapping();
                prefix = prefix + "(url: " + requestMapping + ")";
            }
            if (this.getComment() != null && !this.getComment().equals("")) {
                prefix = prefix + "【" + this.getComment() + "】";
            }
        }
        return toStringList(prefix, calls, function);
    }

    public List<String> toStringList(String prefix, List<Hierarchy<T>> calls, Function<T, String> function) {
        List<String> list = new ArrayList<>();
        if (calls.isEmpty()) {
            list.add(prefix);
            return list;
        }
        for (Hierarchy<T> call : calls) {
            String s = String.format("%s -> %s", prefix, function.apply(call.target));
            if (call.getRequestMapping() != null && !call.getRequestMapping().equals("")) {
                String requestMapping = call.getRequestMapping();
                s = s + "(url: " + requestMapping + ")";
            }
            if (call.getComment() != null && !call.getComment().equals("")) {
                s = s + "【" + call.getComment() + "】";
            }
            if (call.getCalls().isEmpty()) {
                list.add(s);
            } else {
                List<String> childList = toStringList(s, call.getCalls(), function);
                list.addAll(childList);
            }
        }
        return list;
    }

    public Function<T, String> getFunction() {
        return declaration -> {
            if (declaration instanceof ResolvedMethodDeclaration ) {
                ResolvedMethodDeclaration resolvedMethodDeclaration = (ResolvedMethodDeclaration) declaration;
                return resolvedMethodDeclaration.getQualifiedName();
            } else {
                System.err.println("function error: " + declaration.toString());
                return "";
            }
        };
    }

    public DiffLocator.DiffDesc toDiff() {
        DiffLocator.DiffDesc diff = new DiffLocator.DiffDesc();
        diff.module = this.module;
        diff.isFieldDiff = false;
        diff.fieldDesc = null;
        if (target instanceof ResolvedMethodDeclaration ) {
            ResolvedMethodDeclaration methodDeclaration = (ResolvedMethodDeclaration) target;
            diff.methodDesc = new DiffLocator.MethodDesc(
                    methodDeclaration.getPackageName(),
                    methodDeclaration.getClassName(),
                    methodDeclaration.getName()
            );
        }

        return diff;
    }
}
