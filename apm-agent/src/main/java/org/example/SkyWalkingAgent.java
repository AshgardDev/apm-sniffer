package org.example;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import org.example.core.plugin.*;

import java.lang.instrument.Instrumentation;

/**
 * Java Agent 入口
 * @author hbj
 */
@Slf4j
public class SkyWalkingAgent {
    public static void premain(String args, Instrumentation instrumentation) {
        log.info("进入agent");
        PluginFinder pluginFinder = null;
        try {
            pluginFinder = new PluginFinder(new PluginBootstrap().loadPlugins());
        } catch (Exception e) {
            log.error("初始化插件加载错误", e);
            return;
        }
        // 可以设置byteBuddy的默认行为
        ByteBuddy byteBuddy = new ByteBuddy().with(TypeValidation.of(true));
        // 这里其实只是定义类增强逻辑，但不会马上加载类，要等到类被加载的时候才会真正加载对应的类并增强
        new AgentBuilder.Default(byteBuddy)
                // 匹配类
                .type(pluginFinder.buildTypeMatch())
                // 定义增强逻辑，从插件中获取增强逻辑
                .transform(new AgentTransformer(pluginFinder))
                .installOn(instrumentation);
    }
}