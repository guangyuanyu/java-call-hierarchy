package org.joker.java.call.hierarchy.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigProperties {

    public static final String MAVEN_HOME;
    public static final String MAVEN_SETTING;

    static {
//        InputStream inputStream = ConfigProperties.class.getClassLoader().getResourceAsStream("config.properties");
        Properties properties = new Properties();
//        try {
//            properties.load(inputStream);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        properties.put("maven.home", "/usr/local/Cellar/maven@3.2/3.2.5_1/libexec");
        properties.put("maven.setting", "");

        MAVEN_HOME = getAbsolutePath(properties.getProperty("maven.home"));
        MAVEN_SETTING = getAbsolutePath(properties.getProperty("maven.setting"));
    }

    private static String getAbsolutePath(String s) {
        if (s.isEmpty()) {
            return "";
        }
        return Paths.get(s.replace("~", DefaultProperties.USER_HOME.getEnv()))
                .toAbsolutePath()
                .toString();
    }

}
