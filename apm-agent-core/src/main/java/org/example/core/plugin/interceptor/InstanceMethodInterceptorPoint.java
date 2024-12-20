package org.example.core.plugin.interceptor;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

public interface InstanceMethodInterceptorPoint {

    ElementMatcher.Junction<? super MethodDescription> buildMethodJunction();

    String getMethodInterceptor();

}
