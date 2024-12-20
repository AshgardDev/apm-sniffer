package org.example.core.plugin.interceptor;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

public interface ConstructorInterceptorPoint {
    ElementMatcher.Junction<? super MethodDescription> buildContructorMethodJunction();
    String getConstructorInterceptor();
}
