package org.joker.java.call.hierarchy;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import org.joker.java.call.hierarchy.core.Hierarchy;
import org.joker.java.call.hierarchy.core.JavaParserConfiguration;
import org.joker.java.call.hierarchy.core.JavaParserProxy;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CallHierarchy {

    private JavaParserProxy javaParserProxy = new JavaParserProxy();
    private ProjectRoot projectRoot;

    public CallHierarchy(Config config) throws IOException {
        ParserConfiguration parserConfiguration = JavaParserConfiguration.getParserConfiguration(config.getProjectPath(), config.getDependencyProjectPathSet(), config.getDependencyJarPathSet());
        projectRoot = new SymbolSolverCollectionStrategy(parserConfiguration).collect(Paths.get(config.getProjectPath()));
    }

    public List<ResolvedMethodDeclaration> parseMethod(String packageName, String javaName, String methodName) throws IOException {
        List<ResolvedMethodDeclaration> list = new ArrayList<>();
        String methodQualifiedSignature = String.format("%s.%s.%s", packageName, javaName, methodName);
        List<SourceRoot> sourceRoots = javaParserProxy.getSourceRoots(projectRoot);
        for (SourceRoot sourceRoot : sourceRoots) {
            List<CompilationUnit> compilationUnits = javaParserProxy.getCompilationUnits(sourceRoot);
            for (CompilationUnit compilationUnit : compilationUnits) {
                List<ResolvedMethodDeclaration> resolvedMethodDeclarations = compilationUnit.findAll(MethodCallExpr.class)
                        .stream()
                        .filter(f -> f.getNameAsString().contains(methodName))
                        .filter(f -> f.resolve().getQualifiedSignature().contains(methodQualifiedSignature))
                        .map(m -> JavaParseUtil.getParentNode(m, MethodDeclaration.class).resolve())
                        .toList();
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

        List<Hierarchy<ResolvedMethodDeclaration>> hierarchies = resolvedMethodDeclarations.stream().map(Hierarchy::new).toList();
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
