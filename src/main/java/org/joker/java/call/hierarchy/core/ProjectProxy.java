package org.joker.java.call.hierarchy.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.utils.SymbolSolverCollectionStrategy;
import com.github.javaparser.utils.CollectionStrategy;
import com.github.javaparser.utils.ProjectRoot;

import org.joker.java.call.hierarchy.Config;

public class ProjectProxy {

    private ProjectRoot projectRoot;
    private List<ProjectSourceProxy> sourceProxies;

    public ProjectProxy(Config config) throws IOException {
        ParserConfiguration parserConfiguration = JavaParserConfiguration.getParserConfiguration(config);
        CollectionStrategy collectionStrategy = new SymbolSolverCollectionStrategy(parserConfiguration);
        projectRoot = collectionStrategy.collect(Path.of(config.getProjectPath()));
    }

    public List<ProjectSourceProxy> getSourceProxies() {
        if (sourceProxies == null) {
            sourceProxies = projectRoot.getSourceRoots()
                    .stream()
                    .filter(f -> f.getRoot().endsWith("main/java"))
                    .map(ProjectSourceProxy::new)
                    .toList();
        }
        return sourceProxies;
    }

}
