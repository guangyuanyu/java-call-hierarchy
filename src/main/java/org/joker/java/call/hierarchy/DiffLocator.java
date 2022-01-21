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
import org.joker.java.call.hierarchy.diff.FileDiff;
import org.joker.java.call.hierarchy.diff.LineDiff;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class DiffLocator {

    public static class MethodDesc {
        public String packageName;

        public String className;

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

    public static class FieldDesc {
        public String packageName;

        public String className;

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

    public static class DiffDesc {
        public String module;

        public boolean isFieldDiff;

        public FieldDesc fieldDesc;

        public MethodDesc methodDesc;

        @Override
        public String toString() {
            return "{" +
                    "\"module\":  " + module +
                    "\"isFieldDiff\":  " + isFieldDiff +
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
        while (iter.hasNext()){
            FileDiff diff = iter.next();
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

        iter = diffs.iterator();
        while (iter.hasNext()) {
            FileDiff diff = iter.next();
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
