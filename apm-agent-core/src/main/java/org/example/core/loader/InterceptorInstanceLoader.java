package org.example.core.loader;

public class InterceptorInstanceLoader {
    public static <T> T load(String interceptorClassName, ClassLoader targetClassLoader) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
            if(targetClassLoader == null) {
                targetClassLoader = InterceptorInstanceLoader.class.getClassLoader();
            }
            AgentClassLoader classLoader = new AgentClassLoader(targetClassLoader);
            Class<?> aClass = Class.forName(interceptorClassName, true, classLoader);
            Object o = aClass.newInstance();
            return (T) o;
    }
}
