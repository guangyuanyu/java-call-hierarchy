package org.joker.java.call.hierarchy.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Properties;

public class ConfigProperties {

    public static final String MAVEN_HOME;
    public static final String MAVEN_SETTING;

    static {
        InputStream inputStream = ConfigProperties.class.getClassLoader().getResourceAsStream("config.properties");
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MAVEN_HOME = getAbsolutePath(properties.getProperty("maven.home"));
        MAVEN_SETTING = getAbsolutePath(properties.getProperty("maven.setting"));
    }

    private static String getAbsolutePath(String s) {
        if (s.isBlank()) {
            return "";
        }
        return Path.of(s.replace("~", System.getProperty("user.home")))
                .toAbsolutePath()
                .toString();
    }

}
