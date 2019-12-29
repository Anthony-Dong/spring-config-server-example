package com.example.springbootconfig.autoconfig;

/**
 * @date:2019/12/29 13:36
 * @author: <a href='mailto:fanhaodong516@qq.com'>Anthony</a>
 */
public interface ConfigListener {
    void onEvent(String key, Object value);
}
