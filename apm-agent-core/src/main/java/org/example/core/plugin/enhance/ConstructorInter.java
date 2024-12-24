package org.example.core.plugin.enhance;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import org.example.core.loader.InterceptorInstanceLoader;

/**
 * 构造器拦截器
 */
@Slf4j
public class ConstructorInter {

    private final ConstructorAroundInterceptor interceptor;

    public ConstructorInter(String constructorInterceptor, ClassLoader classLoader) {
        try {
            // 加载拦截器
            interceptor = InterceptorInstanceLoader.load(constructorInterceptor, classLoader);
        }catch (Exception e) {
            throw new RuntimeException("构造器拦截器" + constructorInterceptor + "加载失败", e);
        }
    }

    /**
     * 拦截器逻辑
     */
    @RuntimeType
    public void intercept(@This Object instance, @AllArguments Object[] allArguments) {
        try {
            interceptor.onConstructorInterceptor((EnhancedInstance)instance, allArguments);
        } catch (Exception e) {
            log.error("构造器拦截器执行异常", e);
        }
    }
}
