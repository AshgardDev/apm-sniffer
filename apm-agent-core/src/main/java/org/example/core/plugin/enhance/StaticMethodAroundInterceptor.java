package org.example.core.plugin.enhance;

import java.lang.reflect.Method;

public interface StaticMethodAroundInterceptor {

    void beforeMethod(Class<?> clazz, Method method, Object[] allArguments);

    Object afterMethod(Object result, Class<?> clazz, Method method, Object[] allArguments);

    void handleException(Class<?> clazz, Method method, Object[] allArguments, Exception e);
}
