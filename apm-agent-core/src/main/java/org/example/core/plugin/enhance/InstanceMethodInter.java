package org.example.core.plugin.enhance;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

@Slf4j
public class InstanceMethodInter {

    private InstanceMethodAroundInterceptor interceptor;

    public InstanceMethodInter(String methodInterceptor, ClassLoader classLoader) {
    }

    @RuntimeType
    public Object intercept(@This Object instance, @AllArguments Object[] allArguments
        , @SuperCall Callable<?> zuper, @Origin Method method
    ) throws Throwable {
        String clazzType = instance.getClass().getName();
        log.info("类{}.方法{}拦截开始", instance.getClass().getName(), method.getName());
        try {
            interceptor.beforeMethod(instance, method, allArguments);
        } catch (Exception e) {
            log.error("类{}.方法{}前置拦截执行错误", clazzType, method.getName(), e);
        }
        Object result = null;
        try {
            result = zuper.call();
        } catch (Exception e) {
            try {
                interceptor.handleException(instance, method, allArguments, e);
            } catch (Exception ex) {
                log.error("类{}.方法{}异常拦截执行错误", clazzType, method.getName(), ex);
            }
        } finally {
            try {
                result = interceptor.afterMethod(result, instance, method, allArguments);
            } catch (Exception e) {
                log.error("类{}.方法{}后置拦截执行错误", clazzType, method.getName(), e);
            }
        }
        log.info("类{}.方法{}拦截结束", clazzType, method.getName());
        return result;
    }
}
