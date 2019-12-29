package com.example.springbootconfig;


import java.lang.reflect.Field;

public class SpringBootConfigApplicationTests {


	public static void main(String[] args) throws Exception {

		Field name = Bean.class.getDeclaredField("name");
		name.setAccessible(true);

		Bean bean = new Bean("name");

		name.set(bean, "aaaaaaaa");


		System.out.println(bean);

//		Class.forName("").getClassLoader();




	}
}
