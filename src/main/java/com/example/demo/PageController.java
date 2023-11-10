package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/")
public class PageController 
{
	@GetMapping("/")
    public String showSpecificPage() {
        return "Home";
    }
	
	@GetMapping("/add")
    public String Add() {
        return "Add.html";
    }
	
    @GetMapping("/searchName")
    public String search() {
        return "searchByName.html";
    }
    
    @GetMapping("/searchByGpa")
    public String viewSearchByGpa() 
	{
        return "searchByGpa";
    }
	
	@GetMapping("/delete")
    public String viewDelete() 
	{
        return "deleteById";
    }
	
	@GetMapping("/all")
	public String viewAll()
	{
		return "viewAllStudents";
	}

}