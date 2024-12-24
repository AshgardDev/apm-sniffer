package org.example.core.boot;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;

@Slf4j
public class AgentPackagePath {

    private static File AGENT_PACKAGE_PATH;

    public static File getPath() {
        if(AGENT_PACKAGE_PATH == null) {
            AGENT_PACKAGE_PATH = findPath();
        }
        return AGENT_PACKAGE_PATH;
    }

    private static File findPath() {
        String classResourcePath = AgentPackagePath.class.getName().replaceAll("\\.", "/") + ".class";
        /**
         * resource有2种
         * 1 file://xxx --在idea中
         * 2 jar:file:/xxx --构建好的
         */
        URL resource = ClassLoader.getSystemClassLoader().getResource(classResourcePath);
        if(resource != null) {
            String urlString = resource.toString();
            boolean isInJar = urlString.contains("!");
            if (isInJar) {
                urlString = urlString.substring(urlString.indexOf("file:")+5, urlString.indexOf("!"));
                File agentJarFile = null;
                try {
                    agentJarFile = new File(urlString);
                } catch (Exception e) {
                    log.error("找不到agent.jar目录, {}", urlString);
                }
                if(agentJarFile.exists()) {
                    return agentJarFile.getParentFile();
                }
            }
        }

        throw new RuntimeException("找不到agent.jar");
    }
}
