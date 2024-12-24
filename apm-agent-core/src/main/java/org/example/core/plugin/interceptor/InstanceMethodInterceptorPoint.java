package org.example.core.plugin.interceptor;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * 实例方法拦截点
 */
public interface InstanceMethodInterceptorPoint {
    ElementMatcher.Junction<? super MethodDescription> buildMethodJunction();
    String getMethodInterceptor();
}
