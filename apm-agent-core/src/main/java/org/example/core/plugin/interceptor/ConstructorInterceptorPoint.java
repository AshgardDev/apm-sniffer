package org.example.core.plugin.interceptor;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

/**
 * 构造器拦截点
 */
public interface ConstructorInterceptorPoint {
    ElementMatcher.Junction<? super MethodDescription> buildContructorMethodJunction();
    String getConstructorInterceptor();
}
