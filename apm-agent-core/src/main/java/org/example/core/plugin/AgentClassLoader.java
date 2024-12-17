package org.example.core.plugin;

import sun.misc.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AgentClassLoader extends URLClassLoader {

    public static volatile AgentClassLoader INSTANCE;

    public static final String AGENT_PLUGIN_PATH = "plugins/";

    static {
        INSTANCE = getInstance();
    }

    public static AgentClassLoader getInstance() {
        if (INSTANCE == null) {
            synchronized (AgentClassLoader.class) {
                if (INSTANCE == null) {
                    String agentJarFile = AgentClassLoader.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                    System.out.println("Paths.get(agentJarFile) = " + Paths.get(agentJarFile));
                    File pluginsDir = new File(new File(agentJarFile).getParent() + "/" + AGENT_PLUGIN_PATH);
                    ArrayList<Object> urls = new ArrayList<>();
                    for (File file : Objects.requireNonNull(pluginsDir.listFiles())) {
                        try {
                            urls.add(file.toURI().toURL());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    INSTANCE = new AgentClassLoader(urls.toArray(new URL[0]), ClassLoader.getSystemClassLoader());
                }
            }
        }
        return INSTANCE;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            InputStream resourceAsStream = getResourceAsStream(name);
            byte[] bytes = IOUtils.readAllBytes(resourceAsStream);
            return this.defineClass(name, bytes, 0, bytes.length);
        } catch (Exception e) {
            return super.findClass(name);
        }
    }

    public AgentClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        for (URL url : super.getURLs()) {
            try {
                JarFile jarFile = new JarFile(url.getPath());
                Enumeration<JarEntry> entries = jarFile.entries();
                while (jarFile.entries().hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    if (jarEntry.getName().equals(name)) {
                        return jarFile.getInputStream(jarEntry);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> urls = new ArrayList<>();
        for (URL url : super.getURLs()) {
            try {
                JarFile jarFile = new JarFile(url.getPath());
                Enumeration<JarEntry> entries = jarFile.entries();
                while (jarFile.entries().hasMoreElements()) {
                    JarEntry jarEntry = entries.nextElement();
                    if (jarEntry.getName().equals(name)) {
                        urls.add(url);
                        break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return new Enumeration<URL>() {
            int index = 0;

            @Override
            public boolean hasMoreElements() {
                return index < urls.size();
            }

            @Override
            public URL nextElement() {
                return urls.get(index++);
            }
        };
    }
}
