package com.example.springbootconfig;

/**
 * TODO
 *
 * @date:2019/12/28 23:54
 * @author: <a href='mailto:fanhaodong516@qq.com'>Anthony</a>
 */
public class Bean {

    private String name;

    private static String age;

    public Bean(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Bean{" +
                "name='" + name + '\'' +
                '}';
    }
}
