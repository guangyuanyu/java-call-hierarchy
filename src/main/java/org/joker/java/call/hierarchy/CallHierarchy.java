package org.joker.java.call.hierarchy;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.Pair;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.base.Joiner;
import com.google.common.collect.*;
import org.joker.java.call.hierarchy.core.*;
import org.joker.java.call.hierarchy.utils.LambdaUtils;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CallHierarchy {

    public JavaParserProxy javaParserProxy = new JavaParserProxy();
    public ProjectRoot projectRoot;

    List<SourceRoot> sourceRoots = null;
    Map<Path, List<CompilationUnit>> compilationUnitMap = new WeakHashMap<>();
//    Map<String, List<Hierarchy>> methodName2hierarchyMap = Maps.newHashMap();
    ListMultimap<String, MethodDelarationWrapper> methodName2hierarchyMap = ArrayListMultimap.create();

    public CallHierarchy(Config config) throws IOException {
        ParserConfiguration parserConfiguration = JavaParserConfiguration.getParserConfiguration(config.getProjectPath(), config.getDependencyProjectPathSet(), config.getDependencyJarPathSet());
        projectRoot = new SymbolSolverCollectionStrategy(parserConfiguration).collect(Paths.get(config.getProjectPath()));
    }

    /**
     * 尝试用parser定位到改动的method的调用方
     * @param module
     * @param packageName
     * @param javaName
     * @param methodName
     * @return
     * @throws IOException
     */
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
                            if (f.toString().equals("super.preHandle(request, response, handler)")) {
                                return false;
                            }
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

    /**
     * 判断改source 是否需要分析
     * @param modules
     * @param sourceRoot
     * @return
     */
    private boolean needAnalyzeSource(List<String> modules, SourceRoot sourceRoot) {
        HashSet<String> moduleSet = Sets.newHashSet(modules);
        System.out.println("analyze modules: "+ Joiner.on('，').join(moduleSet));
        String sourcePath = sourceRoot.getRoot().toAbsolutePath().toString();

        /**
         * 如果改了base功能，则都需要分析
         */
        if (moduleSet.contains("common") || moduleSet.contains("baseModule")) {
            return true;
        }

        /**
         * 如果该module被改了则需要分析
         */
        for (String module : moduleSet) {
            if (sourcePath.contains(module)) {
                return true;
            }
        }

        /**
         * 默认不需要分析
         */
        return false;
    }

    /**
     * 提取sourceRoot所在的module
     * @param sourceRoot 目前分析的模块
     * @return
     */
    private String extractModule(SourceRoot sourceRoot) {
        String module = "";
        String sourcePath = sourceRoot.getRoot().toAbsolutePath().toString();

        int index = sourcePath.indexOf("/eagle-parent/");
        if (index < 0) {
            module = "";
            return module;
        }
        String temp = sourcePath.substring(index + "/eagle-parent/".length());
        int end = temp.indexOf("/");
        module = temp.substring(0, end);

        if (module.startsWith("eagle-")) {
            module = module.substring(6);
        } else if (module.startsWith("zxjt-")) {
            module = module.substring(5);
        }

        return module;
    }

    private String methodMapKeyGenerate(String module, String qualifyMethod) {
        return module + qualifyMethod;
    }

    /**
     * 批量 尝试用parser定位到改动的method
     * 主要为了加速
     * @param diffs
     * @return
     * @throws IOException
     */
    public ArrayListMultimap<String, MethodDelarationWrapper> batchParseMethods(List<DiffLocator.DiffDesc> diffs) throws IOException {
        //批量生产包名 and 方法名
        List<String> qualifiedMethodNames = new ArrayList<>();
        List<String> qualifiedClassNames = new ArrayList<>();
        List<String> methods = new ArrayList<>();
        List<String> modules = new ArrayList<>();

        // 保存分析结果
        ArrayListMultimap<String, MethodDelarationWrapper> calleeToCallerMap = ArrayListMultimap.create();
        for (DiffLocator.DiffDesc diff : diffs) {
            String classQualifiedName = String.format("%s.%s", diff.methodDesc.packageName, diff.methodDesc.className);
            String methodQualifiedName = String.format("%s.%s.%s", diff.methodDesc.packageName, diff.methodDesc.className, diff.methodDesc.methodName);
            String key = methodMapKeyGenerate(diff.module, methodQualifiedName);
            if (methodName2hierarchyMap.containsKey(key)) { // 如果已经分析过这个方法就不再分析了
                calleeToCallerMap.putAll(methodQualifiedName, methodName2hierarchyMap.get(key));
                continue;
            }
            qualifiedMethodNames.add(methodQualifiedName);
            qualifiedClassNames.add(classQualifiedName);
            methods.add(diff.methodDesc.methodName);
            modules.add(diff.module);
        }

        if (this.sourceRoots == null) {
            sourceRoots = javaParserProxy.getSourceRoots(projectRoot);
        }

//        System.out.println("source root size: " + sourceRoots.size());
        sourceRoots.parallelStream().forEach(sourceRoot -> {
            try {
                processOneRoot(modules, qualifiedMethodNames, qualifiedClassNames, methods, calleeToCallerMap, sourceRoot);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

//        for (SourceRoot sourceRoot : sourceRoots) {
//            processOneRoot(diffs, qualifiedMethodNames, qualifiedClassNames, methods, calleeToCallerMap, sourceRoot);
//        }
        return calleeToCallerMap;
    }

    /**
     * 批量 尝试用parser定位到改动的method
     * 主要为了加速
     * @param diffs
     * @return
     * @throws IOException
     */
    public ArrayListMultimap<String, MethodDelarationWrapper> batchParseMethods_v2(List<DiffLocator.DiffDesc> diffs) throws IOException {
        //批量生产包名 and 方法名
        List<String> qualifiedMethodNames = new ArrayList<>();
        List<String> qualifiedClassNames = new ArrayList<>();
        List<String> methods = new ArrayList<>();
        List<String> modules = new ArrayList<>();

        // 保存分析结果
        ArrayListMultimap<String, MethodDelarationWrapper> calleeToCallerMap = ArrayListMultimap.create();
        for (DiffLocator.DiffDesc diff : diffs) {
            String classQualifiedName = String.format("%s.%s", diff.methodDesc.packageName, diff.methodDesc.className);
            String methodQualifiedName = String.format("%s.%s.%s", diff.methodDesc.packageName, diff.methodDesc.className, diff.methodDesc.methodName);
            String key = methodMapKeyGenerate(diff.module, methodQualifiedName);
            if (methodName2hierarchyMap.containsKey(key)) { // 如果已经分析过这个方法就不再分析了
                calleeToCallerMap.putAll(methodQualifiedName, methodName2hierarchyMap.get(key));
                continue;
            }
            qualifiedMethodNames.add(methodQualifiedName);
            qualifiedClassNames.add(classQualifiedName);
            methods.add(diff.methodDesc.methodName);
            modules.add(diff.module);
        }

        if (this.sourceRoots == null) {
            sourceRoots = javaParserProxy.getSourceRoots(projectRoot);
        }

        sourceRoots.parallelStream().forEach(sourceRoot -> {
            try {
                processOneRoot_v2(modules, qualifiedMethodNames, qualifiedClassNames, methods, calleeToCallerMap, sourceRoot);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

//        for (SourceRoot sourceRoot : sourceRoots) {
//            processOneRoot(diffs, qualifiedMethodNames, qualifiedClassNames, methods, calleeToCallerMap, sourceRoot);
//        }
        return calleeToCallerMap;
    }

    /**
     * parse单个module
     * @param modules
     * @param qualifiedMethodNames
     * @param qualifiedClassNames
     * @param methods
     * @param calleeToCallerMap
     * @param sourceRoot
     * @throws IOException
     */
    private void processOneRoot(List<String> modules, List<String> qualifiedMethodNames, List<String> qualifiedClassNames, List<String> methods, ArrayListMultimap<String, MethodDelarationWrapper> calleeToCallerMap, SourceRoot sourceRoot) throws IOException {
        System.out.println("process one root " + sourceRoot.getRoot().toString() + " module size:"+modules.size() + " method size:"+qualifiedMethodNames.size() + " class size:"+qualifiedClassNames.size() + " origin method size:"+methods.size());
//        System.out.println("process methods: " + Joiner.on(";").join(qualifiedMethodNames));
        // 不需要分析则跳过
//        if (!needAnalyzeSource(modules, sourceRoot)) {
//            return;
//        }
        // source所在module
        String module = extractModule(sourceRoot);
        // 替换needAnalyzeSource方法
        List<String> filterModules = Lists.newArrayList();
        List<String> filterQualifiedMethods = Lists.newArrayList();
        List<String> filterQualifiedClazzs = Lists.newArrayList();
        List<String> filterMethods = Lists.newArrayList();
        for (int i = 0; i < modules.size(); i++) {
            String m = modules.get(i);
            if (m.equals("baseModule") || m.equals("common") || m.equals(module)) {
                filterModules.add(m);
                filterQualifiedMethods.add(qualifiedMethodNames.get(i));
                filterQualifiedClazzs.add(qualifiedClassNames.get(i));
                filterMethods.add(methods.get(i));
            }
        }
        if (filterMethods.size() <= 0) {
            return;
        }


        SymbolResolver symbolResolver = sourceRoot.getParserConfiguration().getSymbolResolver().get();
        List<CompilationUnit> compilationUnits = compilationUnitMap.get(sourceRoot.getRoot());
        if (compilationUnits == null) {
            compilationUnits = javaParserProxy.getCompilationUnits(sourceRoot);
            compilationUnitMap.put(sourceRoot.getRoot(), compilationUnits);
        }

        //Todo 尝试多线程并行parse unit
//        compilationUnits.parallelStream().forEach(compilationUnit ->
//                processCallerFromUnit(module, qualifiedMethodNames, qualifiedClassNames, methods, calleeToCallerMap, symbolResolver, compilationUnit));
        for (CompilationUnit compilationUnit : compilationUnits) {
            processCallerFromUnit(module, filterQualifiedMethods, filterQualifiedClazzs, filterMethods, calleeToCallerMap, symbolResolver, compilationUnit);
        }
    }

    private void processOneRoot_v2(List<String> modules, List<String> qualifiedMethodNames, List<String> qualifiedClassNames, List<String> methods, ArrayListMultimap<String, MethodDelarationWrapper> calleeToCallerMap, SourceRoot sourceRoot) throws IOException {
        // 不需要分析则跳过
        if (!needAnalyzeSource(modules, sourceRoot)) {
            return;
        }
        // source所在module
        String module = extractModule(sourceRoot);

        SymbolResolver symbolResolver = sourceRoot.getParserConfiguration().getSymbolResolver().get();
        List<CompilationUnit> compilationUnits = compilationUnitMap.get(sourceRoot.getRoot());
        if (compilationUnits == null) {
            compilationUnits = javaParserProxy.getCompilationUnits(sourceRoot);
            compilationUnitMap.put(sourceRoot.getRoot(), compilationUnits);
        }

        //Todo 尝试多线程并行parse unit
//        compilationUnits.parallelStream().forEach(compilationUnit ->
//                processCallerFromUnit(module, qualifiedMethodNames, qualifiedClassNames, methods, calleeToCallerMap, symbolResolver, compilationUnit));
        for (CompilationUnit compilationUnit : compilationUnits) {
            processCallerFromUnit(module, qualifiedMethodNames, qualifiedClassNames, methods, calleeToCallerMap, symbolResolver, compilationUnit);
        }
    }

    /**
     * parse 单个unit，通常就是一个package
     * @param module sourceRoot 所在module
     * @param qualifiedMethodNames
     * @param qualifiedClassNames
     * @param methods
     * @param calleeToCallerMap
     * @param symbolResolver
     * @param compilationUnit
     */
    private void processCallerFromUnit(String module, List<String> qualifiedMethodNames, List<String> qualifiedClassNames,
                                       List<String> methods, ArrayListMultimap<String, MethodDelarationWrapper> calleeToCallerMap,
                                       SymbolResolver symbolResolver, CompilationUnit compilationUnit) {
        List<MethodCallExpr> all = compilationUnit.findAll(MethodCallExpr.class);
        ArrayListMultimap<String, MethodDelarationWrapper> localMap = ArrayListMultimap.create();
        for (MethodCallExpr callExpr : all) {
            //首先方法名要一样
            if (!methods.contains(callExpr.getName().getId())) {
                continue;
            }

            // 是否包含在改动范围内
            boolean found = false;
            String qualifiedMethodName = "";
            try {
                String s = callExpr.toString();
                if (canResolve(s)) {
                    String qualifiedName = callExpr.resolve().getQualifiedName();
                    found =  qualifiedMethodNames.contains(qualifiedName);
                    qualifiedMethodName = qualifiedName;
                }
            } catch (Exception e) {
                Pair<Boolean, String> pair = handleParseExeption(qualifiedClassNames, methods, symbolResolver, callExpr);
                found = pair.a;
                qualifiedMethodName = pair.b;
            }
            // 不包含在范围内，跳过
            if (!found) {
                continue;
            }

            ResolvedMethodDeclaration resolvedMethodDeclaration = JavaParseUtil.getParentNode(callExpr, MethodDeclaration.class).resolve();
            // 过滤递归调用，只过滤自己调用自己的直接递归
            if (qualifiedMethodNames.contains(resolvedMethodDeclaration.getQualifiedName())) {
                continue;
            }
            MethodDelarationWrapper wrapper = new MethodDelarationWrapper(resolvedMethodDeclaration, module);
//            calleeToCallerMap.get(qualifiedMethodName).add(wrapper);
            localMap.get(qualifiedMethodName).add(wrapper);
            methodName2hierarchyMap.put(methodMapKeyGenerate(module, qualifiedMethodName), wrapper);
        }
        synchronized (calleeToCallerMap) {
            calleeToCallerMap.putAll(localMap);
        }
    }

    /**
     * 是否可以解析
     * @param s
     * @return
     */
    private boolean canResolve(String s) {
        if (s.startsWith("super") || s.startsWith("filterChain") || s.startsWith("httpclient") ||
                s.startsWith("chain") || s.startsWith("RegistryBuilder") || s.startsWith("request")) {
            return false;
        }
        return true;


//        return !s.equals("super.preHandle(request, response, handler)") &&
//                !s.equals("filterChain.doFilter(servletRequest, servletResponse)") &&
//                !s.equals("filterChain.doFilter(requestWrapper, responseWrapper)") &&
//                !s.equals("filterChain.doFilter(servletRequest, responseWrapper)") &&
//                !s.equals("httpclient.execute(httpPost, responseHandler)") &&
//                !s.equals("httpclient.execute(httpGet, responseHandler)") &&
//                !s.equals("chain.doFilter(requestWrapper, servletResponse)") &&
//                !s.equals("chain.doFilter(servletRequest, servletResponse)") &&
//                !s.equals("super.postHandle(request, response, handler, modelAndView)") &&
//                !s.equals("RegistryBuilder.<ConnectionSocketFactory>create().register(\"http\", PlainConnectionSocketFactory.INSTANCE)") &&
//                !s.equals("RegistryBuilder.<ConnectionSocketFactory>create().register(\"http\", PlainConnectionSocketFactory.getSocketFactory()).register(\"https\", sslSocketFactory)") &&
//                !s.equals("RegistryBuilder.<ConnectionSocketFactory>create().register(\"http\", PlainConnectionSocketFactory.INSTANCE).register(\"https\", sslsf)") &&
//                !s.equals("chain.doFilter(request, response)");
    }

    /**
     * 处理parser 解析异常
     * @param qualifiedClassNames
     * @param methods
     * @param symbolResolver
     * @param callExpr
     * @return
     */
    private Pair<Boolean, String> handleParseExeption(List<String> qualifiedClassNames, List<String> methods, SymbolResolver symbolResolver, MethodCallExpr callExpr) {
        boolean eq = false;
        String qualifiedMethodName = "";
        // 临时记录index
        int index = methods.indexOf(callExpr.getName().getId());
        if (!callExpr.getScope().isPresent()) {
            String qualifiedClassName = JavaParseUtil.getParentNode(callExpr, ClassOrInterfaceDeclaration.class).getFullyQualifiedName().get();
            eq = (qualifiedClassNames.get(index).equals(qualifiedClassName));
            qualifiedMethodName = String.format("%s.%s", qualifiedClassName, callExpr.getNameAsString());
        } else {
            ResolvedType resolvedType = null;
            try {
                resolvedType = symbolResolver.calculateType(callExpr.getScope().get());
            } catch (Exception ex) {
                Optional<String> qualifiedClassName = JavaParseUtil.getParentNode(callExpr, ClassOrInterfaceDeclaration.class).getFullyQualifiedName();
                if (qualifiedClassName.isPresent()) {
                    eq = (qualifiedClassNames.get(index).equals(qualifiedClassName.get()));
                    qualifiedMethodName = String.format("%s.%s", qualifiedClassName.get(), callExpr.getNameAsString());
                }
            }
            if (!eq) {
                if (resolvedType != null && resolvedType.isReferenceType()) {
                    String qualifiedClassName = resolvedType.asReferenceType().getQualifiedName();
                    eq = (qualifiedClassNames.get(index).equals(qualifiedClassName));
                    qualifiedMethodName = String.format("%s.%s", qualifiedClassName, callExpr.getNameAsString());
                } else if (resolvedType != null && resolvedType.isConstraint()) {
                    String qualifiedClassName = resolvedType.asConstraintType().getBound().asReferenceType().getQualifiedName();
                    eq = (qualifiedClassNames.get(index).equals(qualifiedClassName));
                    qualifiedMethodName = String.format("%s.%s", qualifiedClassName, callExpr.getNameAsString());
                } else {
                    System.err.println("resolve error: " + callExpr);
                    eq = false;
                }
            }
        }
        return new Pair(eq, qualifiedMethodName);
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
            //fixme module 有问题，module 应该是调用方的module，即resolvedMethodDeclarations所在的module，不是原始被调用方的module
            parseMethodRecursion(module, hierarchy);
        }

        return hierarchies;
    }

    public List<Hierarchy<ResolvedMethodDeclaration>> batchParseMethodsRecursion(List<DiffLocator.DiffDesc> diffs) throws IOException {
        ArrayListMultimap<String, MethodDelarationWrapper> resolvedMethodDeclarations = batchParseMethods(diffs);
        if (resolvedMethodDeclarations.isEmpty()) {
            return Collections.emptyList();
        }
        System.out.println("batch parse methods from " + diffs.size() + "个 to " + resolvedMethodDeclarations.size() + "个");

        //Todo 从一层调用形成链

        //处理第一层调用
        List<Hierarchy<ResolvedMethodDeclaration>> firstLevelHierarchies = firstLevelMethodFromDiff(diffs);
//        Map<String, Hierarchy<ResolvedMethodDeclaration>> firstLevelMap = LambdaUtils.toMap(firstLevelHierarchies, h -> h.getQualifiedName());
        Map<String, Set<Hierarchy<ResolvedMethodDeclaration>>> firstLevelMap = LambdaUtils.toMultiMap(firstLevelHierarchies, h -> h.getQualifiedName());
        // 上一层调用
        List<Hierarchy<ResolvedMethodDeclaration>> hierarchies = new ArrayList<>();
        for (String key : resolvedMethodDeclarations.keySet()) {
            List<MethodDelarationWrapper> declarations = resolvedMethodDeclarations.get(key);
            List oneHierarchies = declarations.stream().map(d -> new Hierarchy(d.getModule(), d.getResolvedMethodDeclaration())).collect(Collectors.toList());
            hierarchies.addAll(oneHierarchies);
            // 将第一层与上层调用连起来
            Set<Hierarchy<ResolvedMethodDeclaration>> set = firstLevelMap.get(key);
//            Hierarchy<ResolvedMethodDeclaration> firstLevelHierarchy = firstLevelMap.get(key);
//            firstLevelHierarchy.addCalls(oneHierarchies);
            set.forEach(h -> h.addCalls(oneHierarchies));
        }
        batchParseMethodRecursion(hierarchies);

        return firstLevelHierarchies;
    }

    /**
     * 从diff生成qualifiedNames
     * @param diffs
     * @return
     */
    private Map<String, List<String>> generateQualifiedNames(List<DiffLocator.DiffDesc> diffs) {
        //批量生产包名 and 方法名
        List<String> qualifiedMethodNames = new ArrayList<>();
        List<String> qualifiedClassNames = new ArrayList<>();
        List<String> methods = new ArrayList<>();

        // 保存分析结果
        for (DiffLocator.DiffDesc diff : diffs) {
            String classQualifiedName = String.format("%s.%s", diff.methodDesc.packageName, diff.methodDesc.className);
            String methodQualifiedName = String.format("%s.%s.%s", diff.methodDesc.packageName, diff.methodDesc.className, diff.methodDesc.methodName);
            qualifiedMethodNames.add(methodQualifiedName);
            qualifiedClassNames.add(classQualifiedName);
            methods.add(diff.methodDesc.methodName);
        }

        Map<String, List<String>> map = new HashMap<>();
        map.put("qualifiedMethodNames", qualifiedMethodNames);
        map.put("qualifiedClassNames", qualifiedClassNames);
        map.put("methods", methods);

        return map;
    }

    /**
     * 从diff分析到第一层调用
     * @param diffs
     * @return
     */
    private List<Hierarchy<ResolvedMethodDeclaration>> firstLevelMethodFromDiff(List<DiffLocator.DiffDesc> diffs) throws IOException {
        List<Hierarchy<ResolvedMethodDeclaration>> firstLevelHierarchies =
                LambdaUtils.toList(diffs, diff -> new FirstLevelHierarchy(diff.module, diff.methodDesc.packageName, diff.methodDesc.className, diff.methodDesc.methodName));

        // 部分RequestMapping url 和 comment
        if (this.sourceRoots == null) {
            sourceRoots = javaParserProxy.getSourceRoots(projectRoot);
        }

        for (SourceRoot sourceRoot : sourceRoots) {
            populateFirstLevelHierarchies(sourceRoot, firstLevelHierarchies);
        }

        return firstLevelHierarchies;
    }

    /**
     * 补充第一层调用的其他信息
     * @param sourceRoot
     * @param firstLevelHierarchies
     */
    private void populateFirstLevelHierarchies(SourceRoot sourceRoot, List<Hierarchy<ResolvedMethodDeclaration>> firstLevelHierarchies) throws IOException {
        Set<String> modules = LambdaUtils.toSet(firstLevelHierarchies, h -> h.getModule());
        Map<String, List<Hierarchy<ResolvedMethodDeclaration>>> moduleGroup = LambdaUtils.groupBy(firstLevelHierarchies, h -> h.getModule());
        String root = sourceRoot.getRoot().toAbsolutePath().toString();
        boolean needAnalyze = false;

        List<Hierarchy<ResolvedMethodDeclaration>> tmp = null;
        for (Map.Entry<String, List<Hierarchy<ResolvedMethodDeclaration>>> entry : moduleGroup.entrySet()) {
            if (root.contains(entry.getKey())) {
                needAnalyze = true;
                tmp = entry.getValue();
                break;
            }
        }
        if (!needAnalyze) {
            return;
        }
        firstLevelHierarchies = tmp;
        List<CompilationUnit> compilationUnits = compilationUnitMap.get(sourceRoot.getRoot());
        if (compilationUnits == null) {
            compilationUnits = javaParserProxy.getCompilationUnits(sourceRoot);
            compilationUnitMap.put(sourceRoot.getRoot(), compilationUnits);
        }
//        Map<String, Hierarchy<ResolvedMethodDeclaration>> firstLevelMap = LambdaUtils.toMap(firstLevelHierarchies, h -> h.getQualifiedName());
        Map<String, Set<Hierarchy<ResolvedMethodDeclaration>>> firstLevelMap = LambdaUtils.toMultiMap(firstLevelHierarchies, h -> h.getQualifiedName());
        List<String> qualifiedClazzs = LambdaUtils.toList(firstLevelHierarchies, h -> h.getQualifiedClassName());

        boolean targetClass = false;
        boolean found = false;
        for (CompilationUnit compilationUnit : compilationUnits) {
            List<TypeDeclaration> typeDeclarations = compilationUnit.findAll(TypeDeclaration.class);
            for (TypeDeclaration typeDeclaration : typeDeclarations) {
                Optional<String> optional = typeDeclaration.getFullyQualifiedName();
                if (optional.isPresent()) {
                    String s = optional.get();
                    if (qualifiedClazzs.contains(s)) {
                        qualifiedClazzs.remove(s);
                        targetClass = true;
                        found = true;
                        break;
                    }
                }
            }
            if (!targetClass) {
                continue;
            }

            List<String> methodNames = LambdaUtils.toList(firstLevelHierarchies, h -> h.getMethodName());

            List<MethodDeclaration> declarations = compilationUnit.findAll(MethodDeclaration.class);

            for (MethodDeclaration methodDeclaration : declarations) {
                String name = methodDeclaration.getNameAsString();
                if (!methodNames.contains(name)) {
                    continue;
                }
                ResolvedMethodDeclaration resolvedMethodDeclaration = methodDeclaration.resolve();
                String temp = methodDeclaration.getDeclarationAsString(false, false, false);
                String qualifiedMethodName = "";
                try {
                    qualifiedMethodName = String.format("%s.%s.%s",
                            resolvedMethodDeclaration.getPackageName(),
                            resolvedMethodDeclaration.getClassName(),
                            resolvedMethodDeclaration.getName());
                } catch (Exception e) {
                    System.err.println("first level parse ERROR: " + methodDeclaration.getNameAsString());
                    continue;
                }

                Set<Hierarchy<ResolvedMethodDeclaration>> hierarchies = firstLevelMap.get(qualifiedMethodName);
                if (hierarchies == null || hierarchies.size() == 0) {
                    continue;
                }
                String requestMappingValue = "";
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
                            hierarchies.forEach(h -> h.setComment(c));
                        });

                        break;
                    }
                }
                for (Hierarchy<ResolvedMethodDeclaration> hierarchy : hierarchies) {
                    hierarchy.setRequestMapping(requestMappingValue);
                }
            }
        }
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
     * 批量递归method 调用链
     * @param hierarchies
     * @throws IOException
     */
    public void batchParseMethodRecursion(List<Hierarchy<ResolvedMethodDeclaration>> hierarchies) throws IOException {
        System.out.println("start batch parse methods size:" + hierarchies.size());
        // 尝试解析controller层调用
        batchProcessControllerMethod(hierarchies);
        // hierarchy转成diff
        List<DiffLocator.DiffDesc> diffs = hierarchies.stream().map(Hierarchy::toDiff).collect(Collectors.toList());
        ArrayListMultimap<String, MethodDelarationWrapper> resolvedMethodDeclarations = batchParseMethods(diffs);
        System.out.println("end batch parse methods get result size:" + resolvedMethodDeclarations.size());
        if (resolvedMethodDeclarations.isEmpty()) {
            return;
        }

        Map<String, Set<Hierarchy<ResolvedMethodDeclaration>>> methodName2hierarchy = LambdaUtils.toMultiMap(hierarchies, Hierarchy::getQualifiedName);
        //Todo 从一层调用形成链
        List<Hierarchy<ResolvedMethodDeclaration>> callers = new ArrayList<>();
        for (String calleeMethod : resolvedMethodDeclarations.keySet()) {
            // calleeMethod 是被调用method
            List<MethodDelarationWrapper> declarations = resolvedMethodDeclarations.get(calleeMethod);
            List<Hierarchy<ResolvedMethodDeclaration>> oneHierarchies = LambdaUtils.toList(declarations, d -> new Hierarchy(d.getModule(), d.getResolvedMethodDeclaration()));
            callers.addAll(oneHierarchies);

            //Todo 设置hierarchy.call
            Set<Hierarchy<ResolvedMethodDeclaration>> callees = methodName2hierarchy.get(calleeMethod);
            callees.forEach(e -> e.addCalls(oneHierarchies));
        }

        batchParseMethodRecursion(callers);
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

    /**
     * 批量处理controller层调用
     * @param hierarchies
     */
    private void batchProcessControllerMethod(List<Hierarchy<ResolvedMethodDeclaration>> hierarchies) {
        for (Hierarchy<ResolvedMethodDeclaration> call : hierarchies) {
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

    public void batchPrintParseMethodsRecursion(List<DiffLocator.DiffDesc> diffs) throws IOException {
        System.out.println("------ start batch print method call recursion ------");
        batchParseMethodsRecursion(diffs)
                .stream()
                .map(Hierarchy::toStringList)
                .flatMap(Collection::stream)
                .sorted()
                .distinct()
                .forEach(System.out::println);
        System.out.println("------ end batch print method call recursion ------");
    }

    public void prettyPrintHierarchy(List<DiffLocator.DiffDesc> diffs, PrintWriter writer) throws IOException {
        // batch parse
        List<Hierarchy<ResolvedMethodDeclaration>> hierarchies = batchParseMethodsRecursion(diffs);
        hierarchies.stream()
                .map(Hierarchy::toStringList)
                .flatMap(Collection::stream)
                .sorted()
                .distinct()
                .forEach(writer::println);
    }
}
