package org.example.core.loader;

import lombok.RequiredArgsConstructor;
import org.example.core.boot.AgentPackagePath;
import org.example.core.plugin.PluginBootstrap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
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

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        List<Jar> allJars = getAllJars();
        String resourceName = name.replace("\\.", "/") + ".class";
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
