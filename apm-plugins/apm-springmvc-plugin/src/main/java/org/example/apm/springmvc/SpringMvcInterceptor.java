package org.example.apm.springmvc;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.annotation.*;
import org.example.core.plugin.enhance.EnhancedInstance;
import org.example.core.plugin.enhance.InstanceMethodAroundInterceptor;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.Callable;

@Slf4j
public class SpringMvcInterceptor implements InstanceMethodAroundInterceptor {

    @Override
    public void beforeMethod(EnhancedInstance instance, Method method, Object[] allArguments) {
        log.info("SpringMvc方法Before拦截， 拦截方法：{}， 参数：{}", method.getName(), Arrays.toString(allArguments));
        instance.setSkyWalkingDynamicField("this is context");
    }

    @Override
    public void handleException(EnhancedInstance instance, Method method, Object[] allArguments, Exception e) {
        log.info("SpringMvc方法Exception拦截， 拦截方法：{}， 异常：{}", method.getName(), e.getMessage(), e);
    }

    @Override
    public Object afterMethod(Object result, EnhancedInstance instance, Method method, Object[] allArguments) {
        Object skyWalkingDynamicField = instance.getSkyWalkingDynamicField();
        if (skyWalkingDynamicField != null) {
            System.out.println("skyWalkingDynamicField = " + skyWalkingDynamicField);
        }
        return result;
    }
}
