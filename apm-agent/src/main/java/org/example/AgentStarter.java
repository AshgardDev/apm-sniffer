package org.example;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import org.example.core.plugin.*;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.List;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Slf4j
public class AgentStarter {
    public static void premain(String args, Instrumentation instrumentation) throws PluginException {
        // 加载所有插件
        List<AbstractClassEnhancePluginDefine> allPlugins = PluginLoader.loadPlugins();

        ElementMatcher.Junction<TypeDescription> typeMatcher = ElementMatchers.none();
        for (AbstractClassEnhancePluginDefine allPlugin : allPlugins) {
            ClassMatcher classMatcher = allPlugin.enhanceClass();
            typeMatcher.or(classMatcher.getClassMatcher());
        }

        new AgentBuilder.Default()
                .type(typeMatcher)
                .transform(new AgentBuilder.Transformer() {
                    @Override
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
                                                            ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {

                        builder = builder.method(named("").or(isStatic())).intercept(MethodDelegation.to(new Object()));
                        return builder;
                    }
                })
                .installOn(instrumentation);
    }

    public static void buildMethodMatcher(List<AbstractClassEnhancePluginDefine> allPlugins, DynamicType.Builder<?> builder, TypeDescription typeDescription,
                                          ClassLoader classLoader) throws PluginException {
        ElementMatcher.Junction<MethodDescription> methodMatcher = ElementMatchers.none();
        for (AbstractClassEnhancePluginDefine allPlugin : allPlugins) {
            InstanceMethodsInterceptorPoint[] instanceMethodsInterceptorPoints = allPlugin.getInstanceMethodsInterceptorPoints();
            for (InstanceMethodsInterceptorPoint instanceMethodsInterceptorPoint : instanceMethodsInterceptorPoints) {
                ElementMatcher<? super MethodDescription> methodsMatcher = instanceMethodsInterceptorPoint.getMethodsMatcher();
                builder = builder.method(methodMatcher).intercept(MethodDelegation.to(""));
            }
        }

    }
}