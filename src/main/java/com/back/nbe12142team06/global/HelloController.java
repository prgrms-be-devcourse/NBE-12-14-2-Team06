package com.back.nbe12142team06.global;

import org.springframework.web.bind.annotation.GetMapping;

public class HelloController {
    @GetMapping("/")
    public String hello() {
        return "hello";
    }
}
