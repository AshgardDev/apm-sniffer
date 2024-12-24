package org.example.core.plugin;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.example.core.plugin.match.ClassMatch;
import org.example.core.plugin.match.IndirectMatch;
import org.example.core.plugin.match.NameMatch;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.bytebuddy.matcher.ElementMatchers.isInterface;
import static net.bytebuddy.matcher.ElementMatchers.not;

/**
 * 插件查找器
 */
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

    /**
     * 构建插件查找器，将插件按照classMatch类型进行分类
     * NameMatch分到nameMatchDefine中
     * IndirectMatch分到signatureMatchDefine中
     * @param plugins
     */
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

    /**
     * 构造最终的类匹配器
     * @return
     */
    public ElementMatcher<? super TypeDescription> buildTypeMatch() {
        // 一个抽象匹配器，匹配所有nameMatch匹配的类
        ElementMatcher.Junction<? super TypeDescription> junction = new ElementMatcher.Junction.AbstractBase<TypeDescription>() {
            @Override
            public boolean matches(TypeDescription target) {
                return nameMatchDefine.containsKey(target.getActualName());
            }
        };

        // 匹配所有非接口
        junction = junction.and(not(isInterface()));

        // 匹配所有indirectMatch匹配的类
        for (AbstractClassEnhancePluginDefine pluginDefine : signatureMatchDefine) {
            ClassMatch classMatch = pluginDefine.enhanceClass();
            if (classMatch instanceof IndirectMatch) {
                IndirectMatch indirectMatch = (IndirectMatch) classMatch;
                junction = junction.or(indirectMatch.buildJunction());
            }
        }
        return junction;
    }

    /**
     * 根据具体的类信息 查找 匹配的插件
     * ps： 这个方法非常重要，主要是用在方法匹配时，获取对应的类的插件信息。 也是IndirectMatch中 isMatch方法产生的原因
     * @param typeDescription
     * @return
     */
    public List<AbstractClassEnhancePluginDefine> find(TypeDescription typeDescription) {
        List<AbstractClassEnhancePluginDefine> matchPlugins = new LinkedList<>();
        // 匹配所有nameMatch匹配的插件
        if (nameMatchDefine.containsKey(typeDescription.getTypeName())) {
            matchPlugins.addAll(nameMatchDefine.get(typeDescription.getTypeName()));
        }
        // 匹配所有indirectMatch匹配的插件
        for (AbstractClassEnhancePluginDefine pluginDefine : signatureMatchDefine) {
            ClassMatch classMatch = pluginDefine.enhanceClass();
            if (classMatch instanceof IndirectMatch) {
                IndirectMatch indirectMatch = (IndirectMatch) classMatch;
                // 类是否符合间接匹配器的条件
                if (indirectMatch.isMatch(typeDescription)) {
                    matchPlugins.add(pluginDefine);
                }
            }
        }

        return matchPlugins;
    }
}
