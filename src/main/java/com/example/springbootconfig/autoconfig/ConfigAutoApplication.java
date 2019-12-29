package com.example.springbootconfig.autoconfig;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @date:2019/12/28 20:46
 * @author: <a href='mailto:fanhaodong516@qq.com'>Anthony</a>
 */
@Component
public class ConfigAutoApplication implements ApplicationContextInitializer<ConfigurableApplicationContext>, PriorityOrdered {

    /**
     * Initialize the given application context.
     *
     * @param applicationContext the application to configure
     */
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {

        System.out.println("========ApplicationContextInitializer===================");

        ConfigurableEnvironment environment = applicationContext.getEnvironment();


        MutablePropertySources propertySources = environment.getPropertySources();

        // 模拟远程拉配置信息
        Map<String, Object> config = ConfigServer.getRemoteConfig();

        MapPropertySource propertySource = new MapPropertySource("test-config", config);
        propertySources.addFirst(propertySource);
    }

    /**
     * Get the order value of this object.
     * <p>Higher values are interpreted as lower priority. As a consequence,
     * the object with the lowest value has the highest priority (somewhat
     * analogous to Servlet {@code load-on-startup} values).
     * <p>Same order values will result in arbitrary sort positions for the
     * affected objects.
     *
     * @return the order value
     * @see #HIGHEST_PRECEDENCE
     * @see #LOWEST_PRECEDENCE
     */
    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
