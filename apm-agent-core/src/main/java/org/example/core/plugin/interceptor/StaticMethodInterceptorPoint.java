package org.example.core.plugin.interceptor;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * 静态方法拦截点
 */
public interface StaticMethodInterceptorPoint {
    ElementMatcher.Junction<? super MethodDescription> buildStaticMethodJunction();
    String getStaticMethodInterceptor();
}
