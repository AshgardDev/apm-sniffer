package org.example.core.plugin.interceptor;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

public interface StaticMethodInterceptorPoint {

    ElementMatcher.Junction<? super MethodDescription> buildStaticMethodJunction();

    String getStaticMethodInterceptor();
}
