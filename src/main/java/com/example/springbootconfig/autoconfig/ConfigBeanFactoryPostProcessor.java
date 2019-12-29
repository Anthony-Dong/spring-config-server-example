package com.example.springbootconfig.autoconfig;


import org.springframework.context.EnvironmentAware;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * TODO
 *
 * @date:2019/12/29 14:30
 * @author: <a href='mailto:fanhaodong516@qq.com'>Anthony</a>
 */
@Component
public class ConfigBeanFactoryPostProcessor implements EnvironmentAware, PriorityOrdered {


    @Override
    public void setEnvironment(Environment environment) {
        System.out.println("========EnvironmentAware===================");
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
