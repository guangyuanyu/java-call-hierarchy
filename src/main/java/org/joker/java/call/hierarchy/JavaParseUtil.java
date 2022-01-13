package org.joker.java.call.hierarchy;

import com.github.javaparser.ast.Node;

public class JavaParseUtil {

    public static <T extends Node> T getParentNode(Node node, Class<T> nodeType) {
        if (node == null || !node.getParentNode().isPresent()) {
            return null;
        }

        Node parentNode = node.getParentNode().get();

        if (nodeType.isAssignableFrom(parentNode.getClass())) {
            return (T) parentNode;
        } else {
            return getParentNode(parentNode, nodeType);
        }
    }

}
