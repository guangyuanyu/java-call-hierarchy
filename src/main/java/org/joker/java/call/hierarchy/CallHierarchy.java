package org.joker.java.call.hierarchy;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import org.joker.java.call.hierarchy.core.Hierarchy;
import org.joker.java.call.hierarchy.core.JavaParserConfiguration;
import org.joker.java.call.hierarchy.core.JavaParserProxy;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CallHierarchy {

    private JavaParserProxy javaParserProxy = new JavaParserProxy();
    private ProjectRoot projectRoot;

    List<SourceRoot> sourceRoots = null;
    Map<Path, List<CompilationUnit>> compilationUnitMap = new HashMap<>();

    public CallHierarchy(Config config) throws IOException {
        ParserConfiguration parserConfiguration = JavaParserConfiguration.getParserConfiguration(config.getProjectPath(), config.getDependencyProjectPathSet(), config.getDependencyJarPathSet());
        projectRoot = new SymbolSolverCollectionStrategy(parserConfiguration).collect(Paths.get(config.getProjectPath()));
    }

    public List<ResolvedMethodDeclaration> parseMethod(String packageName, String javaName, String methodName) throws IOException {
        List<ResolvedMethodDeclaration> list = new ArrayList<>();
        String classQualifiedName = String.format("%s.%s", packageName, javaName);
        String methodQualifiedName = String.format("%s.%s.%s", packageName, javaName, methodName);
        if (this.sourceRoots == null) {
            sourceRoots = javaParserProxy.getSourceRoots(projectRoot);
        }
        for (SourceRoot sourceRoot : sourceRoots) {
            SymbolResolver symbolResolver = sourceRoot.getParserConfiguration().getSymbolResolver().get();
            List<CompilationUnit> compilationUnits = compilationUnitMap.get(sourceRoot.getRoot());
            if (compilationUnits == null) {
                compilationUnits = javaParserProxy.getCompilationUnits(sourceRoot);
                compilationUnitMap.put(sourceRoot.getRoot(), compilationUnits);
            }
            for (CompilationUnit compilationUnit : compilationUnits) {
                List<ResolvedMethodDeclaration> resolvedMethodDeclarations = compilationUnit.findAll(MethodCallExpr.class)
                        .stream()
                        .filter(f -> f.getNameAsString().equals(methodName))
                        .filter(f -> {
                            try {
                                return f.resolve().getQualifiedName().equals(methodQualifiedName);
                            } catch (Exception e) {
                                if (!f.getScope().isPresent()) {
                                    return JavaParseUtil.getParentNode(f, ClassOrInterfaceDeclaration.class).getFullyQualifiedName().get().equals(classQualifiedName);
                                }
                                ResolvedType resolvedType;
                                try {
                                    resolvedType = symbolResolver.calculateType(f.getScope().get());
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

    public void printParseMethod(String packageName, String javaName, String methodName) throws IOException {
        System.out.println("------ start print method call ------");
        parseMethod(packageName, javaName, methodName)
                .stream()
                .map(ResolvedMethodDeclaration::getQualifiedSignature)
                .forEach(System.out::println);
        System.out.println("------  end print method call  ------");
    }

    public List<Hierarchy<ResolvedMethodDeclaration>> parseMethodRecursion(String packageName, String javaName, String methodName) throws IOException {
        List<ResolvedMethodDeclaration> resolvedMethodDeclarations = parseMethod(packageName, javaName, methodName);
        if (resolvedMethodDeclarations.isEmpty()) {
            return Collections.emptyList();
        }

        List<Hierarchy<ResolvedMethodDeclaration>> hierarchies = resolvedMethodDeclarations.stream().map(Hierarchy::new).collect(Collectors.toList());
        for (Hierarchy<ResolvedMethodDeclaration> hierarchy : hierarchies) {
            parseMethodRecursion(hierarchy);
        }

        return hierarchies;
    }

    public void parseMethodRecursion(Hierarchy<ResolvedMethodDeclaration> hierarchy) throws IOException {
        ResolvedMethodDeclaration target = hierarchy.getTarget();
        List<ResolvedMethodDeclaration> resolvedMethodDeclarations = parseMethod(target.getPackageName(), target.getClassName(), target.getName());
        if (resolvedMethodDeclarations.isEmpty()) {
            return;
        }
        for (ResolvedMethodDeclaration resolvedMethodDeclaration : resolvedMethodDeclarations) {
            Hierarchy<ResolvedMethodDeclaration> call = new Hierarchy<>(resolvedMethodDeclaration);
            hierarchy.addCall(call);

            parseMethodRecursion(call);
        }
    }

    public void printParseMethodRecursion(String packageName, String javaName, String methodName) throws IOException {
        System.out.println("------ start print method call recursion ------");
        parseMethodRecursion(packageName, javaName, methodName)
                .stream()
                .map(Hierarchy::toStringList)
                .flatMap(Collection::stream)
                .sorted()
                .distinct()
                .forEach(System.out::println);
        System.out.println("------  end print method call recursion  ------");
    }

}
