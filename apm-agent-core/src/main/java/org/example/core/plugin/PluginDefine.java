package org.example.core.plugin;

import lombok.Getter;

/**
 * Def插件定义
 */
@Getter
public class PluginDefine {

    private String name;

    private String defineClass;

    private PluginDefine(String name, String defineClass) {
        this.name = name;
        this.defineClass = defineClass;
    }

    public static PluginDefine build(String define) {
        String[] pluginDefine = define.split("=");
        return new PluginDefine(pluginDefine[0], pluginDefine[1]);
    }

}
