package org.example.apm.springmvc;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.example.core.plugin.*;

import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.nameEndsWith;

public class SpringMvcPlugin extends AbstractClassEnhancePluginDefine {

    private static final String CONTROLLER_NAME = "org.springframework.stereotype.Controller";
    private static final String REST_CONTROLLER_NAME = "org.springframework.web.bind.annotation.RestController";
    private static final String MAPPING_PKG_PREFIX = "org.springframework.web.bind.annotation";
    private static final String MAPPING = "Mapping";
    private static final String INTERCEPTOR_CLASS_NAME = "org.example.apm.springmvc.SpringMvcInterceptor";

    @Override
    public ClassMatcher enhanceClass() throws PluginException {
        return new ClassMatcher() {
            @Override
            public ElementMatcher<? super TypeDescription> getClassMatcher() {
                return isAnnotatedWith(named(CONTROLLER_NAME)).or(isAnnotatedWith(named(REST_CONTROLLER_NAME)));
            }
        };
    }

    @Override
    public InstanceMethodsInterceptorPoint[] getInstanceMethodsInterceptorPoints() throws PluginException {
        return new InstanceMethodsInterceptorPoint[]{
                new InstanceMethodsInterceptorPoint() {
                    @Override
                    public ElementMatcher<? super MethodDescription> getMethodsMatcher() {
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
    public ConstructorInterceptorPoint[] getConstructorInterceptorPoints() throws PluginException {
        return new ConstructorInterceptorPoint[0];
    }

    @Override
    public String[] getStaticMethodsInterceptorPoints() throws PluginException {
        return new String[0];
    }

}
