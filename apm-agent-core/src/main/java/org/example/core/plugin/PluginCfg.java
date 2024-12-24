package org.example.core.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public enum PluginCfg {

    INSTANCE;

    private final List<PluginDefine> pluginDefineList = new ArrayList<>();

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

    public List<PluginDefine> getPluginDefineList() {
        return pluginDefineList;
    }
}
