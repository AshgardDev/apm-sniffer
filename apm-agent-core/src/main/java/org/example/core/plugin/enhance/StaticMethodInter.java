package org.example.core.plugin.enhance;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

@Slf4j
public class StaticMethodInter {

    private StaticMethodAroundInterceptor interceptor;

    public StaticMethodInter(String staticMethodInterceptor, ClassLoader classLoader) {
    }

    @RuntimeType
    public Object intercept(@Origin Class<?> clazz, @Origin Method method, @AllArguments Object[] allArguments, @SuperCall Callable<?> zuper) throws Throwable {
        log.info("静态类{}.静态方法{}拦截开始", clazz.getName(), method.getName());
        try {
            interceptor.beforeMethod(clazz, method, allArguments);
        } catch (Exception e) {
            log.error("静态类{}.静态方法{}前置拦截执行错误", clazz.getName(), method.getName(), e);
        }
        Object result = null;
        try {
            result = zuper.call();
        } catch (Exception e) {
            try {
                interceptor.handleException(clazz, method, allArguments, e);
            } catch (Exception ex) {
                log.error("静态类{}.静态方法{}异常拦截执行错误", clazz.getName(), method.getName(), ex);
            }
        } finally {
            try {
                result = interceptor.afterMethod(result, clazz, method, allArguments);
            } catch (Exception e) {
                log.error("静态类{}.静态方法{}后置拦截执行错误", clazz.getName(), method.getName(), e);
            }
        }
        log.info("静态类{}.静态方法{}拦截结束", clazz.getName(), method.getName());
        return result;
    }

}
