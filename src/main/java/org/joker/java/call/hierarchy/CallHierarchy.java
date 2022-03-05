package org.joker.java.call.hierarchy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

import org.joker.java.call.hierarchy.core.Hierarchy;
import org.joker.java.call.hierarchy.core.ProjectProxy;
import org.joker.java.call.hierarchy.core.ProjectSourceProxy;

public class CallHierarchy {

    private ProjectProxy projectProxy;

    public CallHierarchy(Config config) throws IOException {
        projectProxy = new ProjectProxy(config);
    }

    public List<ResolvedMethodDeclaration> parseMethod(String packageName, String className, String methodName,
            int paramSize)
            throws IOException {
        List<ResolvedMethodDeclaration> list = new ArrayList<>();
        String classQualifiedName = String.format("%s.%s", packageName, className);
        List<ProjectSourceProxy> sourceProxies = projectProxy.getSourceProxies();
        for (ProjectSourceProxy sourceProxy : sourceProxies) {
            SymbolResolver symbolResolver = sourceProxy.getSymbolResolver();
            List<CompilationUnit> compilationUnits = sourceProxy.getCompilationUnits();
            for (CompilationUnit compilationUnit : compilationUnits) {

                // filter call scope
                boolean isPass = compilationUnit.getTypes().stream()
                        .allMatch(f -> f.getNameAsString().equals(className))
                        || compilationUnit.getPackageDeclaration().orElseThrow().getNameAsString().equals(packageName)
                        || compilationUnit.getImports().stream()
                                .anyMatch(f -> f.getNameAsString().equals(classQualifiedName));
                if (!isPass) {
                    continue;
                }

                List<ResolvedMethodDeclaration> resolvedMethodDeclarations = compilationUnit
                        .findAll(MethodCallExpr.class)
                        .stream()
                        .filter(f -> f.getNameAsString().equals(methodName) && f.getArguments().size() == paramSize)
                        .filter(f -> {
                            if (f.getScope().isEmpty()) {
                                return JavaParseUtil.getParentNode(f, ClassOrInterfaceDeclaration.class)
                                        .getFullyQualifiedName().orElseThrow().equals(classQualifiedName);
                            }
                            return symbolResolver.calculateType(f.getScope().get()).describe()
                                    .equals(classQualifiedName);
                        })
                        .map(m -> JavaParseUtil.getParentNode(m, MethodDeclaration.class).resolve())
                        .toList();
                list.addAll(resolvedMethodDeclarations);
            }
        }
        return list;
    }

    public void printParseMethod(String packageName, String className, String methodName, int paramSize)
            throws IOException {
        System.out.println("------ start print method call ------");
        parseMethod(packageName, className, methodName, paramSize)
                .stream()
                .map(ResolvedMethodDeclaration::getQualifiedSignature)
                .forEach(System.out::println);
        System.out.println("------  end print method call  ------");
    }

    public List<Hierarchy<ResolvedMethodDeclaration>> parseMethodRecursion(String packageName, String className,
            String methodName, int paramSize) throws IOException {
        List<ResolvedMethodDeclaration> resolvedMethodDeclarations = parseMethod(packageName, className, methodName,
                paramSize);
        if (resolvedMethodDeclarations.isEmpty()) {
            return Collections.emptyList();
        }

        List<Hierarchy<ResolvedMethodDeclaration>> hierarchies = resolvedMethodDeclarations.stream().map(Hierarchy::new)
                .toList();
        for (Hierarchy<ResolvedMethodDeclaration> hierarchy : hierarchies) {
            parseMethodRecursion(hierarchy);
        }

        return hierarchies;
    }

    public void parseMethodRecursion(Hierarchy<ResolvedMethodDeclaration> hierarchy) throws IOException {
        ResolvedMethodDeclaration target = hierarchy.getTarget();
        List<ResolvedMethodDeclaration> resolvedMethodDeclarations = parseMethod(target.getPackageName(),
                target.getClassName(), target.getName(), target.getNumberOfParams());
        if (resolvedMethodDeclarations.isEmpty()) {
            return;
        }
        for (ResolvedMethodDeclaration resolvedMethodDeclaration : resolvedMethodDeclarations) {
            Hierarchy<ResolvedMethodDeclaration> call = new Hierarchy<>(resolvedMethodDeclaration);
            hierarchy.addCall(call);
            parseMethodRecursion(call);
        }
    }

    public void printParseMethodRecursion(String packageName, String className, String methodName, int paramSize)
            throws IOException {
        System.out.println("------ start print method call recursion ------");
        parseMethodRecursion(packageName, className, methodName, paramSize)
                .stream()
                .map(Hierarchy::toStringList)
                .flatMap(Collection::stream)
                .sorted()
                .distinct()
                .forEach(System.out::println);
        System.out.println("------  end print method call recursion  ------");
    }

}
