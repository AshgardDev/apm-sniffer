package org.example.core.plugin;

import org.example.core.loader.AgentClassLoader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class PluginBootstrap {

    /**
     * 加载所有生效的插件
     *
     * @return
     */
    public List<AbstractClassEnhancePluginDefine> loadPlugins() throws IOException {
        AgentClassLoader.initDefaultLoader();
        Enumeration<URL> resources = AgentClassLoader.getDefault().getResources("skywalking-plugin.def");
        List<AbstractClassEnhancePluginDefine> plugins = new ArrayList<>();
        if (resources != null) {
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                PluginCfg.INSTANCE.load(url.openStream());
            }
        }

        for (PluginDefine pluginDefine : PluginCfg.INSTANCE.getPluginDefineList()) {
            try {
                AbstractClassEnhancePluginDefine plugin = (AbstractClassEnhancePluginDefine) Class.forName(pluginDefine.getDefineClass(), true, AgentClassLoader.getDefault()).newInstance();
                plugins.add(plugin);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return plugins;
    }
}
