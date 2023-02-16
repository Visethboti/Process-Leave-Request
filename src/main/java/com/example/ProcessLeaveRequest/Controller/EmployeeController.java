package com.example.ProcessLeaveRequest.Controller;

import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;

import com.example.ProcessLeaveRequest.Entity.LeaveRequest;

import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.auth.SelfManagedAuthentication;
import io.camunda.tasklist.dto.Task;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.dto.TaskState;
import io.camunda.tasklist.dto.Variable;
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

	@GetMapping("/RequestLeaveList")
	public String showRequestLeaveList(Model theModel) throws TaskListException {

		// auth to tasklist
		SelfManagedAuthentication sma = new SelfManagedAuthentication().clientId("tasklist")
				.clientSecret("XALaRPl5qwTEItdwCMiPS62nVpKs7dL7").keycloakUrl("http://localhost:18080")
				.keycloakRealm("camunda-platform");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder().shouldReturnVariables()
				.taskListUrl("http://localhost:8082/").authentication(sma).build();

		// get all tasks assigned to this user
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		TaskList tasks = client.getAssigneeTasks(auth.getName(), TaskState.CREATED, 10);

		// add to model
		theModel.addAttribute("tasks", tasks);

		return "employee-request-leave-list";
	}

	@GetMapping("/ReviewLeaveRequest")
	public String showReviewRequestLeave(Model theModel, @RequestParam("taskID") String taskID)
			throws TaskListException {

		// define variablies
		LeaveRequest leaveRequest = new LeaveRequest();

		// get task variables
		SelfManagedAuthentication sma = new SelfManagedAuthentication().clientId("tasklist")
				.clientSecret("XALaRPl5qwTEItdwCMiPS62nVpKs7dL7").keycloakUrl("http://localhost:18080")
				.keycloakRealm("camunda-platform");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder().shouldReturnVariables()
				.taskListUrl("http://localhost:8082/").authentication(sma).build();

		Task task = client.getTask(taskID);
		List<Variable> variables = task.getVariables();

		for (Variable v : variables) {
			System.out.println(v.getName() + ":" + v.getValue());
			if (v.getName().equals("user_submitted"))
				leaveRequest.setEmployeeUsername((String) v.getValue());
			else if (v.getName().equals("field_startDate"))
				leaveRequest.setStartDate((String) v.getValue());
			else if (v.getName().equals("field_endDate"))
				leaveRequest.setEndDate((String) v.getValue());
			else if (v.getName().equals("field_comment"))
				leaveRequest.setComment((String) v.getValue());

		}

		// add variablies to Model
		theModel.addAttribute("leaveRequest", leaveRequest);
		theModel.addAttribute("taskID", taskID);

		return "employee-review-request-leave";
	}

	@PostMapping("/ProcessReviewRequestLeave")
	public String processReviewLeaveRequest(@ModelAttribute("LeaveRequest") LeaveRequest leaveRequest,
			@RequestParam("taskID") String taskID) throws TaskListException {

		SelfManagedAuthentication sma = new SelfManagedAuthentication().clientId("tasklist")
				.clientSecret("XALaRPl5qwTEItdwCMiPS62nVpKs7dL7").keycloakUrl("http://localhost:18080")
				.keycloakRealm("camunda-platform");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder().shouldReturnVariables()
				.taskListUrl("http://localhost:8082/").authentication(sma).build();

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		// Claim Task
		client.claim(taskID, auth.getName());

		// retrieve variables
		Map<String, Object> map = Map.of("field_comment", leaveRequest.getComment());

		// Finish Task
		client.completeTask(taskID, map);

		return "redirect:/Employee/RequestLeaveList";
	}
}