package com.example.springbootconfig;

import com.example.springbootconfig.annotation.AutoConfigListener;
import com.example.springbootconfig.bean.MyBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class SpringBootConfigApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootConfigApplication.class, args);
    }

    @Autowired
    private MyBean myBean;

    @Override
    public void run(String... args) throws Exception {
        System.out.println(" bean : " + myBean);

        TimeUnit.SECONDS.sleep(10);
        System.out.println(" 修改后的 : bean : " + myBean);

    }

    @AutoConfigListener(key = "${user.name}")
    public void onEvent(Object o) {
        System.out.println("event : " + o);
    }
}
