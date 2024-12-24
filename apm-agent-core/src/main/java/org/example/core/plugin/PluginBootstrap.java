package org.example.core.plugin;

import org.example.core.loader.AgentClassLoader;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * 插件启动类
 */
public class PluginBootstrap {

    public static final String PLUGIN_DEFINE_FILE = "skywalking-plugin.def";

    /**
     * 加载所有生效的插件
     *
     * @return
     */
    public List<AbstractClassEnhancePluginDefine> loadPlugins() throws IOException {
        // 初始化默认的类加载器
        AgentClassLoader.initDefaultLoader();
        // 获取所有的def文件--这个也就是为什么要重写getResources方法的原因
        List<URL> resources = PluginResourcesResolver.getResources();
        for (URL url : resources) {
            // 读取def文件内容，并转成PluginDefine对象，存储到PluginCfg的配置列表里
            PluginCfg.INSTANCE.load(url.openStream());
        }

        List<AbstractClassEnhancePluginDefine> plugins = new ArrayList<>();
        for (PluginDefine pluginDefine : PluginCfg.INSTANCE.getPluginDefineList()) {
            try {
                // 通过反射创建插件对象--使用默认类加载器
                AbstractClassEnhancePluginDefine plugin = (AbstractClassEnhancePluginDefine) Class.forName(pluginDefine.getDefineClass(), true, AgentClassLoader.getDefault()).newInstance();
                plugins.add(plugin);
            } catch (Exception e) {
                throw new RuntimeException("插件定义类" + pluginDefine.getDefineClass() + "加载失败", e);
            }
        }

        return plugins;
    }
}
