package org.example;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import org.example.core.plugin.*;

import java.lang.instrument.Instrumentation;

@Slf4j
public class SkyWalkingAgent {
    public static void premain(String args, Instrumentation instrumentation) throws PluginException {
        PluginFinder pluginFinder = null;
        try {
            pluginFinder = new PluginFinder(null);
        } catch (Exception e) {
            log.error("Init plugin finder error", e);
            return;
        }
        ByteBuddy byteBuddy = new ByteBuddy().with(TypeValidation.of(true));
        new AgentBuilder.Default(byteBuddy)
                .type(pluginFinder.buildTypeMatch())
                .transform(new AgentTransformer(pluginFinder))
                .installOn(instrumentation);
    }
}