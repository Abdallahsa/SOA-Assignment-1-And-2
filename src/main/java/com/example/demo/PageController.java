package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")

public class PageController {
    @GetMapping("/")
    public String showSpecificPage() {
        return "Home.html";
    }

    @GetMapping("/add")
    public String Add() {
        return "Add.html";
    }
    @GetMapping("/searchName")
    public String search() {
        return "searchByName.html";
    }

}