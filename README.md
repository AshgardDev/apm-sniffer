# apm-sniffer

### 学习
从java-agent-study项目中，已经实现了agent的基本使用，其主要逻辑是：
在入口进行拦截，先匹配类@1，再匹配方法@2，然后进行拦截，最后交给拦截处理@3。
PS：@1 @2 @3请看代码注释，三个步骤是agent实现多插件匹配的核心。
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

补充知识点： maven中依赖包compile和provide包作用于的区别
特性	                    compile 作用域	                  provided 作用域
依赖的可用性      	编译时、测试时、运行时都可用  	    编译时、测试时可用，运行时由容器提供
是否包含在最终输出中	会包含在最终构建的JAR/WAR 中	    不会包含在最终构建的JAR/WAR 中
适用场景	        适用于项目中需要的所有库（比如常见的库）	适用于容器提供的库（如 Servlet API）
示例	            常见的第三方库，如 Apache Commons	    Servlet API、JEE API、Tomcat 等容器提供的库

### 如何多插件配置
我们关注上述三个步骤
@1: 匹配TypeDescription（即class），匹配逻辑完全是bytebuddy的ElementMatchers.Junction<? super TypeDescription>实现。
    ElementMatchers.Junction有提供and 和 or的实现，因此，如果要匹配多个类，即多个插件，可以采用如下为代码：
    ElementMatchers.Junction<TypeDescription> junction = 
          junction.or(SpringMVC插件类_junction)
                  .or(Sqlite3插件类_junction)
                  .xxx 
    即可实现多个插件匹配

@2: 匹配MethodDescription（即方法），匹配逻辑完全是bytebuddy的ElementMatchers.Junction<? super MethodDescription>实现。
    该匹配逻辑，和TypeDescription类似，只是匹配的是MethodDescription，伪代码如下。
    ElementMatchers.Junction<MethodDescription> junction = 
          ElementMatchers.method(SpringMVC插件类方法_junction).interceptor(SpringMVC方法拦截器)
                         .method(Sqlite3插件类_junction).interceptor(Sqlite3方法拦截器)
                         .method(xxxxx).interceptor(xxxxx)
    即可实现多方法匹配

@3：拦截器类的类加载和绑定
    拦截器类是最终会切入到应用代码中执行的，因此，拦截器的类需要能被应用类加载器加载到，否则，会抛出异常。

阅读上面的代码逻辑，这里要解决四个问题：
1.如何只指定一个agent，就可以拦截到所有插件 -- 采用@1的匹配方式
2.怎么加载多plugin 
3.怎么把typeDescription和要拦截的method关联起来 -- 类和方法是没有绑定关系的，必须有绑定约束，否则可能匹配到其他类的方法
4.怎么把typeDescription和要拦截的method的拦截器关联起来 -- 拦截器是和方法绑定的，方法又是和类绑定的，因此，需要一个绑定关系
 
举例，比如A插件要拦截ClassA的methodA，B插件要拦截ClassB的methodB，但ClassB中也有一个名叫methodA的方法，由于A和B插件都需要匹配，
所以类匹配逻辑中 junction.or(ClassA).or(ClassB),也就是ClassA和ClassB都匹配，但方法匹配的时候，如果没有类绑定关系，就会导致ClassB的methodA也被拦截了，
且拦截器是methodA的，就会出现不可预期的错误。

### 抽像插件定义
为了绑定类和方法，拦截器之间的关系，需要定义一个抽象插件定义，该抽象插件定义中，定义了拦截器，拦截器绑定关系，以及类和方法的匹配关系。
AbstractClassEnhancePluginDefine 抽象插件定义
```java
public abstract ClassMatch enhanceClass(); // 类匹配器
public abstract InstanceMethodInterceptorPoint[] getInstanceMethodInterceptorPoints(); // 实例方法拦截点
public abstract ConstructorInterceptorPoint[] getConstructorInterceptorPoints(); // 构造方法拦截点
public abstract StaticMethodInterceptorPoint[] getStaticMethodInterceptorPoints(); // 静态方法拦截点
```

类匹配器中定义一个顶级接口ClassMatch，两个子接口
1.NameMatch：通过全类名匹配
2.IndirectMatch：间接匹配（除了全类名的其他匹配方式）  

为什么要做这层接口分割呢？其实是为了将 多个类名匹配 合并成一个匹配器，减少or的分支代码
比如多个类名匹配，可以合并定义一个抽象匹配器，类似 ElementMatchers.oneOf(ClassA, ClassB, ClassC)，或者 allPlugins.containKey(name)等。  
间接匹配器因为不能直接通过全类名直接匹配，因此，需要借助ByteBuddy的ElementMatchers.Junction<? super TypeDescription>来匹配，
也就是说可能匹配到一个类，也可能匹配到多个类，因此，它需要有两个方法：
```java
    ElementMatcher.Junction<? super TypeDescription> buildJunction();

    /**
     * 是否匹配TypeDescription，用来在方法匹配时，筛选插件是否匹配要求--这个是在后面代码编写过程中，逐渐衍生出来的方法
     * @param typeDescription 类描述
     * @return 插件是否匹配
     */
    boolean isMatch(TypeDescription typeDescription);
```
第一个方法是指定匹配器，匹配逻辑
第二个方法是为了在进行方法匹配，过滤插件时，根据isMatch(TypeDescription typeDescription)来绑定类和方法的关系--即解决问题3,所以这里传入了类信息。


















