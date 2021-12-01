package org.joker.java.call.hierarchy.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigProperties {

    public static final String JAVA_HOME;
    public static final String MAVEN_HOME;

    static {
        InputStream inputStream = ConfigProperties.class.getClassLoader().getResourceAsStream("config.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JAVA_HOME = properties.getProperty("java.home").isBlank() ? System.getProperty("java.home") : properties.getProperty("java.home");
        MAVEN_HOME = properties.getProperty("maven.home");
    }

}
