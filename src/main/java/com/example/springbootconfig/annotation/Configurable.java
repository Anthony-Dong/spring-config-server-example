package com.example.springbootconfig.annotation;

import java.lang.annotation.*;

/**
 * 可以自动刷新的类  字段可以自动刷新  @value注解的配置
 *
 * @date:2019/12/28 21:11
 * @author: <a href='mailto:fanhaodong516@qq.com'>Anthony</a>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Configurable {


}
