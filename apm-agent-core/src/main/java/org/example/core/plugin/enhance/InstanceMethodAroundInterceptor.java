package org.example.core.plugin.enhance;

import java.lang.reflect.Method;

public interface InstanceMethodAroundInterceptor {

    void beforeMethod(EnhancedInstance instance, Method method, Object[] allArguments);

    void handleException(EnhancedInstance instance, Method method, Object[] allArguments, Exception e);

    Object afterMethod(Object result, EnhancedInstance instance, Method method, Object[] allArguments);
}
