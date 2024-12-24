package org.example.core.boot;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;

/**
 * agent jar包路径定位
 */
@Slf4j
public class AgentPackagePath {

    private static File AGENT_PACKAGE_PATH;

    public static File getPath() {
        if (AGENT_PACKAGE_PATH == null) {
            AGENT_PACKAGE_PATH = findPath();
        }
        return AGENT_PACKAGE_PATH;
    }

    /**
     * 从类加载器中定位到资源位置：
     * 资源定位有2种
     * 1  file://xxx -- 在idea中，会默认匹配到模块，这种情况这里不考虑（若考虑开发方便，可以添加对应逻辑）
     * 2  jar:file:/xxx -- 构建后的jar包中
     */
    private static File findPath() {
        String classResourcePath = AgentPackagePath.class.getName().replaceAll("\\.", "/") + ".class";
        try {
            URL resource = ClassLoader.getSystemClassLoader().getResource(classResourcePath);
            if (resource != null) {
                String urlString = resource.toString();
                boolean isInJar = urlString.contains("!");
                if (isInJar) {
                    // 剔除jar:file和!后面的资源路径, 只提取jar包的路径
                    urlString = urlString.substring(urlString.indexOf("file:") + 5, urlString.indexOf("!"));
                    File agentJarFile = new File(urlString);
                    if (agentJarFile.exists()) {
                        return agentJarFile.getParentFile();
                    }
                }
            }
        } catch (Exception e) {
            log.error("定位agent.jar位置失败", e);
        }
        throw new RuntimeException("定位不到Agent.jar位置");
    }
}
