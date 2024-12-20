package org.example.core.plugin;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.example.core.match.ClassMatch;
import org.example.core.match.IndirectMatch;
import org.example.core.match.NameMatch;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.not;

public class PluginFinder {

    /**
     * 用于存放nameMatch匹配到的插件容器
     * key：全类名
     * value：插件列表 (可能存在多个) 说明同一个类可以被多个插件增强
     */
    private final Map<String, LinkedList<AbstractClassEnhancePluginDefine>> nameMatchDefine = new ConcurrentHashMap<>();

    /**
     * 用于存储indirectMatch匹配到的插件容器
     */
    private final List<AbstractClassEnhancePluginDefine> signatureMatchDefine = new LinkedList<>();

    public PluginFinder(List<AbstractClassEnhancePluginDefine> plugins) {
        for (AbstractClassEnhancePluginDefine pluginDefine : plugins) {
            ClassMatch classMatch = pluginDefine.enhanceClass();
            if (classMatch == null) {
                continue;
            }
            if (classMatch instanceof NameMatch) {
                NameMatch nameMatch = (NameMatch) classMatch;
                String className = nameMatch.getClassName();
                LinkedList<AbstractClassEnhancePluginDefine> list = nameMatchDefine.computeIfAbsent(className, k -> new LinkedList<>());
                list.add(pluginDefine);
            } else {
                signatureMatchDefine.add(pluginDefine);
            }
        }
    }

    public ElementMatcher<? super TypeDescription> buildTypeMatch() {
        ElementMatcher.Junction<? super TypeDescription> junction = new ElementMatcher.Junction.AbstractBase<TypeDescription>() {
            @Override
            public boolean matches(TypeDescription target) {
                return nameMatchDefine.containsKey(target.getActualName());
            }
        };

        // 优化，直接使用namedOneOf
//        ElementMatcher.Junction<? super TypeDescription> junction = ElementMatchers.namedOneOf(nameMatchDefine.keySet().toArray(new String[0]));

        junction = junction.and(not(isInterface()));

        for (AbstractClassEnhancePluginDefine pluginDefine : signatureMatchDefine) {
            ClassMatch classMatch = pluginDefine.enhanceClass();
            if (classMatch instanceof IndirectMatch) {
                IndirectMatch indirectMatch = (IndirectMatch) classMatch;
                junction = junction.or(indirectMatch.buildJunction());
            }
        }
        return junction;
    }

    public List<AbstractClassEnhancePluginDefine> find(TypeDescription typeDescription) {
        List<AbstractClassEnhancePluginDefine> matchPlugins = new LinkedList<>();
        if (nameMatchDefine.containsKey(typeDescription.getTypeName())) {
            matchPlugins.addAll(nameMatchDefine.get(typeDescription.getTypeName()));
        }
        for (AbstractClassEnhancePluginDefine pluginDefine : signatureMatchDefine) {
            ClassMatch classMatch = pluginDefine.enhanceClass();
            if (classMatch instanceof IndirectMatch) {
                IndirectMatch indirectMatch = (IndirectMatch) classMatch;
                if (indirectMatch.isMatch(typeDescription)) {
                    matchPlugins.add(pluginDefine);
                }
            }
        }

        return matchPlugins;
    }
}
