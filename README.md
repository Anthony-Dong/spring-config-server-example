# Nacos 和 Apollo 配置中心实现实时配置刷新

> ​	其实他借助了 Spring的两个特性 , 第一个就是`org.springframework.core.env.Environment`  , 这个是Spring所有配置信息的元信息接口, 其中其子实现类 `ConfigurableEnvironment` , 使我们需要拿到的对象,对其进行修改
>
> ​	还有一个就是`BeanPostProcess` , Bean初始化完成的后置处理器, 这里面可以拿到已经实例化完成的Bean. 此时我们可以对其进行过滤 以及其他手脚 , 这个借助于Spring的单例对象模式 ,真的很不错

## 1. 获取Environment

1) 第一种方式 `org.springframework.context.ApplicationContextInitializer`  接口

需要在`META-INF/spring.factories` 中写入,他的初始化很早, 在Bean装载之前,所以不能用`@Component`注入

```properties
org.springframework.context.ApplicationContextInitializer=\
com.example.springbootconfig.autoconfig.ConfigAutoApplication
```

```java
public class ConfigAutoApplication implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();

   		// 处理
    }
}
```

2) 第二种方式 `org.springframework.boot.env.EnvironmentPostProcessor` 接口

需要在 `META-INF/spring.factories` 中添加

```properties
org.springframework.boot.env.EnvironmentPostProcessor=\
com.example.springbootconfig.autoconfig.EnvironmentAutoWire
```

此时可以通过 

```java
public class EnvironmentAutoWire implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		// 获取对象
    }
}
```

3) 第三种方式 : `org.springframework.context.EnvironmentAware`

```java
@Component
public class ConfigEnvironmentAware implements EnvironmentAware, PriorityOrdered {
    @Override
    public void setEnvironment(Environment environment) {
        // 处理
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
```

这三种方式加载的时间

`org.springframework.boot.env.EnvironmentPostProcessor`  早与 `org.springframework.context.ApplicationContextInitializer`  早于` org.springframework.context.EnvironmentAware`

所以第二种方式注入的时候最早

## 2. 处理单例Bean

> ​	这里我们需要拿到Bean对象, 这就需要所谓的`org.springframework.beans.factory.config.BeanPostProcessor` ,这里面可以获取已经实例化的Bean , 此时我们可以对其加一修饰

```java
@Component
public class ConfigBeanPostProcess implements BeanPostProcessor {

    /**
     * 保存所有实例化对象
     */
    private Set<Object> set = new HashSet<>();


    /**
     * 保存所有的被@AutoConfigListener修饰方法对象和方法调用对象
     */
    private Map<String, Pair<Object, Method>> methodMap = new HashMap<>();


    /**
     * 保存所有的被@Configurable修饰的类 , 同时获取他的被 @Value修饰的字段和字段对象
     */
    private volatile Map<String, Pair<Object, Field>> map = new HashMap<>();


    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> aClass = bean.getClass();


        /**
         * 监听器
         */
        Method[] methods = aClass.getMethods();
        for (Method method : methods) {
            AutoConfigListener annotation = method.getAnnotation(AutoConfigListener.class);

            if (annotation != null) {

                String key = annotation.key();

                if (method.getParameters().length == 1 && method.getParameters()[0].getType() == Object.class) {
                    methodMap.put(key, new Pair<>(bean, method));
                }
            }

        }


        /**
         * 注入属性
         */
        if (aClass == ConfigServer.class) {
            ConfigServer server = (ConfigServer) bean;
            server.map = map;
            server.methodMap = methodMap;

        }


        /**
         * 配置bean - > 自动修改字段值设置值
         */
        Configurable annotation = aClass.getAnnotation(Configurable.class);
        if (annotation != null) {
            set.add(bean);

            Field[] fields = bean.getClass().getDeclaredFields();

            for (Field field : fields) {
                Value value = field.getAnnotation(Value.class);
                if (null != value) {
                    // private需要设置可见性
                    field.setAccessible(true);
                    String value1 = value.value();
                    map.put(value1, new Pair<>(bean, field));
                }
            }
        }
        return bean;
    }


    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }
}
```



这里借助了 两个注解修饰

```java
/**
 * 监听器 处理器
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoConfigListener {
    String key();
}
```

```java
/**
 * 可以自动刷新的类  字段可以自动刷新  @value注解的配置
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Configurable {

}
```

主要逻辑就是 当Bean被 `Configurable` 修饰时 ,`@value` 注解标记的就会被刷新

当Bean的method, 被 `AutoConfigListener`  修饰时, 他的方法会被我们调用执行,当他所监听的配置刷新的时候

主要是实现逻辑很简单 ,我们需要拿到 `Field` 和 `Method` 对象, 和这俩对象所对应的实例化对象

```java
@Component
public class ConfigServer {


    Map<String, Pair<Object, Method>> methodMap;
    Map<String, Pair<Object, Field>> map;


    /**
     * 这里监听器还可以细分
     */
    private ConfigListener listener = new ConfigListener() {
        @Override
        public void onEvent(String key, Object value) {

            Pair<Object, Method> objectMethodPair = methodMap.get(key);
            Method v = objectMethodPair.getV();
            Object k = objectMethodPair.getK();

            try {
                v.invoke(k, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }

            
            Pair<Object, Field> pair = map.get(key);
            try {
                pair.getV().set(pair.getK(), value);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 模拟拉去远程服务
     *
     * @return
     */
    static Map<String, Object> getRemoteConfig() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("user.name", "xiaoli");
        map.put("user.age", 1);
        return map;
    }


    /**
     * 启动线程模拟拉去
     */
    public ConfigServer() {
        new Thread(() -> {
            System.out.println("启动配置中心监听");
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // 收到配置发生变化, 发布事件
            listener.onEvent("${user.name}", "测试");
        }).start();
    }
}
```



## 3. 总结

其实基本是基于Spring的单例模式带来的优势, 有些人会投机取巧的问 , 如果我将`Environment` 配置后期修改了, Bean所对应的属性不就改了吗. 实际上是错误的, 因为他不会修改, 因为啥呢,因为Spring说过, Bean只会实例化一次, 不会多次刷新, 我们知道刷新过程会自动装载bean. 但是`SpringApplication`不允许



其次还有设计上 ,我上面的Demo就写了半个小时, 基本就是一个框框 ,没考虑太多的设计成分 . 其实可以基于SpringApplicationEvent, 和 SpringApplication的publish方法来实现,  事件的发布, 无所谓了 , 其实思想都是一样的,



配置中心拉去信息, 监听是否发生变化,  变化了发布事件, 进行修改操作 . 无非这些操作

## 4. Apollo和Nacos是如何实现的

**Apollo** 的实现在于 `com.ctrip.framework.apollo.spring.boot.ApolloApplicationContextInitializer` 和 `com.ctrip.framework.apollo.spring.config.PropertySourcesProcessor` 这两个类来实现Env的注入 , 

其实对于刷新 主要是基于`com.ctrip.framework.apollo.spring.annotation.ApolloProcessor` 这个类对于Bean的信息保存 



其中**Nacos**的实现基本是在于`com.alibaba.nacos.spring.context.annotation.config.NacosValueAnnotationBeanPostProcessor` 这个类实现 对于`com.alibaba.nacos.api.annotation.NacosInjected` 这个注解的实现配置自动注入的功能

`com.alibaba.nacos.spring.context.annotation.config.NacosConfigListenerMethodProcessor` 这个类实现对于`com.alibaba.nacos.api.config.annotation.NacosConfigListener`这个监听方法的实现配置刷新自动调用的功能

`com.alibaba.nacos.spring.beans.factory.annotation.AnnotationNacosInjectedBeanPostProcessor` 实现对`com.alibaba.nacos.api.annotation.NacosInjected` 功能注入的实现

所以实现并不难



难就难在谁的设计最优雅, 其实都是基于Spring提供的强大的事件处理机制,配置注入简单, 配置刷新的方式基本都一样 , 难就难在设计谁的更巧妙





我写的在我的github上的项目里 叫 `spring-config-server` 项目里