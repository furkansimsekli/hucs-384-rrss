package com.fosketeers.rrss.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ProductController {
    @RequestMapping("/product/{id}")
    public String productHandler(@PathVariable String id) {
        return "product";
    }
}
