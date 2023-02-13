package com.example.ProcessLeaveRequest.Controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.ProcessLeaveRequest.Entity.LeaveRequest;

import io.camunda.tasklist.exception.TaskListException;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

@Controller
@RequestMapping(value = { "/Employee" })
public class EmployeeController {

	@Autowired
	private ZeebeClient zeebeClient;

	@GetMapping(value = { "/", "" })
	public String showEmployeeHome() {

		return "employee-home";
	}

	@GetMapping("/RequestLeave")
	public String showRequestLeaveForm(Model theModel) {

		// define variablies
		LeaveRequest leaveRequest = new LeaveRequest();

		// add variablies to Model
		theModel.addAttribute("leaveRequest", leaveRequest);

		System.out.println("Leave Request page sent successfully!");

		return "employee-request-leave-page";
	}

	@PostMapping("/RequestLeave")
	public String startRequestLeaveProcess(@ModelAttribute("LeaveRequest") LeaveRequest leaveRequest)
			throws TaskListException {

		// get user name
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String username = auth.getName();

		Map<String, Object> map = Map.of("user_submitted", username, "field_startDate", leaveRequest.getStartDate(),
				"field_endDate", leaveRequest.getEndDate(), "field_comment", leaveRequest.getComment());

		// Start Process
		final ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand().bpmnProcessId("Process_RequestLeave")
				.latestVersion().variables(map).send().join();

		System.out.println("Process instance started successfully!");

		return "redirect:/Employee";
	}

}