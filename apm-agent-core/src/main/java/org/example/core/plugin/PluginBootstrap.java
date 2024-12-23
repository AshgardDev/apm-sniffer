package org.example.core.plugin;

import org.example.core.loader.AgentClassLoader;

import java.util.List;

public class PluginBootstrap {

    /**
     * 加载所有生效的插件
     * @return
     */
    public List<AbstractClassEnhancePluginDefine> loadPlugins() {
        AgentClassLoader.initDefaultLoader();
        return null;
    }
}
