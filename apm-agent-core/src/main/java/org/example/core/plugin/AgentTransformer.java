package org.example.core.plugin;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

import java.security.ProtectionDomain;
import java.util.List;

public class AgentTransformer implements AgentBuilder.Transformer {

    private final PluginFinder pluginFinder;

    public AgentTransformer(PluginFinder pluginFinder) {
        this.pluginFinder = pluginFinder;
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
        List<AbstractClassEnhancePluginDefine> plugins = pluginFinder.find(typeDescription);
        DynamicType.Builder<?> newBuilder = builder;
        if(!plugins.isEmpty()){
            for (AbstractClassEnhancePluginDefine plugin : plugins) {
                DynamicType.Builder<?> possibleNewBuilder = plugin.define(newBuilder, typeDescription, classLoader, module, protectionDomain);
                if(possibleNewBuilder != null){
                    newBuilder = possibleNewBuilder;
                }
            }
        }
        return newBuilder;
    }
}
