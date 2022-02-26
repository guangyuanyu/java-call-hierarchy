package org.joker.java.call.hierarchy;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class Config {

    private String projectPath;
    private String javaHomePath;
    private String mavenHomePath;
    private String mavenSettingsPath;
    private Set<String> dependencyProjectPathSet = new HashSet<>();
    private Set<String> dependencyJarPathSet = new HashSet<>();

    public String getProjectPath() {
        return getAbsolutePath(projectPath);
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getJavaHomePath() {
        return javaHomePath;
    }

    public void setJavaHomePath(String javaHomePath) {
        this.javaHomePath = javaHomePath;
    }

    public String getMavenHomePath() {
        return getAbsolutePath(mavenHomePath);
    }

    public void setMavenHomePath(String mavenHomePath) {
        this.mavenHomePath = mavenHomePath;
    }

    public String getMavenSettingsPath() {
        return getAbsolutePath(mavenSettingsPath);
    }

    public void setMavenSettingsPath(String mavenSettingsPath) {
        this.mavenSettingsPath = mavenSettingsPath;
    }

    public Set<String> getDependencyProjectPathSet() {
        return dependencyProjectPathSet;
    }

    public void addDependencyProjectPath(String dependencyProjectPath) {
        dependencyProjectPathSet.add(dependencyProjectPath);
    }

    public Set<String> getDependencyJarPathSet() {
        return dependencyJarPathSet;
    }

    public void addDependencyJarPath(String dependencyJarPath) {
        dependencyJarPathSet.add(dependencyJarPath);
    }

    private String getAbsolutePath(String s) {
        if (s == null || s.isBlank()) {
            return "";
        }
        return Path.of(s.replace("~", System.getProperty("user.home")))
                .toAbsolutePath()
                .toString();
    }

}
