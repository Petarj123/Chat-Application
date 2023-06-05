package com.auth.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
@RequestMapping("/application")
public class HomeController {
    @GetMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public String getLogin() {
        return "login";
    }
}
