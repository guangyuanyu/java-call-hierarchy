package org.joker.java.call.hierarchy;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import org.joker.java.call.hierarchy.core.JavaParserConfiguration;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CallHierarchy {

    private ProjectRoot projectRoot;

    public CallHierarchy(Config config) throws IOException {
        ParserConfiguration parserConfiguration = JavaParserConfiguration.getParserConfiguration(config.getProjectPath(), config.getDependencyProjectPathSet(), config.getDependencyJarPathSet());
        projectRoot = new SymbolSolverCollectionStrategy(parserConfiguration).collect(Paths.get(config.getProjectPath()));
    }

    public List<MethodDeclaration> parseMethodList(String packageName, String javaName) throws IOException {
        List<MethodDeclaration> list = new ArrayList<>(2);
        List<SourceRoot> sourceRootList = projectRoot.getSourceRoots();
        for (SourceRoot sourceRoot : sourceRootList) {
            ParseResult<CompilationUnit> parseResult = sourceRoot.tryToParse(packageName, javaName + ".java");
            List<MethodDeclaration> methodDeclarationList = parseResult.getResult()
                    .orElseThrow()
                    .findAll(MethodDeclaration.class);
            list.addAll(methodDeclarationList);
        }
        return list;
    }

    public void printMethod(String packageName, String javaName) throws IOException {
        System.out.println("------ start print method qualified signature ------");
        parseMethodList(packageName, javaName).forEach(f -> System.out.println(f.resolve().getQualifiedSignature()));
        System.out.println("------  end print method qualified signature  ------");
    }

    public Set<ResolvedMethodDeclaration> getResolvedMethodDeclarationSet(MethodDeclaration methodDeclaration) throws IOException {
        Set<ResolvedMethodDeclaration> set = new HashSet<>(2);
        List<SourceRoot> sourceRootList = projectRoot.getSourceRoots();
        for (SourceRoot sourceRoot : sourceRootList) {
            sourceRoot.tryToParse();
            List<CompilationUnit> compilationUnitList = sourceRoot.getCompilationUnits();
            for (CompilationUnit cu : compilationUnitList) {
                List<MethodCallExpr> methodCallExprList = cu.findAll(MethodCallExpr.class);
                Set<ResolvedMethodDeclaration> methodCallSet = JavaParseUtil.getMethodCallSet(methodCallExprList, methodDeclaration);
                set.addAll(methodCallSet);
            }
        }
        return set;
    }

    public void printMethodCall(String packageName, String javaName, String methodQualifiedSignature) throws IOException {
        MethodDeclaration methodDeclaration = parseMethodList(packageName, javaName).stream().filter(f -> f.resolve().getQualifiedSignature().equals(methodQualifiedSignature)).findAny().orElseThrow();
        Set<ResolvedMethodDeclaration> resolvedMethodDeclarationSet = getResolvedMethodDeclarationSet(methodDeclaration);
        System.out.println("------ start print method call ------");
        resolvedMethodDeclarationSet.forEach(f -> System.out.println(f.getQualifiedSignature()));
        System.out.println("------  end print method call  ------");
    }

}
