package com.example.ProcessLeaveRequest.Controller;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = { "/see" })
public class LoginController {

	@GetMapping("")
	public String showLoginAs() {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		System.out.println(auth.getName());
		System.out.println(auth.getDetails());
		System.out.println(auth.getCredentials());
		System.out.println(auth.getPrincipal());

		Set<String> roles = auth.getAuthorities().stream().map(r -> r.getAuthority()).collect(Collectors.toSet());
		for (String s : roles)
			System.out.println(s);

		return "redirect:Employee";
	}

//	@GetMapping("/showMyLoginPage")
//	public String showMyLoginPage() {
//		
//		return "login";
//	}
//	
//	// add request mapping for /access-denied
//	@GetMapping("/access-denied")
//	public String showAccessDeniedPage() {
//		
//		return "access-denied";
//		
//	}
//	
//	@GetMapping("")
//    public String redirectOnLogin(Model model) {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (!(auth instanceof AnonymousAuthenticationToken)) {
//        	String role = auth.getAuthorities().toString();
//        	
//        	if(role.contains("ROLE_ADMIN"))
//        		return "redirect:/adminhome";
//        	else if(role.contains("ROLE_STUDENT"))
//        		return "redirect:/studenthome";
//        	else if(role.contains("ROLE_FACULTY"))
//        		return "redirect:/facultyhome";
//        }
//           
//        return "redirect:/";
//    }

}