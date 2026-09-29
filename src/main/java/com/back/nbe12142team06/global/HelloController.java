package com.back.nbe12142team06.global;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
public class HelloController {
    @GetMapping("/")
    public String hello() {
        return "hello";
    }
}
