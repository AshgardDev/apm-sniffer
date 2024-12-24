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

/**
 * Agent 插件的类加载器
 * ps：这个类加载器只加载plugins目录下的jar，agent-core这个jar包其实是应用类加载器加载的！
 *
 * @author hbj
 */
public class AgentClassLoader extends ClassLoader {

    /**
     * 加载插件定义及其相关类，但排除（插件的拦截器比如 Sqlite3Instrumentation）
     */
    private static AgentClassLoader DEFAULT_LOADER = null;

    /**
     * 扫描的类路径
     */
    private final LinkedList<File> classpath;

    /**
     * 加载的所有jar
     */
    private List<Jar> allJars;

    private final ReentrantLock jarScanLock = new ReentrantLock();

    public AgentClassLoader(ClassLoader parent) {
        super(parent);
        // 获取agent.jar目录
        File agentDir = AgentPackagePath.getPath();
        classpath = new LinkedList<>();
        classpath.add(new File(agentDir, "plugins"));
    }

    /**
     * 初始化
     */
    public static void initDefaultLoader() {
        if (DEFAULT_LOADER == null) {
            DEFAULT_LOADER = new AgentClassLoader(PluginBootstrap.class.getClassLoader());
        }
    }

    /**
     * 获取默认的类加载器--插件加载器
     *
     * @return
     */
    public static AgentClassLoader getDefault() {
        return DEFAULT_LOADER;
    }

    /**
     * 获取资源
     * ps：一般都会重写该方法，用于获取jar包中的资源
     *
     * @param name The resource name
     * @return
     */
    @Override
    public URL getResource(String name) {
        for (Jar jar : getAllJars()) {
            JarEntry jarEntry = jar.jarFile.getJarEntry(name);
            if (jarEntry != null) {
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
            if (jarEntry != null) {
                URL entryURL = getEntryURL(jar.jarFile, jarEntry);
                urls.add(entryURL);
            }
        }
        return Collections.enumeration(urls);
    }

    /**
     * 将jar中jarEntry的资源转成URL
     *
     * @param jarFile
     * @param jarEntry
     * @return
     * @throws IOException
     */
    private static URL getEntryURL(JarFile jarFile, JarEntry jarEntry) throws IOException {
        // 使用 JarURLConnection 获取 URL
        URL jarURL = new URL("jar:file:" + jarFile.getName() + "!/" + jarEntry.getName());
        JarURLConnection jarConnection = (JarURLConnection) jarURL.openConnection();
        // 返回对应的 URL
        return jarConnection.getURL();
    }

    /**
     * 重写类查找方法，查找指定plugin目录下的类
     *
     * @param name The <a href="#name">binary name</a> of the class
     * @return
     * @throws ClassNotFoundException
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        List<Jar> allJars = getAllJars();
        String resourceName = name.replaceAll("\\.", "/").concat(".class");
        for (Jar jar : allJars) {
            JarEntry jarEntry = jar.jarFile.getJarEntry(resourceName);
            if (jarEntry == null) {
                continue;
            }
            try (
                    InputStream inputStream = jar.jarFile.getInputStream(jarEntry);
            ) {
                byte[] bytes = readBytes(inputStream);
                if (bytes != null) {
                    return this.defineClass(name, bytes, 0, bytes.length);
                }
            } catch (Exception e) {
                throw new RuntimeException("匹配到[" + name + "]资源，但类加载失败", e);
            }
        }
        throw new ClassNotFoundException("找不到类[" + name + "]");
    }

    /**
     * 读取输入流，转成字节数组
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
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

    /**
     * 获取所有jar
     *
     * @return
     */
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

    /**
     * 扫描plugins下的所有jar
     *
     * @return
     */
    private List<Jar> doGetJars() {
        List<Jar> allJars = new LinkedList<>();
        for (File path : classpath) {
            if (path.exists() && path.isDirectory()) {
                String[] jarFileNames = path.list((dir, name) -> name.endsWith(".jar"));
                if (jarFileNames == null || jarFileNames.length == 0) {
                    continue;
                }
                for (String jarFileName : jarFileNames) {
                    File jarSourceFile = new File(path, jarFileName);
                    try {
                        allJars.add(new Jar(new JarFile(jarSourceFile), jarSourceFile));
                    } catch (Exception e) {
                        throw new RuntimeException("plugin包下的[" + jarFileName + "].jar加载失败", e);
                    }
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
