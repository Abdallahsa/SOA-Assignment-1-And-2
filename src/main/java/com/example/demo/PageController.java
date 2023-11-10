package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/")
public class PageController 
{
	@GetMapping("/viewAll")
    public String viewAllStudents() 
	{
        return "viewAllStudents";
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
}
