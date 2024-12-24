package org.example.core.plugin.enhance;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.*;
import org.example.core.loader.InterceptorInstanceLoader;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * 实例方法拦截器
 */
@Slf4j
public class InstanceMethodInter {

    private final InstanceMethodAroundInterceptor interceptor;

    public InstanceMethodInter(String methodInterceptor, ClassLoader classLoader) {
        try {
            interceptor = InterceptorInstanceLoader.load(methodInterceptor, classLoader);
        } catch (Exception e) {
            throw new RuntimeException("实例方法拦截器" + methodInterceptor + "加载失败", e);
        }
    }

    @RuntimeType
    public Object intercept(@This Object instance, @AllArguments Object[] allArguments
            , @SuperCall Callable<?> zuper, @Origin Method method
    ) {
        String clazzType = instance.getClass().getName();
        log.info("类{}.方法{}拦截开始", instance.getClass().getName(), method.getName());
        EnhancedInstance enhancedInstance = (EnhancedInstance) instance;
        try {
            interceptor.beforeMethod(enhancedInstance, method, allArguments);
        } catch (Exception e) {
            log.error("类{}.方法{}前置拦截执行错误", clazzType, method.getName(), e);
        }
        Object result = null;
        try {
            result = zuper.call();
        } catch (Exception e) {
            try {
                interceptor.handleException(enhancedInstance, method, allArguments, e);
            } catch (Exception ex) {
                log.error("类{}.方法{}异常拦截执行错误", clazzType, method.getName(), ex);
            }
        } finally {
            try {
                result = interceptor.afterMethod(result, enhancedInstance, method, allArguments);
            } catch (Exception e) {
                log.error("类{}.方法{}后置拦截执行错误", clazzType, method.getName(), e);
            }
        }
        log.info("类{}.方法{}拦截结束", clazzType, method.getName());
        return result;
    }
}
