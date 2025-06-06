package com.collegeproject.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.collegeproject.dao.ContactRepository;
import com.collegeproject.dao.UserRepository;
import com.collegeproject.entities.Contact;
import com.collegeproject.entities.User;
import com.collegeproject.helper.Message;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bcp;
	@Autowired
	private UserRepository ur;
	@Autowired
	private ContactRepository cr;

	@PersistenceContext
	private EntityManager entityManager;

	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {

		String userName = principal.getName();
		System.out.println("USERNAME " + userName);

		User user = ur.getUserByUserName(userName);
		model.addAttribute("user", user);

	}

	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {
		model.addAttribute("title", "User DashBoard");

		return "normal/user_dashboard";
	}

	@GetMapping("/add-contact")
	public String addContact(Model model) {

		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());

		return "normal/add_contact_form";
	}

	@PostMapping("/process")
	public String processContact(@ModelAttribute Contact contact, Principal principal, HttpSession session) {

		try {

			String name = principal.getName();
			User user = this.ur.getUserByUserName(name);
			contact.setUser(user);
			user.getContacts().add(contact);
			this.ur.save(user);
			System.out.println("DATA IS " + contact);
			System.out.println("Added to data base");

		} catch (Exception e) {
			System.out.println("ERROR " + e.getMessage());
			e.printStackTrace();

		}

		return "normal/add_contact_form";
	}

	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {
		model.addAttribute("title", "Show User Contacts");

		String userName = principal.getName();

		User user = this.ur.getUserByUserName(userName);

		Pageable p = PageRequest.of(page, 5);
		Page<Contact> contacts = this.cr.findContactsByUser(user.getId(), p);

		model.addAttribute("contacts", contacts);

		model.addAttribute("currentPage", page);

		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";

	}

	// Delete Contact Handler....

	@PostMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session) {

		Optional<Contact> op = this.cr.findById(cId);
		Contact contact = op.get();

		this.cr.delete(contact);

		contact.setUser(null);

		return "redirect:/user/show-contacts/0";
	}

	// update Contact Handler

	@PostMapping("/update-contact/{cid}")
	public String updateContact(@PathVariable("cid") Integer cid, Model model) {

		model.addAttribute("title", "Update Contact");

		Contact contact = this.cr.findById(cid).get();
		model.addAttribute("contact", contact);
		return "normal/update_form";
	}

// process update contact handler

	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, Model model, HttpSession session,
			Principal principal) {

		try {

			User user = this.ur.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.cr.save(contact);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return "redirect:/user/show-contacts/0";
	}

	// your profile handler..

	@GetMapping("/profile")
	public String yourProfile(Model model) {

		model.addAttribute("title", "Your Profile");
		return "normal/profile";

	}

	// open setting handler

	@GetMapping("/settings")
	public String openSettings() {

		return "normal/settings";
	}

	// change password handler..

	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword,Principal principal,HttpSession session) {
		
		 String userName=principal.getName();
		User currentUser= this.ur.getUserByUserName(userName);
        
		if(this.bcp.matches(oldPassword, currentUser.getPassword()))
		{
			currentUser.setPassword(this.bcp.encode(newPassword));
			this.ur.save(currentUser);
			
		}
		
		else {
			return "redirect:/user/settings";
		}
		
		
		return "redirect:/user/index";
	}

}
