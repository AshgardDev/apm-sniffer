package org.example.core.plugin;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;
import org.example.core.match.ClassMatch;
import org.example.core.plugin.interceptor.ConstructorInterceptorPoint;
import org.example.core.plugin.interceptor.InstanceMethodInterceptorPoint;
import org.example.core.plugin.interceptor.StaticMethodInterceptorPoint;

import java.security.ProtectionDomain;

/**
 * 所有插件的顶级父类
 */
@Slf4j
public abstract class AbstractClassEnhancePluginDefine {

    public abstract ClassMatch enhanceClass();

    public abstract InstanceMethodInterceptorPoint[] getInstanceMethodInterceptorPoints();

    public abstract ConstructorInterceptorPoint[] getConstructorInterceptorPoints();

    public abstract StaticMethodInterceptorPoint[] getStaticMethodInterceptorPoints();

    /**
     * 定义增强类的主入口
     *
     * @param builder
     * @param typeDescription
     * @param classLoader
     * @param module
     * @param protectionDomain
     * @return
     */
    public DynamicType.Builder<?> define(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
        DynamicType.Builder<?> newBuilder = builder;
        // 静态方法增强
        DynamicType.Builder<?> staticBuilder = this.enhanceStaticMethod(newBuilder, typeDescription, classLoader, module, protectionDomain);
        if (staticBuilder != null) {
            newBuilder = staticBuilder;
        }
        // 实例方法包括构造方法和普通方法
        DynamicType.Builder<?> instanceBuilder = this.enhanceInstanceMethod(newBuilder, typeDescription, classLoader, module, protectionDomain);
        if (instanceBuilder != null) {
            newBuilder = instanceBuilder;
        }
        return newBuilder;
    }

    protected abstract DynamicType.Builder<?> enhanceInstanceMethod(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain);

    protected abstract DynamicType.Builder<?> enhanceStaticMethod(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain);
}
