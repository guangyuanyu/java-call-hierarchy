package org.joker.java.call.hierarchy.core;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.model.resolution.TypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class JavaParserConfiguration {

    public static ParserConfiguration getParserConfiguration(String projectPath) throws IOException {
        return getParserConfiguration(projectPath, null, null);
    }

    public static ParserConfiguration getParserConfiguration(String projectPath, Set<String> dependencySourcePathSet,
            Set<String> dependencyJarPathSet) throws IOException {
        if (dependencyJarPathSet == null) {
            dependencyJarPathSet = new HashSet<>();
        }
        Set<String> mavenDependencyJarPath = getMavenDependencyJarPath(projectPath);
        dependencyJarPathSet.addAll(mavenDependencyJarPath);

        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setAttributeComments(false);
        parserConfiguration
                .setSymbolResolver(new JavaSymbolSolver(getTypeSolver(dependencySourcePathSet, dependencyJarPathSet)));
        return parserConfiguration;
    }

    public static Set<String> getMavenDependencyJarPath(String projectPath) {
        Set<String> dependencyJarPathSet = new HashSet<>();

        boolean anyMatch = Arrays.stream(Path.of(projectPath).toFile().listFiles()).map(File::getName)
                .anyMatch("pom.xml"::equals);
        if (!anyMatch) {
            return dependencyJarPathSet;
        }

        InvocationRequest request = new DefaultInvocationRequest();
        request.setJavaHome(new File(System.getProperty("java.home")));
        if (!ConfigProperties.MAVEN_SETTING.isEmpty()) {
            request.setUserSettingsFile(new File(ConfigProperties.MAVEN_SETTING));
        }
        request.setPomFile(new File(projectPath + "/pom.xml"));
        request.setGoals(Collections.singletonList("dependency:build-classpath"));

        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(ConfigProperties.MAVEN_HOME));
        invoker.setInputStream(InputStream.nullInputStream());
        invoker.setOutputHandler(dependencyJarPathSet::add);
        try {
            invoker.execute(request);
        } catch (MavenInvocationException e) {
            e.printStackTrace();
        }

        return dependencyJarPathSet.stream()
                .filter(f -> !f.startsWith("["))
                .map(m -> System.getProperty("os.name").startsWith("Win") ? m.split(";") : m.split(":"))
                .flatMap(Arrays::stream)
                .collect(Collectors.toSet());
    }

    public static TypeSolver getTypeSolver(Set<String> dependencySourcePathSet, Set<String> dependencyJarPathSet)
            throws IOException {
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();

        combinedTypeSolver.add(new ReflectionTypeSolver(false));

        if (dependencySourcePathSet != null) {
            for (String dependencySourcePath : dependencySourcePathSet) {
                JavaParserTypeSolver javaParserTypeSolver = new JavaParserTypeSolver(Path.of(dependencySourcePath));
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

}
