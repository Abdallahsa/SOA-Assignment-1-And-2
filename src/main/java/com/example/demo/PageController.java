package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


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

	@GetMapping("/sortedStudents")
	public String sortedStudents()
	{
		return "sortedStudents";
	}

	@GetMapping("/updateStudent")
	public String updateStudent(@RequestParam(name = "id") String studentId, Model model)
	{
		 model.addAttribute("studentId", studentId);
		return "updateStudent";
	}

	@GetMapping("/searchstudent")
	public String searchstudent()
	{
		return "SearchStudent";
	}

}