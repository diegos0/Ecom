package com.ecom.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class UserController {

    @GetMapping("/")
    public String home() {
        return "user/home";
    }

}
