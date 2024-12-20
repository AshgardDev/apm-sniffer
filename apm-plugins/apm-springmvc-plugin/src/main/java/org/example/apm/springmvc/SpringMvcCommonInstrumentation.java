package org.example.apm.springmvc;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.example.core.plugin.AbstractClassEnhancePluginDefine;
import org.example.core.plugin.interceptor.ConstructorInterceptorPoint;
import org.example.core.plugin.interceptor.InstanceMethodInterceptorPoint;
import org.example.core.plugin.interceptor.StaticMethodInterceptorPoint;

import static net.bytebuddy.matcher.ElementMatchers.*;

public abstract class SpringMvcCommonInstrumentation extends AbstractClassEnhancePluginDefine {

    private static final String MAPPING_PKG_PREFIX = "org.springframework.web.bind.annotation";
    private static final String MAPPING = "Mapping";
    private static final String INTERCEPTOR_CLASS_NAME = "org.example.apm.springmvc.SpringMvcInterceptor";

    @Override
    public InstanceMethodInterceptorPoint[] getInstanceMethodInterceptorPoints() {
        return new InstanceMethodInterceptorPoint[]{
                new InstanceMethodInterceptorPoint() {
                    @Override
                    public ElementMatcher<? super MethodDescription> getMethodMatcher() {
                        return not(isStatic()).and(isAnnotatedWith(nameStartsWith(MAPPING_PKG_PREFIX).and(nameEndsWith(MAPPING))));
                    }

                    @Override
                    public String getMethodInterceptor() {
                        return INTERCEPTOR_CLASS_NAME;
                    }
                }
        };
    }

    @Override
    public ConstructorInterceptorPoint[] getConstructorInterceptorPoints() {
        return new ConstructorInterceptorPoint[0];
    }

    @Override
    public StaticMethodInterceptorPoint[] getStaticMethodInterceptorPoints() {
        return new StaticMethodInterceptorPoint[0];
    }

}
