package org.example.core.plugin.enhance;

public interface ConstructorAroundInterceptor {

    /**
     * 在构造方法后拦截
     * @param instance
     * @param allArguments
     */
    void onConstructorInterceptor(Object instance, Object[] allArguments);
}
