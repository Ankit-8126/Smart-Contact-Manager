package com.collegeproject.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.collegeproject.dao.UserRepository;
import com.collegeproject.entities.User;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder bp;
	
	@Autowired
	private UserRepository ur;

	@RequestMapping("/")
	public String home(Model model) {
		model.addAttribute("title","Home - Smart Contact Manager");
		return "home";
	}
	
	@RequestMapping("/about")
	public String about(Model model) {
		model.addAttribute("title","About - Smart Contact Manager");
		return "about";
	}
	
	@RequestMapping("/signup")
	public String signup(Model model) {
		model.addAttribute("title","Register - Smart Contact Manager");
		model.addAttribute("user",new User());
		return "signup";
	}

	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") 
	User user,BindingResult br,@RequestParam(value="agreement",defaultValue="false")
	boolean agreement,Model model,
	HttpSession session) {
		
		try {
		if(!agreement) {
			System.out.println("you have not agreed terms and conditions");
			throw new Exception("you have not agrees terms and condition");
		}
		
		if(br.hasErrors()) {
			System.out.println("ERROR "+br.toString());
			model.addAttribute("user",user);
			return "signup";
		}
		
		user.setRole("ROLE_USER");
		user.setEnabled(true);	
		user.setImageUrl("default.png");
		user.setPassword(bp.encode(user.getPassword()));
		
		System.out.println("Agreement"+agreement);
		System.out.println("User"+user);
		
		User result=this.ur.save(user);
		model.addAttribute("user",new User());
		session.setAttribute("message",new com.collegeproject.helper.Message("Successfully Registered !!","alert-success"));
		return "signup";
		
		}
		catch(Exception e) {
			
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message",new com.collegeproject.helper.Message("something went wrong !!"+e.getMessage(),"alert-danger"));
			
			return "signup";
		}
		
	
		
	}
	
	@GetMapping("/signin")
	public String login(Model model) {
        model.addAttribute("title","Login Page");
		return "login";
	}
	
	
	
	
	
	
	
	
	
	
}
