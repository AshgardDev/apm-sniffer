package org.example.core.plugin.enhance;

import lombok.Getter;

/**
 * 处理类的上下文状态
 * ps：是为类新增属性时，判断是否第一次新增而衍生出来的辅助类
 */
public class EnhanceContext {

    /**
     * 是否被增强了
     */
    private boolean isEnhanced = false;

    /**
     * 是否新增了CONTEXT_ATTR_NAME
     */
    @Getter
    private boolean objectExtended = false;

    public void initializationStageCompleted() {
        isEnhanced = true;
    }

    public boolean isEnhanced() {
        return isEnhanced;
    }

    public void setEnhanced(boolean enhanced) {
        isEnhanced = enhanced;
    }

    public void objectExtendedCompleted() {
        this.objectExtended = true;
    }
}
