package org.joker.java.call.hierarchy;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JavaParseUtil {

    public static TypeSolver buildTypeSolver(Set<String> dependencyProjectPathSet, Set<String> dependencyJarPathSet) throws IOException {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();

        combinedTypeSolver.add(new ReflectionTypeSolver(false));

        if (dependencyProjectPathSet != null) {
            for (String dependencyProjectPath : dependencyProjectPathSet) {
                JavaParserTypeSolver javaParserTypeSolver = new JavaParserTypeSolver(Path.of(dependencyProjectPath));
                combinedTypeSolver.add(javaParserTypeSolver);
            }
        }

        if (dependencyJarPathSet != null) {
            for (String dependencyJarPath : dependencyJarPathSet) {
                JarTypeSolver jarTypeSolver = new JarTypeSolver(Path.of(dependencyJarPath));
                combinedTypeSolver.add(jarTypeSolver);
            }
        }

        return combinedTypeSolver;
    }

    public static ParserConfiguration buildParserConfiguration(Set<String> dependencyProjectPathSet, Set<String> dependencyJarPathSet) throws IOException {
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setAttributeComments(false);
        parserConfiguration.setSymbolResolver(new JavaSymbolSolver(buildTypeSolver(dependencyProjectPathSet, dependencyJarPathSet)));
        return parserConfiguration;
    }

    public static <T extends Node> T getParentNode(Node node, Class<T> nodeType) {
        if (node == null || node.getParentNode().isEmpty()) {
            return null;
        }

        Node parentNode = node.getParentNode().get();

        if (nodeType.isAssignableFrom(parentNode.getClass())) {
            return (T) parentNode;
        } else {
            return getParentNode(parentNode, nodeType);
        }
    }

    public static Set<ResolvedMethodDeclaration> getMethodCallSet(List<MethodCallExpr> methodCallExprList, MethodDeclaration methodDeclaration) {
        ResolvedMethodDeclaration srcMethod = methodDeclaration.resolve();
        Set<ResolvedMethodDeclaration> dstMethodSet = new HashSet<>(2);

        for (MethodCallExpr methodCallExpr : methodCallExprList) {
            try {
                ResolvedMethodDeclaration callMethod = methodCallExpr.resolve();
                if (callMethod.getQualifiedSignature().equals(srcMethod.getQualifiedSignature())) {
                    MethodDeclaration disMethod = getParentNode(methodCallExpr, MethodDeclaration.class);
                    if (disMethod != null) {
                        dstMethodSet.add(disMethod.resolve());
                    }
                }
            } catch (Exception e) {
                System.err.printf("resolve error: %s%n", methodCallExpr);
            }
        }

        return dstMethodSet;
    }

}
