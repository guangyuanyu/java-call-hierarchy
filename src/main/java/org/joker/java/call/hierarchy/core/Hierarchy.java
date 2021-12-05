package org.joker.java.call.hierarchy.core;

import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Hierarchy<T> {

    private T target;
    private List<Hierarchy<T>> calls;

    public Hierarchy(T target) {
        this(target, new ArrayList<>());
    }

    public Hierarchy(T target, List<Hierarchy<T>> calls) {
        this.target = target;
        this.calls = calls;
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

    public void addCall(Hierarchy<T> call) {
        calls.add(call);
    }

    public List<String> toStringList() {
        if (target instanceof ResolvedMethodDeclaration) {
            return toStringList(((ResolvedMethodDeclaration) target).getQualifiedSignature(), calls, hierarchy -> ((ResolvedMethodDeclaration) hierarchy.getTarget()).getQualifiedSignature());
        }
        return toStringList("", calls, Object::toString);
    }

    public List<String> toStringList(String prefix, List<Hierarchy<T>> calls, Function<Hierarchy<T>, String> function) {
        List<String> list = new ArrayList<>();
        for (Hierarchy<T> call : calls) {
            String s = String.format("%s -> %s", prefix, function.apply(call));
            if (call.getCalls().isEmpty()) {
                list.add(s);
            } else {
                List<String> childList = toStringList(s, call.getCalls(), function);
                list.addAll(childList);
            }
        }
        return list;
    }

}
