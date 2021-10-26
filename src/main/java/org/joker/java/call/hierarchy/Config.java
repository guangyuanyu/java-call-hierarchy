package org.joker.java.call.hierarchy;

import java.util.HashSet;
import java.util.Set;

public class Config {

    String projectPath;
    Set<String> dependencyProjectPathSet = new HashSet<>();
    Set<String> dependencyJarPathSet = new HashSet<>();

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
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

}
