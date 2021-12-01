package org.joker.java.call.hierarchy;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JavaParseUtil {

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
