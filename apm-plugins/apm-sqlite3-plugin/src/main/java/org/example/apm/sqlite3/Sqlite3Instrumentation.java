package org.example.apm.sqlite3;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.example.core.match.ClassMatch;
import org.example.core.match.MultiClassNameMatch;
import org.example.core.plugin.*;
import org.example.core.plugin.enhance.ClassEnhancePluginDefine;
import org.example.core.plugin.interceptor.ConstructorInterceptorPoint;
import org.example.core.plugin.interceptor.InstanceMethodInterceptorPoint;
import org.example.core.plugin.interceptor.StaticMethodInterceptorPoint;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

public class Sqlite3Instrumentation extends ClassEnhancePluginDefine {


    private static final String JDBC3_PREPARED_STATEMENT = "org.sqlite.jdbc3.JDBC3PreparedStatement";

    private static final String JDBC4_PREPARED_STATEMENT = "org.sqlite.jdbc4.JDBC4PreparedStatement";

    private static final String INTERCEPTOR_CLASS_NAME = "org.example.apm.sqlite3.Sqlite3Interceptor";

    @Override
    public ClassMatch enhanceClass() {
        return MultiClassNameMatch.byMultiClassMatch(JDBC3_PREPARED_STATEMENT, JDBC4_PREPARED_STATEMENT);
    }

    @Override
    public InstanceMethodInterceptorPoint[] getInstanceMethodInterceptorPoints() {
        return new InstanceMethodInterceptorPoint[] {
                new InstanceMethodInterceptorPoint() {
                    @Override
                    public ElementMatcher.Junction<? super MethodDescription> buildMethodJunction() {
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
    public ConstructorInterceptorPoint[] getConstructorInterceptorPoints() {
        return new ConstructorInterceptorPoint[0];
    }

    @Override
    public StaticMethodInterceptorPoint[] getStaticMethodInterceptorPoints() {
        return new StaticMethodInterceptorPoint[0];
    }
}
