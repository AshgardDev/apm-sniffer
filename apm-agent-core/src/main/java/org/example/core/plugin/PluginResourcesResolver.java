package org.example.core.plugin;

import org.example.core.loader.AgentClassLoader;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class PluginResourcesResolver {

    public static List<URL> getResources(){
        try {
            Enumeration<URL> resources = AgentClassLoader.getDefault().getResources(PluginBootstrap.PLUGIN_DEFINE_FILE);
            return Collections.list(resources);
        } catch (IOException e) {
            throw new RuntimeException("解析Plugin的def资源失败", e);
        }
    }
}
