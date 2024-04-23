package com.fosketeers.rrss;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class UserController {
    @RequestMapping("/user/{id}")
    public String userProfileHandler(Model model, @PathVariable String id) {
        return "user";
    }

    @RequestMapping("/login")
    public String loginHandler(Model model) {
        return "login";
    }

    @RequestMapping("/signup")
    public String registerHandler(Model model) {
        return "register";
    }
}
