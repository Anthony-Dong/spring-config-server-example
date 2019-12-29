package com.example.springbootconfig.annotation;

import java.lang.annotation.*;

/**
 * 监听key
 *
 * @date:2019/12/29 13:55
 * @author: <a href='mailto:fanhaodong516@qq.com'>Anthony</a>
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoConfigListener {
    String key();
}
