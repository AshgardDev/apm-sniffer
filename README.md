# apm-sniffer

### 学习
从java-agent-study项目中，已经实现了agent的基本使用，其主要逻辑是：
在入口进行拦截，先匹配类@1，再匹配方法@2，然后进行拦截，最后交给拦截处理@3。
```java
public static void premain(String args, Instrumentation instrumentation) {
    new AgentBuilder.Default()
            //@1 这里是匹配类
            .type(new ElementMatcher<TypeDescription>() {
                @Override
                public boolean matches(TypeDescription target) {
                    // 这里实现类的匹配规则，这里是可以无差别判断哪些类要拦截
                    return false;
                }})
            .transform(new AgentBuilder.Transformer() {
                @Override
                public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
                                                        ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
                    //@2 这里是匹配方法
                    builder = builder.method(new ElementMatcher<MethodDescription>() {
                        @Override
                        public boolean matches(MethodDescription target) {
                            // 这里实现方法的匹配规则，这里是可以无差别判断哪些方法要拦截
                            return false;
                        }})
                    //@3 这里是交给拦截器处理
                    .intercept(MethodDelegation.to(new Interceptor()));
                    return builder;
                }
            })
            .installOn(instrumentation);
}
```
从上面的3个步骤可知，在类和方法匹配的时候，是可以任意拦截的    
因此，我们可以将多个插件的类匹配逻辑和方法匹配逻辑进行组合，即可实现多个插件同时拦截。  
伪代码：  
```java
public static void premain(String args, Instrumentation instrumentation) {
    // 先收集所有的插件
    // 插件应该包含类匹配逻辑，方法匹配逻辑，静态方法匹配逻辑，构造函数匹配逻辑等等，还有方法对应的拦截器
    List<Plugin> allPlugins = Plugin.loadAll();
    new AgentBuilder.Default()
            //@1 这里是匹配类
            .type(new ElementMatcher<TypeDescription>() {
                @Override
                public boolean matches(TypeDescription target) {
                    // 这里实现类的匹配规则，这里是可以无差别判断哪些类要拦截
                    boolean match = false;
                    for(Plugin plugin: allPlugins){
                        match = match || plugin.matchClass(target);
                    }
                    return false;
                }})
            .transform(new AgentBuilder.Transformer() {
                @Override
                public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription,
                                                        ClassLoader classLoader, JavaModule module, ProtectionDomain protectionDomain) {
                    //@2 这里是匹配方法
                    builder = builder.method(new ElementMatcher<MethodDescription>() {
                        @Override
                        public boolean matches(MethodDescription target) {
                            // 这里实现方法的匹配规则，这里是可以无差别判断哪些方法要拦截
                            boolean match = false;
                            for(Plugin plugin: allPlugins){
                                match = match || plugin.matchMethod(target);
                            }
                            return false;
                        }})
                    //@3 这里是交给拦截器处理
                    // 根据当前类和方法信息，查找插件的拦截器，这里的拦截器和classLoader关系非常重要！
                    .intercept(MethodDelegation.to(Plugin.loadInterceptor(typeDescription, methodDescription, classLoader)));
                    return builder;
                }
            })
            .installOn(instrumentation);
}
```

### 抽像插件定义
AbstractClassEnhancePluginDefine 抽象插件定义


知识点： maven中依赖包compile和provide包作用于的区别
特性	                    compile 作用域	                  provided 作用域
依赖的可用性      	编译时、测试时、运行时都可用  	    编译时、测试时可用，运行时由容器提供
是否包含在最终输出中	会包含在最终构建的JAR/WAR 中	    不会包含在最终构建的JAR/WAR 中
适用场景	        适用于项目中需要的所有库（比如常见的库）	适用于容器提供的库（如 Servlet API）
示例	            常见的第三方库，如 Apache Commons	    Servlet API、JEE API、Tomcat 等容器提供的库














