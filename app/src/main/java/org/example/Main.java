package org.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("org.example.mapper")
public class Main {
    public static void main(String[] args) {
        System.out.println(Main.class.getClassLoader());
        System.out.println(SpringApplication.class.getClassLoader());
        SpringApplication.run(Main.class);
    }
}