package org.joker.java.call.hierarchy.core;

import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

public class MethodDelarationWrapper {

    private ResolvedMethodDeclaration resolvedMethodDeclaration;

    private String module;

    public MethodDelarationWrapper(ResolvedMethodDeclaration resolvedMethodDeclaration, String module) {
        this.resolvedMethodDeclaration = resolvedMethodDeclaration;
        this.module = module;
    }

    public ResolvedMethodDeclaration getResolvedMethodDeclaration() {
        return resolvedMethodDeclaration;
    }

    public void setResolvedMethodDeclaration(ResolvedMethodDeclaration resolvedMethodDeclaration) {
        this.resolvedMethodDeclaration = resolvedMethodDeclaration;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }
}
