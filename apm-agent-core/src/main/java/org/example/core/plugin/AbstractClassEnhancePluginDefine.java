package org.example.core.plugin;

/**
 * 所有插件的顶级父类
 */
public abstract class AbstractClassEnhancePluginDefine {

    public abstract ClassMatcher enhanceClass() throws PluginException;

    public abstract InstanceMethodsInterceptorPoint[] getInstanceMethodsInterceptorPoints() throws PluginException;

    public abstract ConstructorInterceptorPoint[] getConstructorInterceptorPoints() throws PluginException;

    public abstract String[] getStaticMethodsInterceptorPoints() throws PluginException;
}
