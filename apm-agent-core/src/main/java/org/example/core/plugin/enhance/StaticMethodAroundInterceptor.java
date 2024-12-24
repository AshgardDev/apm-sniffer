package org.example.core.plugin.enhance;

import java.lang.reflect.Method;

/**
 * 静态方法拦截器接口
 *
 * @author hbj
 */
public interface StaticMethodAroundInterceptor {

    void beforeMethod(Class<?> clazz, Method method, Object[] allArguments);

    Object afterMethod(Object result, Class<?> clazz, Method method, Object[] allArguments);

    void handleException(Class<?> clazz, Method method, Object[] allArguments, Exception e);
}
