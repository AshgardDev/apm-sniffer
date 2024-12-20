package org.example.core.plugin.enhance;

import java.lang.reflect.Method;

public interface InstanceMethodAroundInterceptor {

    void beforeMethod(Object instance, Method method, Object[] allArguments);

    void handleException(Object instance, Method method, Object[] allArguments, Exception e);

    Object afterMethod(Object result, Object instance, Method method, Object[] allArguments);
}
