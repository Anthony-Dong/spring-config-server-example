package com.example.springbootconfig.autoconfig;

import com.example.springbootconfig.annotation.Configurable;
import com.example.springbootconfig.annotation.AutoConfigListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 配置前置处理器
 *
 * @date:2019/12/28 20:58
 * @author: <a href='mailto:fanhaodong516@qq.com'>Anthony</a>
 */
@Component
public class ConfigBeanPostProcess implements BeanPostProcessor {

    /**
     * 所有实例化对象
     */
    private Set<Object> set = new HashSet<>();


    /**
     * 所有的  method 监听器
     */
    private Map<String, Pair<Object, Method>> methodMap = new HashMap<>();


    /**
     * 所有的  key 自动注入
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
