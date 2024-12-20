package org.example.core.plugin.enhance;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;
import org.example.core.plugin.AbstractClassEnhancePluginDefine;
import org.example.core.plugin.interceptor.ConstructorInterceptorPoint;
import org.example.core.plugin.interceptor.InstanceMethodInterceptorPoint;
import org.example.core.plugin.interceptor.StaticMethodInterceptorPoint;

import java.security.ProtectionDomain;

import static net.bytebuddy.matcher.ElementMatchers.isStatic;
import static net.bytebuddy.matcher.ElementMatchers.not;

@Slf4j
public abstract class ClassEnhancePluginDefine extends AbstractClassEnhancePluginDefine {

    @Override
    protected DynamicType.Builder<?> enhanceStaticMethod(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
        StaticMethodInterceptorPoint[] staticMethodInterceptorPoints = getStaticMethodInterceptorPoints();
        if (staticMethodInterceptorPoints == null || staticMethodInterceptorPoints.length == 0) {
            return builder;
        }
        for (StaticMethodInterceptorPoint staticMethodInterceptorPoint : getStaticMethodInterceptorPoints()) {
            String staticMethodInterceptor = staticMethodInterceptorPoint.getStaticMethodInterceptor();
            if (staticMethodInterceptor == null || staticMethodInterceptor.isEmpty()) {
                log.warn("静态类{}的静态方法拦截器为空", typeDescription.getTypeName());
                continue;
            }
            ElementMatcher.Junction<? super MethodDescription> junction = staticMethodInterceptorPoint.buildStaticMethodJunction();
            builder = builder.method(isStatic().and(junction)).intercept(MethodDelegation.withDefaultConfiguration().to(new StaticMethodInter(staticMethodInterceptorPoint.getStaticMethodInterceptor(), classLoader)));
        }
        return builder;
    }

    @Override
    protected DynamicType.Builder<?> enhanceInstanceMethod(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
        ConstructorInterceptorPoint[] constructorInterceptorPoints = getConstructorInterceptorPoints();
        InstanceMethodInterceptorPoint[] instanceMethodInterceptorPoints = getInstanceMethodInterceptorPoints();

        boolean existsConstructorInterceptorPoint = constructorInterceptorPoints != null && constructorInterceptorPoints.length > 0;
        boolean existInstanceMethodInterceptorPoint = instanceMethodInterceptorPoints != null && instanceMethodInterceptorPoints.length > 0;

        if (!existsConstructorInterceptorPoint && !existInstanceMethodInterceptorPoint) {
            return builder;
        }

        if (existsConstructorInterceptorPoint) {
            for (ConstructorInterceptorPoint constructorInterceptorPoint : constructorInterceptorPoints) {
                String constructorInterceptor = constructorInterceptorPoint.getConstructorInterceptor();
                if (constructorInterceptor == null || constructorInterceptor.isEmpty()) {
                    log.warn("类{}的构造方法拦截器为空", typeDescription.getTypeName());
                    continue;
                }
                ElementMatcher.Junction<? super MethodDescription> junction = constructorInterceptorPoint.buildContructorMethodJunction();
                builder = builder.constructor(junction).intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration().to(new ConstructorInter(constructorInterceptor, classLoader))));
            }
        }

        if (existInstanceMethodInterceptorPoint) {
            for (InstanceMethodInterceptorPoint instanceMethodInterceptorPoint : instanceMethodInterceptorPoints) {
                String methodInterceptor = instanceMethodInterceptorPoint.getMethodInterceptor();
                if (methodInterceptor == null || methodInterceptor.isEmpty()) {
                    log.warn("类{}的实例方法拦截器为空", typeDescription.getTypeName());
                    continue;
                }
                ElementMatcher.Junction<? super MethodDescription> junction = instanceMethodInterceptorPoint.buildMethodJunction();
                builder = builder.method(not(isStatic()).and(junction)).intercept(MethodDelegation.withDefaultConfiguration().to(new InstanceMethodInter(methodInterceptor, classLoader)));
            }
        }

        return builder;
    }
}
