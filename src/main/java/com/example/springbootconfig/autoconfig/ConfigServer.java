package com.example.springbootconfig.autoconfig;

import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @date:2019/12/29 13:40
 * @author: <a href='mailto:fanhaodong516@qq.com'>Anthony</a>
 */
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
            System.out.println("启动监听");
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            listener.onEvent("${user.name}", "测试");
        }).start();
    }
}
