package com.example.springbootconfig.bean;

import com.example.springbootconfig.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * TODO
 *
 * @date:2019/12/28 20:58
 * @author: <a href='mailto:fanhaodong516@qq.com'>Anthony</a>
 */
@Configurable
@Component
public class MyBean {


    @Value("${user.name}")
    private String name;

    @Value("${user.age}")
    private int age;


    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }


    public int getAge() {
        return age;
    }


    public void setAge(int age) {
        this.age = age;
    }


    @Override
    public String toString() {
        return "MyBean{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
