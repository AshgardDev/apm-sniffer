package org.example.core.plugin;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

public interface InstanceMethodsInterceptorPoint {

    ElementMatcher<? super MethodDescription> getMethodsMatcher();

    String getMethodInterceptor();
}
