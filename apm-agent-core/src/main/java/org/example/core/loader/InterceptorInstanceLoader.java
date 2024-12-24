package org.example.core.loader;

/**
 * 拦截器实例加载器
 * ps：这个类非常重要，是应用类加载器和插件类加载器之间的桥梁
 */
public class InterceptorInstanceLoader {

    /**
     * 加载拦截器实例
     * @param interceptorClassName 拦截器类全限名
     * @param targetClassLoader    应用类加载器
     * @return
     */
    public static <T> T load(String interceptorClassName, ClassLoader targetClassLoader) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (targetClassLoader == null) {
            targetClassLoader = InterceptorInstanceLoader.class.getClassLoader();
        }
        // 指定父类加载器是应用类加载器，这样才能共享agent-core的类信息
        AgentClassLoader classLoader = new AgentClassLoader(targetClassLoader);
        Class<?> aClass = Class.forName(interceptorClassName, true, classLoader);
        Object interceptor = aClass.newInstance();
        return (T) interceptor;
    }
}
