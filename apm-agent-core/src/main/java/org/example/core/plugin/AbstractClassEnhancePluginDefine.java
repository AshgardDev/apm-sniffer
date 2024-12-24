package org.example.core.plugin;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;
import org.example.core.plugin.match.ClassMatch;
import org.example.core.plugin.enhance.EnhanceContext;
import org.example.core.plugin.interceptor.ConstructorInterceptorPoint;
import org.example.core.plugin.interceptor.InstanceMethodInterceptorPoint;
import org.example.core.plugin.interceptor.StaticMethodInterceptorPoint;

import java.security.ProtectionDomain;

/**
 * 所有插件的顶级父类
 */
@Slf4j
public abstract class AbstractClassEnhancePluginDefine {

    /**
     * 新增属性名
     */
    public static final String CONTEXT_ATTR_NAME = "_$EnhancedClassField_ws";

    /**
     * 增强类
     * @return
     */
    public abstract ClassMatch enhanceClass();

    /**
     * 实例方法拦截点列表，一个类可以有多个实例方法被拦截
     * @return
     */
    public abstract InstanceMethodInterceptorPoint[] getInstanceMethodInterceptorPoints();

    /**
     * 构造方法拦截点列表，一个类可以有多个构造方法被拦截
     * @return
     */
    public abstract ConstructorInterceptorPoint[] getConstructorInterceptorPoints();

    /**
     * 静态方法拦截点列表，一个类可以有多个静态方法被拦截
     * @return
     */
    public abstract StaticMethodInterceptorPoint[] getStaticMethodInterceptorPoints();

    /**
     * 定义增强类的主入口
     */
    public DynamicType.Builder<?> define(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain, EnhanceContext context) {
        String pluginDefineClassName = this.getClass().getName();
        String type = typeDescription.getActualName();
        log.info("开始使用插件{}来增强类{}", pluginDefineClassName, type);
        DynamicType.Builder<?> newBuilder = this.enhance(builder, typeDescription, classLoader, module, protectionDomain, context);
        context.initializationStageCompleted();
        log.info("结束使用插件{}来增强类{}", pluginDefineClassName, type);
        return newBuilder;
    }

    /**
     * 增强逻辑定义
     */
    public DynamicType.Builder<?> enhance(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain, EnhanceContext context) {
        DynamicType.Builder<?> newBuilder = builder;
        // 静态方法增强
        DynamicType.Builder<?> staticBuilder = this.enhanceStaticMethod(newBuilder, typeDescription, classLoader, module, protectionDomain);
        if (staticBuilder != null) {
            newBuilder = staticBuilder;
        }
        // 实例方法包括构造方法和普通方法
        DynamicType.Builder<?> instanceBuilder = this.enhanceInstanceMethod(newBuilder, typeDescription, classLoader, module, protectionDomain, context);
        if (instanceBuilder != null) {
            newBuilder = instanceBuilder;
        }
        return newBuilder;
    }

    protected abstract DynamicType.Builder<?> enhanceStaticMethod(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain);

    protected abstract DynamicType.Builder<?> enhanceInstanceMethod(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain, EnhanceContext context);
}
