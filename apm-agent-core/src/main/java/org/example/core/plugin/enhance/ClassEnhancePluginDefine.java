package org.example.core.plugin.enhance;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;
import org.example.core.plugin.AbstractClassEnhancePluginDefine;
import org.example.core.plugin.interceptor.ConstructorInterceptorPoint;
import org.example.core.plugin.interceptor.InstanceMethodInterceptorPoint;
import org.example.core.plugin.interceptor.StaticMethodInterceptorPoint;

import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;

import static net.bytebuddy.matcher.ElementMatchers.isStatic;
import static net.bytebuddy.matcher.ElementMatchers.not;

/**
 * 类增强定义
 * <p>
 * ps：builder、junction、matcher在bytebuddy中都是不可变对象，junction可以链式操作matcher，但不会更改原有的junction，因此必须要重新赋值更改后的对象
 * 方法增强：
 * builder.method(xxx).intercept(xxxx)
 *
 * @author hbj
 */
@Slf4j
public abstract class ClassEnhancePluginDefine extends AbstractClassEnhancePluginDefine {

    /**
     * 静态方法增强
     */
    @Override
    protected DynamicType.Builder<?> enhanceStaticMethod(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
        StaticMethodInterceptorPoint[] staticMethodInterceptorPoints = getStaticMethodInterceptorPoints();
        if (staticMethodInterceptorPoints == null || staticMethodInterceptorPoints.length == 0) {
            return builder;
        }
        for (StaticMethodInterceptorPoint staticMethodInterceptorPoint : getStaticMethodInterceptorPoints()) {
            String staticMethodInterceptor = staticMethodInterceptorPoint.getStaticMethodInterceptor();
            if (staticMethodInterceptor == null || staticMethodInterceptor.isEmpty()) {
                throw new RuntimeException("类[" + typeDescription.getName() + "]的静态方法拦截器为空");
            }
            ElementMatcher.Junction<? super MethodDescription> junction = staticMethodInterceptorPoint.buildStaticMethodJunction();
            builder = builder.method(isStatic().and(junction))
                    .intercept(MethodDelegation.withDefaultConfiguration()
                            .to(new StaticMethodInter(staticMethodInterceptorPoint.getStaticMethodInterceptor(), classLoader)));
        }
        return builder;
    }

    /**
     * 实例方法增强
     */
    @Override
    protected DynamicType.Builder<?> enhanceInstanceMethod(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain, EnhanceContext context) {
        ConstructorInterceptorPoint[] constructorInterceptorPoints = getConstructorInterceptorPoints();
        InstanceMethodInterceptorPoint[] instanceMethodInterceptorPoints = getInstanceMethodInterceptorPoints();

        boolean existsConstructorInterceptorPoint = constructorInterceptorPoints != null && constructorInterceptorPoints.length > 0;
        boolean existInstanceMethodInterceptorPoint = instanceMethodInterceptorPoints != null && instanceMethodInterceptorPoints.length > 0;

        if (!existsConstructorInterceptorPoint && !existInstanceMethodInterceptorPoint) {
            return builder;
        }

        // 判断类是否实现了EnhancedInstance接口
        if (!typeDescription.isAssignableTo(EnhancedInstance.class)) {
            // 判断是否扩展过
            if (!context.isObjectExtended()) {
                // 新增属性,对于同一个类只需要执行一次
                builder = builder.defineField(CONTEXT_ATTR_NAME, Object.class, Modifier.PRIVATE | Modifier.VOLATILE)
                        .implement(EnhancedInstance.class)
                        .intercept(FieldAccessor.ofField(CONTEXT_ATTR_NAME));
                // 设置扩展标志
                context.objectExtendedCompleted();
            }
        }

        // 存在构造方法拦截点
        if (existsConstructorInterceptorPoint) {
            for (ConstructorInterceptorPoint constructorInterceptorPoint : constructorInterceptorPoints) {
                String constructorInterceptor = constructorInterceptorPoint.getConstructorInterceptor();
                if (constructorInterceptor == null || constructorInterceptor.isEmpty()) {
                    throw new RuntimeException("类" + typeDescription.getName() + "的构造方法拦截器为空");
                }
                ElementMatcher.Junction<? super MethodDescription> junction = constructorInterceptorPoint.buildContructorMethodJunction();
                builder = builder.constructor(junction)
                        // 构造方法拦截，必须要先调用父类构造方法
                        .intercept(SuperMethodCall.INSTANCE.andThen(MethodDelegation.withDefaultConfiguration()
                                .to(new ConstructorInter(constructorInterceptor, classLoader))));
            }
        }

        // 存在实例方法拦截点
        if (existInstanceMethodInterceptorPoint) {
            for (InstanceMethodInterceptorPoint instanceMethodInterceptorPoint : instanceMethodInterceptorPoints) {
                String methodInterceptor = instanceMethodInterceptorPoint.getMethodInterceptor();
                if (methodInterceptor == null || methodInterceptor.isEmpty()) {
                    throw new RuntimeException("类" + typeDescription.getName() + "的实例方法拦截器为空");
                }
                ElementMatcher.Junction<? super MethodDescription> junction = instanceMethodInterceptorPoint.buildMethodJunction();
                builder = builder.method(not(isStatic()).and(junction))
                        .intercept(MethodDelegation.withDefaultConfiguration()
                                .to(new InstanceMethodInter(methodInterceptor, classLoader)));
            }
        }

        return builder;
    }
}
