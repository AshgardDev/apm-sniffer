package org.example.core.plugin.enhance;

/**
 * 构造方法拦截器
 */
public interface ConstructorAroundInterceptor {

    /**
     * 在构造方法后拦截
     * @param instance
     * @param allArguments
     */
    void onConstructorInterceptor(EnhancedInstance instance, Object[] allArguments);
}
