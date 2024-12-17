package org.example.core.plugin;

import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PluginLoader {

    public static List<AbstractClassEnhancePluginDefine> loadPlugins() {
        List<AbstractClassEnhancePluginDefine> plugins = new ArrayList<>();
        try {
            Enumeration<URL> resources = AgentClassLoader.INSTANCE.getResources("META-INF/agent.properties");
            while (resources.hasMoreElements()) {
                JarFile jarFile = new JarFile(resources.nextElement().getPath());
                InputStream inputStream = jarFile.getInputStream(jarFile.getEntry("META-INF/agent.properties"));
                String plugin = new String(IOUtils.readAllBytes(inputStream));
                Class<?> clazz = AgentClassLoader.INSTANCE.findClass(plugin);
                Object o = clazz.newInstance();
                plugins.add((AbstractClassEnhancePluginDefine) o);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return plugins;
    }
}
