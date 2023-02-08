package org.joker.java.call.hierarchy;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedFieldDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.utils.SourceRoot;
import com.google.common.base.Objects;
import org.apache.maven.shared.utils.StringUtils;
import org.joker.java.call.hierarchy.diff.FileDiff;
import org.joker.java.call.hierarchy.diff.LineDiff;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DiffLocator {

    /**
     * 方法描述
     */
    public static class MethodDesc {
        /**
         * 包名
         */
        public String packageName;

        /**
         * 类名
         */
        public String className;

        /**
         * 方法名
         */
        public String methodName;

        public MethodDesc(String packageName, String className, String methodName) {
            this.packageName = packageName;
            this.className = className;
            this.methodName = methodName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MethodDesc that = (MethodDesc) o;
            return Objects.equal(packageName, that.packageName) &&
                    Objects.equal(className, that.className) &&
                    Objects.equal(methodName, that.methodName);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(packageName, className, methodName);
        }

        @Override
        public String toString() {
            return "{" +
                    "\"packageName\": \"" + packageName + '\"' +
                    ", \"className\": \"" + className + '\"' +
                    ", \"methodName\": \"" + methodName + '\"' +
                    '}';
        }
    }

    /**
     * 字段描述
     */
    public static class FieldDesc {
        /**
         * 包名
         */
        public String packageName;

        /**
         * 类名
         */
        public String className;

        /**
         * 字段名
         */
        public String fieldName;

        public FieldDesc(String packageName, String className, String fieldName) {
            this.packageName = packageName;
            this.className = className;
            this.fieldName = fieldName;
        }

        @Override
        public String toString() {
            return "{" +
                    "\"packageName\": \"" + packageName + '\"' +
                    ", \"className\": \"" + className + '\"' +
                    ", \"fieldName\": \"" + fieldName + '\"' +
                    '}';
        }
    }

    /**
     * 代码改动信息
     */
    public static class DiffDesc {
        /**
         * 一个maven project 有多个module时用
         */
        public String module;

        /**
         * 标识改动的是不是field，true-是field，false-method
         * 默认是false
         */
        public boolean isFieldDiff;

        /**
         * 字段改动描述，
         * @see isFieldDiff 是true时才有值
         */
        public FieldDesc fieldDesc;

        /**
         * 方法改动描述，
         * @see isFieldDiff 是false时才有值
         */
        public MethodDesc methodDesc;

        @Override
        public String toString() {
            return "{" +
                    "\"module\":  " + module +
                    "\", isFieldDiff\":  " + isFieldDiff +
                    ", \"fieldDesc\": " + fieldDesc +
                    ", \"methodDesc\": " + methodDesc +
                    '}';
        }
    }

    private CallHierarchy callHierarchy;

    public DiffLocator(CallHierarchy callHierarchy) throws IOException {
        this.callHierarchy = callHierarchy;
    }

    public List<DiffDesc> locate(List<FileDiff> diffs) throws IOException {
        List<DiffDesc> list = new ArrayList<>();

        Iterator<FileDiff> iter = diffs.iterator();
        // 分析新增的代码 - method
        while (iter.hasNext()){
            FileDiff fileDiff = iter.next();
            FileDiff diff = fileDiff.typeDiff(LineDiff.DiffType.ADD);
            List<ResolvedMethodDeclaration> resolvedMethodDeclarations = tryLocateMethod(diff);
            Set<DiffDesc> methodDiffs = resolvedMethodDeclarations.stream().map(r -> {
                DiffDesc diffDesc = new DiffDesc();
                diffDesc.module = diff.getModule();
                diffDesc.isFieldDiff = false;
                diffDesc.methodDesc = new MethodDesc(r.getPackageName(), r.getClassName(), r.getName());
                return diffDesc;
            }).collect(Collectors.toSet());
            if (methodDiffs.size() > 0) {
                list.addAll(methodDiffs);
                iter.remove();
            }
        }

        // 分析新增的代码 - field
        iter = diffs.iterator();
        while (iter.hasNext()) {
            FileDiff fileDiff = iter.next();
            FileDiff diff = fileDiff.typeDiff(LineDiff.DiffType.ADD);
            List<ResolvedFieldDeclaration> resolvedFieldDeclarations = tryLocateField(diff);
            Set<DiffDesc> methodDiffs = resolvedFieldDeclarations.stream().map(r -> {
                DiffDesc diffDesc = new DiffDesc();
                diffDesc.module = diff.getModule();
                diffDesc.isFieldDiff = true;
                diffDesc.fieldDesc = new FieldDesc(diff.diffSet.get(0).packageName, diff.diffSet.get(0).clazzName
                        , r.getName());
                return diffDesc;
            }).collect(Collectors.toSet());
            if (methodDiffs.size() > 0) {
                list.addAll(methodDiffs);
                iter.remove();
            }
        }

        // 对于删除的代码，检查老的代码
        String sourceDir = Main.sourceDir;

        // 代码切换老版本
        if (StringUtils.isNotBlank(Main.oldVersion)) {
            Runtime.getRuntime().exec("cd " + sourceDir + "; git checkout " + Main.oldVersion);
        }

        // 分析删除的代码 - method
        iter = diffs.iterator();
        while (iter.hasNext()) {
            FileDiff fileDiff = iter.next();
            FileDiff diff = fileDiff.typeDiff(LineDiff.DiffType.DELETE);
            List<ResolvedMethodDeclaration> resolvedMethodDeclarations = tryLocateMethod(diff);
            Set<DiffDesc> methodDiffs = resolvedMethodDeclarations.stream().map(r -> {
                DiffDesc diffDesc = new DiffDesc();
                diffDesc.module = diff.getModule();
                diffDesc.isFieldDiff = false;
                diffDesc.methodDesc = new MethodDesc(r.getPackageName(), r.getClassName(), r.getName());
                return diffDesc;
            }).collect(Collectors.toSet());
            if (methodDiffs.size() > 0) {
                list.addAll(methodDiffs);
                iter.remove();
            }
        }

        // 分析删除的代码 - field
        iter = diffs.iterator();
        while (iter.hasNext()) {
            FileDiff fileDiff = iter.next();
            FileDiff diff = fileDiff.typeDiff(LineDiff.DiffType.DELETE);
            List<ResolvedFieldDeclaration> resolvedFieldDeclarations = tryLocateField(diff);
            Set<DiffDesc> methodDiffs = resolvedFieldDeclarations.stream().map(r -> {
                DiffDesc diffDesc = new DiffDesc();
                diffDesc.module = diff.getModule();
                diffDesc.isFieldDiff = true;
                diffDesc.fieldDesc = new FieldDesc(diff.diffSet.get(0).packageName, diff.diffSet.get(0).clazzName
                        , r.getName());
                return diffDesc;
            }).collect(Collectors.toSet());
            if (methodDiffs.size() > 0) {
                list.addAll(methodDiffs);
                iter.remove();
            }
        }

        return list;
    }

    public List<ResolvedMethodDeclaration> tryLocateMethod(FileDiff diff) throws IOException {
        String packageName = diff.diffSet.get(0).packageName;
        String clazzName = diff.diffSet.get(0).clazzName;
        String qualifyName = packageName + "." + clazzName;
        List<LineDiff> origin = diff.diffSet;
        List<LineDiff> diffSet = new ArrayList<>();
        diffSet.addAll(origin);

        if (callHierarchy.sourceRoots == null) {
            callHierarchy.sourceRoots = callHierarchy.javaParserProxy.getSourceRoots(callHierarchy.projectRoot);
        }

        boolean found = false;
        List<ResolvedMethodDeclaration> list = new ArrayList<>();
        for (SourceRoot sourceRoot : callHierarchy.sourceRoots) {
            if (found) {
                break;
            }
            if (diffSet.size() <= 0) {
                break;
            }
            List<CompilationUnit> compilationUnits = callHierarchy.compilationUnitMap.get(sourceRoot.getRoot());
            if (compilationUnits == null) {
                compilationUnits = callHierarchy.javaParserProxy.getCompilationUnits(sourceRoot);
                callHierarchy.compilationUnitMap.put(sourceRoot.getRoot(), compilationUnits);
            }
            for (CompilationUnit compilationUnit : compilationUnits) {
                if (found) {
                    break;
                }
                if (diffSet.size() <= 0) {
                    break;
                }
                boolean targetClass = false;
                List<TypeDeclaration> typeDeclarations = compilationUnit.findAll(TypeDeclaration.class);
                for (TypeDeclaration typeDeclaration : typeDeclarations) {
                    Optional<String> optional = typeDeclaration.getFullyQualifiedName();
                    if (optional.isPresent()) {
                        String s = optional.get();
                        if (s.equals(qualifyName)) {
                            targetClass = true;
                            found = true;
                            break;
                        }
                    }
                }
                if (!targetClass) {
                    continue;
                }
                List<ResolvedMethodDeclaration> resolvedMethodDeclarations = compilationUnit.findAll(MethodDeclaration.class)
                        .stream()
                        .filter(f -> {
                            Range range = f.getRange().get();
                            int begin = range.begin.line;
                            int end = range.end.line;
                            Iterator<LineDiff> iter = diffSet.iterator();
                            while (iter.hasNext()) {
                                LineDiff line = iter.next();
                                if (line.lineNum >= begin && line.lineNum <= end) {
                                    iter.remove();
                                    return true;
                                }
                            }
                            return false;
                        })
                        .map(m -> m.resolve())
                        .collect(Collectors.toList());
                list.addAll(resolvedMethodDeclarations);
            }
        }
        return list;
    }

    public List<ResolvedFieldDeclaration> tryLocateField(FileDiff diff) throws IOException {
        String packageName = diff.diffSet.get(0).packageName;
        String clazzName = diff.diffSet.get(0).clazzName;
        String qualifyName = packageName + "." + clazzName;
        List<LineDiff> origin = diff.diffSet;
        List<LineDiff> diffSet = new ArrayList<>();
        diffSet.addAll(origin);

        if (callHierarchy.sourceRoots == null) {
            callHierarchy.sourceRoots = callHierarchy.javaParserProxy.getSourceRoots(callHierarchy.projectRoot);
        }

        List<ResolvedFieldDeclaration> list = new ArrayList<>();
        for (SourceRoot sourceRoot : callHierarchy.sourceRoots) {
            if (diffSet.size() <= 0) {
                break;
            }
            List<CompilationUnit> compilationUnits = callHierarchy.compilationUnitMap.get(sourceRoot.getRoot());
            if (compilationUnits == null) {
                compilationUnits = callHierarchy.javaParserProxy.getCompilationUnits(sourceRoot);
                callHierarchy.compilationUnitMap.put(sourceRoot.getRoot(), compilationUnits);
            }
            for (CompilationUnit compilationUnit : compilationUnits) {
                if (diffSet.size() <= 0) {
                    break;
                }
                boolean targetClass = false;
                List<TypeDeclaration> typeDeclarations = compilationUnit.findAll(TypeDeclaration.class);
                for (TypeDeclaration typeDeclaration : typeDeclarations) {
                    Optional<String> optional = typeDeclaration.getFullyQualifiedName();
                    if (optional.isPresent()) {
                        String s = optional.get();
                        if (s.equals(qualifyName)) {
                            targetClass = true;
                            break;
                        }
                    }
                }
                if (!targetClass) {
                    continue;
                }
                List<ResolvedFieldDeclaration> resolvedMethodDeclarations = compilationUnit.findAll(FieldDeclaration.class)
                        .stream()
                        .filter(f -> {
                            Range range = f.getRange().get();
                            int begin = range.begin.line;
                            int end = range.end.line;
                            Iterator<LineDiff> iter = diffSet.iterator();
                            while (iter.hasNext()) {
                                LineDiff line = iter.next();
                                if (line.lineNum >= begin && line.lineNum <= end) {
                                    iter.remove();
                                    return true;
                                }
                            }
                            return false;
                        })
                        .map(m -> m.resolve())
                        .collect(Collectors.toList());
                list.addAll(resolvedMethodDeclarations);
            }
        }
        return list;
    }
}
