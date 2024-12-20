package org.example.core.match;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

public interface IndirectMatch extends ClassMatch {

    ElementMatcher.Junction<? super TypeDescription> buildJunction();

    /**
     * 是否匹配TypeDescription，用来在方法匹配时，筛选插件是否匹配要求
     * @param typeDescription 类描述
     * @return 插件是否匹配
     */
    boolean isMatch(TypeDescription typeDescription);
}
