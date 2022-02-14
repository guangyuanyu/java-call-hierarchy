package org.joker.java.call.hierarchy;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
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

    public JavaParserProxy javaParserProxy = new JavaParserProxy();
    public ProjectRoot projectRoot;

    List<SourceRoot> sourceRoots = null;
    Map<Path, List<CompilationUnit>> compilationUnitMap = new HashMap<>();

    public CallHierarchy(Config config) throws IOException {
        ParserConfiguration parserConfiguration = JavaParserConfiguration.getParserConfiguration(config.getProjectPath(), config.getDependencyProjectPathSet(), config.getDependencyJarPathSet());
        projectRoot = new SymbolSolverCollectionStrategy(parserConfiguration).collect(Paths.get(config.getProjectPath()));
    }

    public List<ResolvedMethodDeclaration> parseMethod(String module, String packageName, String javaName, String methodName) throws IOException {
        List<ResolvedMethodDeclaration> list = new ArrayList<>();
        String classQualifiedName = String.format("%s.%s", packageName, javaName);
        String methodQualifiedName = String.format("%s.%s.%s", packageName, javaName, methodName);
        if (this.sourceRoots == null) {
            sourceRoots = javaParserProxy.getSourceRoots(projectRoot);
        }
        for (SourceRoot sourceRoot : sourceRoots) {
            if (!sourceRoot.getRoot().toAbsolutePath().toString().contains(module)
                    && !module.contains("common") && !module.contains("baseModule")) {
                continue;
            }

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
                        .filter(m -> {
                            return !(m.getQualifiedName().equals(methodQualifiedName));
                        }) // 过滤递归调用
                        .collect(Collectors.toList());
                list.addAll(resolvedMethodDeclarations);
            }
        }
        return list;
    }

    public void printParseMethod(String module, String packageName, String javaName, String methodName) throws IOException {
        System.out.println("------ start print method call ------");
        parseMethod(module, packageName, javaName, methodName)
                .stream()
                .map(ResolvedMethodDeclaration::getQualifiedSignature)
                .forEach(System.out::println);
        System.out.println("------  end print method call  ------");
    }

    public List<Hierarchy<ResolvedMethodDeclaration>> parseMethodRecursion(String module, String packageName, String javaName, String methodName) throws IOException {
        List<ResolvedMethodDeclaration> resolvedMethodDeclarations = parseMethod(module, packageName, javaName, methodName);
        if (resolvedMethodDeclarations.isEmpty()) {
            return Collections.emptyList();
        }

        List<Hierarchy<ResolvedMethodDeclaration>> hierarchies = resolvedMethodDeclarations.stream().map(Hierarchy::new).collect(Collectors.toList());
        for (Hierarchy<ResolvedMethodDeclaration> hierarchy : hierarchies) {
            parseMethodRecursion(module, hierarchy);
        }

        return hierarchies;
    }

    public void parseMethodRecursion(String module, Hierarchy<ResolvedMethodDeclaration> hierarchy) throws IOException {
        processControllerMethod(hierarchy);
        ResolvedMethodDeclaration target = hierarchy.getTarget();
        List<ResolvedMethodDeclaration> resolvedMethodDeclarations = parseMethod(module, target.getPackageName(), target.getClassName(), target.getName());
        if (resolvedMethodDeclarations.isEmpty()) {
            return;
        }
        for (ResolvedMethodDeclaration resolvedMethodDeclaration : resolvedMethodDeclarations) {
            Hierarchy<ResolvedMethodDeclaration> call = new Hierarchy<>(resolvedMethodDeclaration);
            hierarchy.addCall(call);

            processControllerMethod(call);

            if (hierarchy.getRequestMapping() != null && !hierarchy.getRequestMapping().equals("")) {
                continue;
            }
            parseMethodRecursion(module, call);
        }
    }

    /**
     * 处理controller层的调用
     * @param call
     */
    private void processControllerMethod(Hierarchy<ResolvedMethodDeclaration> call) {
        String requestMappingValue = "";
        MethodDeclaration methodDeclaration = ((JavaParserMethodDeclaration) call.getTarget()).getWrappedNode();

        // 处理requestMapping的值
        NodeList<AnnotationExpr> annotations = methodDeclaration.getAnnotations();
        for (AnnotationExpr annotation : annotations) {
            if ("RequestMapping".equals(annotation.getName().toString())) {
                requestMappingValue = extractRequestMapping(annotations);
                if (methodDeclaration.getParentNode().isPresent()) {
                    Node parent = methodDeclaration.getParentNode().get();
                    ClassOrInterfaceDeclaration classNode = (ClassOrInterfaceDeclaration) parent;
                    NodeList<AnnotationExpr> annoList = classNode.getAnnotations();
                    String classRequestMapping = extractRequestMapping(annoList);
                    requestMappingValue = Paths.get(classRequestMapping, requestMappingValue).toString();
                }

                // 处理comment
                methodDeclaration.getComment().ifPresent(comment -> {
                    String c = Arrays.stream(comment.getContent().split("\n")).filter(s -> !s.trim().startsWith("* @"))
                            .reduce((s1, s2) -> s1 + "\t" + s2).orElse("").replace("\n", "").replace("\r", "");
                    call.setComment(c);
                });

                break;
            }
        }
        call.setRequestMapping(requestMappingValue);
    }

    private String extractRequestMapping(NodeList<AnnotationExpr> annoList) {
        String value = "";
        for (AnnotationExpr annotationExpr : annoList) {
            if ("RequestMapping".equals(annotationExpr.getName().toString())) {
                List<Node> children = annotationExpr.getChildNodes();
                for (Node childNode : children) {
                    if (childNode instanceof MemberValuePair) {
                        MemberValuePair pair = (MemberValuePair) childNode;
                        String id = pair.getName().getId();
                        if ("value".equals(id)) {
                            value = pair.getValue().asLiteralStringValueExpr().getValue();
                        }
                    } else if (childNode instanceof StringLiteralExpr) {
                        StringLiteralExpr expr = (StringLiteralExpr) childNode;
                        value = expr.getValue();
                    }
                }
            }
        }
        return value;
    }

    public void printParseMethodRecursion(String module, String packageName, String javaName, String methodName) throws IOException {
        System.out.println("------ start print method call recursion ------");
        parseMethodRecursion(module, packageName, javaName, methodName)
                .stream()
                .map(Hierarchy::toStringList)
                .flatMap(Collection::stream)
                .sorted()
                .distinct()
                .forEach(System.out::println);
        System.out.println("------  end print method call recursion  ------");
    }

}
