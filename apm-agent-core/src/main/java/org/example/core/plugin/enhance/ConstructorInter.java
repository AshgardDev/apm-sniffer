package org.example.core.plugin.enhance;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;

@Slf4j
public class ConstructorInter {

    private ConstructorAroundInterceptor interceptor;

    public ConstructorInter(String constructorInterceptor, ClassLoader classLoader) {
    }

    @RuntimeType
    public void intercept(@This Object instance, @AllArguments Object[] allArguments) throws Throwable {
        try {
            interceptor.onConstructorInterceptor(instance, allArguments);
        } catch (Exception e) {
            log.error("构造后置拦截异常", e);
        }
    }

}
