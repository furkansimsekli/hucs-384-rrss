package com.fosketeers.rrss.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProductController {
    @GetMapping("/product/{id}")
    public String productGetHandler(@PathVariable String id) {
        return "product";
    }
}
