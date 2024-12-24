package org.example.core.plugin;

import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 插件def收集器
 */
@Getter
public enum PluginCfg {

    INSTANCE;

    /**
     * def收集列表
     */
    private final List<PluginDefine> pluginDefineList = new ArrayList<>();

    /**
     * 读取def内容，转成PluginDefine列表
     * @param input
     * @throws IOException
     */
    void load(InputStream input) throws IOException {
        try (
                BufferedReader br = new BufferedReader(new InputStreamReader((input)));
        ) {
            String pluginDefine;
            while ((pluginDefine = br.readLine()) != null) {
                pluginDefineList.add(PluginDefine.build(pluginDefine));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
