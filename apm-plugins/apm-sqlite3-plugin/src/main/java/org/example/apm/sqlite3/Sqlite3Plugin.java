package org.example.apm.sqlite3;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.example.core.plugin.*;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class Sqlite3Plugin extends AbstractClassEnhancePluginDefine {

    private static final String JDBC_PREPARED_STATEMENT = "org.sqlite.jdbc4.JDBC4PreparedStatement";

    private static final String INTERCEPTOR_CLASS_NAME = "org.example.apm.sqlite3.Sqlite3Interceptor";

    @Override
    public ClassMatcher enhanceClass() throws PluginException {
        return new ClassMatcher() {
            @Override
            public ElementMatcher<? super TypeDescription> getClassMatcher() {
                return named(JDBC_PREPARED_STATEMENT);
            }
        };
    }

    @Override
    public InstanceMethodsInterceptorPoint[] getInstanceMethodsInterceptorPoints() throws PluginException {
        return new InstanceMethodsInterceptorPoint[] {
                new InstanceMethodsInterceptorPoint() {
                    @Override
                    public ElementMatcher<? super MethodDescription> getMethodsMatcher() {
                        return nameStartsWith("exec");
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
