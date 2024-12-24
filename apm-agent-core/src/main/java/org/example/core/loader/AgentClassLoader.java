package org.example.core.loader;

import lombok.RequiredArgsConstructor;
import org.example.core.boot.AgentPackagePath;
import org.example.core.plugin.PluginBootstrap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AgentClassLoader extends ClassLoader {

    /**
     * 加载插件定义及其相关类，但排除（插件的拦截器比如 Sqlite3Instrumentation）
     */
    private static AgentClassLoader DEFAULT_LOADER = null;

    private final LinkedList<File> classpath;

    private List<Jar> allJars;

    private final ReentrantLock jarScanLock = new ReentrantLock();

    public AgentClassLoader(ClassLoader parent) {
        super(parent);
        // 获取agent.jar目录
        File agentDir = AgentPackagePath.getPath();
        classpath = new LinkedList<>();
        classpath.add(new File(agentDir, "plugins"));
    }

    public static void initDefaultLoader() {
        if (DEFAULT_LOADER == null) {
            DEFAULT_LOADER = new AgentClassLoader(PluginBootstrap.class.getClassLoader());
        }
    }

    public static AgentClassLoader getDefault(){
        return DEFAULT_LOADER;
    }

    @Override
    public URL getResource(String name) {
        for (Jar jar : allJars) {
            JarEntry jarEntry = jar.jarFile.getJarEntry(name);
            if(jarEntry != null) {
                try {
                    return getEntryURL(jar.jarFile, jarEntry);
                } catch (IOException e) {
                }
            }
        }
        return null;
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        List<URL> urls = new ArrayList<>();
        for (Jar jar : getAllJars()) {
            JarEntry jarEntry = jar.jarFile.getJarEntry(name);
            if(jarEntry != null){
                URL entryURL = getEntryURL(jar.jarFile, jarEntry);
                urls.add(entryURL);
            }
        }
        return Collections.enumeration(urls);
    }

    private static URL getEntryURL(JarFile jarFile, JarEntry jarEntry) throws IOException {
        // 使用 JarURLConnection 获取 URL
        URL jarURL = new URL("jar:file:" + jarFile.getName() + "!/" + jarEntry.getName());
        JarURLConnection jarConnection = (JarURLConnection) jarURL.openConnection();

        // 返回对应的 URL
        return jarConnection.getURL();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        List<Jar> allJars = getAllJars();
        String resourceName = name.replaceAll("\\.", "/") + ".class";
        for (Jar jar : allJars) {
            JarEntry jarEntry = jar.jarFile.getJarEntry(resourceName);
            if (jarEntry == null) {
                continue;
            }
            try (
                    InputStream inputStream = jar.jarFile.getInputStream(jarEntry);
            ) {
                byte[] bytes = readBytes(inputStream);
                return this.defineClass(name, bytes, 0, bytes.length);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new ClassNotFoundException("找不到类");
    }

    public static byte[] readBytes(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        }
        return null;
    }

    private List<Jar> getAllJars() {
        if (allJars == null) {
            jarScanLock.lock();
            try {
                if (allJars == null) {
                    allJars = doGetJars();
                }
            } finally {
                jarScanLock.unlock();
            }
        }
        return allJars;
    }

    private List<Jar> doGetJars() {
        List<Jar> allJars = new LinkedList<>();
        for (File path : classpath) {
            if (path.exists() && path.isDirectory()) {
                String[] jarFileNames = path.list((dir, name) -> {
                    return name.endsWith(".jar");
                });
                if (jarFileNames == null || jarFileNames.length == 0) {
                    continue;
                }
                for (String jarFileName : jarFileNames) {
                    File jarSourceFile = new File(path, jarFileName);
                    JarFile jarFile = null;
                    try {
                        jarFile = new JarFile(jarSourceFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    allJars.add(new Jar(jarFile, jarSourceFile));
                }
            }
        }
        return allJars;
    }

    @RequiredArgsConstructor
    private static class Jar {
        private final JarFile jarFile;
        private final File sourceFile;
    }
}
