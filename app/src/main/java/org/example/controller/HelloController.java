package org.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@RestController
public class HelloController {

    @PostConstruct
    public void init(){
        System.out.println("HelloController.class.getClassLoader() = " + HelloController.class.getClassLoader());
    }

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }
}
