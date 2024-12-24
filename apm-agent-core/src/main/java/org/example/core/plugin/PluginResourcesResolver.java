package org.example.core.plugin;

import org.example.core.loader.AgentClassLoader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PluginResourcesResolver {

    public List<URL> getResources(){
        List<URL> cfgUrlPaths = new ArrayList<>();
        try {
            Enumeration<URL> resources = AgentClassLoader.getDefault().getResources("skywalking-plugin.def");
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                cfgUrlPaths.add(resource);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return cfgUrlPaths;
    }
}
