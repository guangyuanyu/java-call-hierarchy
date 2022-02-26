package org.joker.java.call.hierarchy.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;
import com.github.javaparser.utils.SourceRoot;

import org.joker.java.call.hierarchy.Config;

public class JavaParserProxy {

    public ProjectRoot getProjectRoot(Path root) {
        return new SymbolSolverCollectionStrategy().collect(root);
    }

    public List<SourceRoot> getSourceRoots(ProjectRoot projectRoot) {
        return projectRoot.getSourceRoots()
                .stream()
                .filter(f -> f.getRoot().endsWith("main/java"))
                .toList();
    }

    public SourceRoot getSourceRoot(Path root) {
        return new SourceRoot(root);
    }

    public List<CompilationUnit> getCompilationUnits(SourceRoot sourceRoot, Config config) throws IOException {
        if (sourceRoot.getParserConfiguration().getSymbolResolver().isEmpty()) {
            // if maven project
            if (sourceRoot.getRoot().endsWith("src/main/java")) {
                Path projectPath = sourceRoot.getRoot().getParent().getParent().getParent();
                boolean anyMatch = Arrays.stream(projectPath.toFile().listFiles()).map(File::getName)
                        .anyMatch("pom.xml"::equals);
                if (anyMatch) {
                    ParserConfiguration parserConfiguration = JavaParserConfiguration.getParserConfiguration(config);
                    sourceRoot.setParserConfiguration(parserConfiguration);
                    System.out.println(
                            String.format(">>> parser pom.xml: %s%spom.xml", sourceRoot.getRoot(), File.separator));
                }
            }
        }

        sourceRoot.tryToParse();
        return sourceRoot.getCompilationUnits();
    }

}
