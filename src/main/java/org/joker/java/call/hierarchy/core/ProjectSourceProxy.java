package org.joker.java.call.hierarchy.core;

import java.io.IOException;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.resolution.SymbolResolver;
import com.github.javaparser.utils.SourceRoot;

public class ProjectSourceProxy {

    private SourceRoot sourceRoot;

    public ProjectSourceProxy(SourceRoot sourceRoot) {
        this.sourceRoot = sourceRoot;
    }

    public SymbolResolver getSymbolResolver() {
        return sourceRoot.getParserConfiguration().getSymbolResolver().orElseThrow();
    }

    public List<CompilationUnit> getCompilationUnits() throws IOException {
        if (sourceRoot.getCache().isEmpty()) {
            sourceRoot.tryToParse();
        }
        return sourceRoot.getCompilationUnits();
    }

}
