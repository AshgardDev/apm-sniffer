package org.example.core.plugin;

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

    public String getName() {
        return name;
    }

    public String getDefineClass() {
        return defineClass;
    }
}
