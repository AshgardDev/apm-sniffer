package org.example.core.plugin;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;
import org.example.core.plugin.enhance.EnhanceContext;

import java.security.ProtectionDomain;
import java.util.List;

@Slf4j
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
            EnhanceContext context = new EnhanceContext();
            for (AbstractClassEnhancePluginDefine plugin : plugins) {
                DynamicType.Builder<?> possibleNewBuilder = plugin.define(newBuilder, typeDescription, classLoader, module, protectionDomain, context);
                if(possibleNewBuilder != null){
                    newBuilder = possibleNewBuilder;
                }
            }
            if(context.isEnhanced()){
                log.info("类{}扩展结束", typeDescription.getActualName());
            }
        }
        return newBuilder;
    }
}
