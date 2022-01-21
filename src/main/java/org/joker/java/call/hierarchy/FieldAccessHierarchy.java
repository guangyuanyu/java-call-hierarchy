package org.joker.java.call.hierarchy;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.github.javaparser.utils.SourceRoot;
import org.joker.java.call.hierarchy.core.Hierarchy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FieldAccessHierarchy {

    private CallHierarchy callHierarchy;

    public FieldAccessHierarchy(CallHierarchy callHierarchy) throws IOException {
        this.callHierarchy = callHierarchy;
    }

    public void printFieldAccessRecursion(String module, String packageName, String javaName, String fieldName) throws IOException {
        System.out.println("------ start print field access recursion ------");
        parseFieldAccessRecursion(module, packageName, javaName, fieldName)
                .stream()
                .map(Hierarchy::toStringList)
                .flatMap(Collection::stream)
                .sorted()
                .distinct()
                .forEach(System.out::println);
        System.out.println("------  end print field access recursion  ------");
    }

    public void printControllerMethod(String module, String packageName, String javaName, String fieldName) throws IOException {
        System.out.println("------ start print field access recursion ------");
        parseFieldAccessRecursion(module, packageName, javaName, fieldName)
                .stream()
                .map(Hierarchy::toStringList)
                .flatMap(Collection::stream)
                .sorted()
                .distinct()
                .forEach(System.out::println);
        System.out.println("------  end print field access recursion  ------");
    }

    public List<Hierarchy<ResolvedMethodDeclaration>> parseFieldAccessRecursion(String module, String packageName, String javaName, String fieldName) throws IOException {
        List<ResolvedMethodDeclaration> resolvedMethodDeclarations = parseField(module, packageName, javaName, fieldName);
        if (resolvedMethodDeclarations.isEmpty()) {
            return Collections.emptyList();
        }

        List<Hierarchy<ResolvedMethodDeclaration>> hierarchies = resolvedMethodDeclarations.stream().map(Hierarchy::new).collect(Collectors.toList());
        for (Hierarchy<ResolvedMethodDeclaration> hierarchy : hierarchies) {
            callHierarchy.parseMethodRecursion(module, hierarchy);
        }

        return hierarchies;
    }

    public List<ResolvedMethodDeclaration> parseField(String module, String packageName, String javaName, String fieldName) throws IOException {
        List<ResolvedMethodDeclaration> list = new ArrayList<>();
        String classQualifiedName = String.format("%s.%s", packageName, javaName);
        if (this.callHierarchy.sourceRoots == null) {
            this.callHierarchy.sourceRoots = this.callHierarchy.javaParserProxy.getSourceRoots(this.callHierarchy.projectRoot);
        }
        for (SourceRoot sourceRoot : this.callHierarchy.sourceRoots) {
            if (!sourceRoot.getRoot().toAbsolutePath().toString().contains(module)
                && !module.contains("common") && !module.contains("baseModule")) {
                continue;
            }
            SymbolResolver symbolResolver = sourceRoot.getParserConfiguration().getSymbolResolver().get();
            List<CompilationUnit> compilationUnits = this.callHierarchy.compilationUnitMap.get(sourceRoot.getRoot());
            if (compilationUnits == null) {
                compilationUnits = this.callHierarchy.javaParserProxy.getCompilationUnits(sourceRoot);
                this.callHierarchy.compilationUnitMap.put(sourceRoot.getRoot(), compilationUnits);
            }
            for (CompilationUnit compilationUnit : compilationUnits) {
                List<ResolvedMethodDeclaration> resolvedMethodDeclarations = compilationUnit.findAll(FieldAccessExpr.class)
                        .stream()
                        .filter(f -> f.getNameAsString().equals(fieldName))
                        .filter(f -> {
                            try {
//                                return f.resolve().getName().equals(fieldQualifiedName);
                                return JavaParseUtil.getParentNode(((JavaParserFieldDeclaration)f.resolve()).getWrappedNode(), ClassOrInterfaceDeclaration.class)
                                        .getFullyQualifiedName().get().equals(classQualifiedName);
                            } catch (Exception e) {
                                if (f.getScope() == null) {
                                    return JavaParseUtil.getParentNode(f, ClassOrInterfaceDeclaration.class).getFullyQualifiedName().get().equals(classQualifiedName);
                                }
                                ResolvedType resolvedType;
                                try {
                                    resolvedType = symbolResolver.calculateType(f.getScope());
                                } catch (Exception ex) {
                                    return JavaParseUtil.getParentNode(f, ClassOrInterfaceDeclaration.class).getFullyQualifiedName().get().equals(classQualifiedName);
                                }
                                if (resolvedType.isReferenceType()) {
                                    return resolvedType.asReferenceType().getQualifiedName().equals(classQualifiedName);
                                } else if (resolvedType.isConstraint()) {
                                    return resolvedType.asConstraintType().getBound().asReferenceType().getQualifiedName().equals(classQualifiedName);
                                } else {
                                    System.err.println("resolve error: " + f);
                                    return false;
                                }
                            }
                        })
                        .map(m -> JavaParseUtil.getParentNode(m, MethodDeclaration.class).resolve())
                        .collect(Collectors.toList());
                list.addAll(resolvedMethodDeclarations);
            }
        }
        return list;
    }
}
