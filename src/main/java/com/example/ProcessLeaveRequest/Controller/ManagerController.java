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

@Controller
@RequestMapping(value = { "/Manager" })
public class ManagerController {

	@Autowired
	private ZeebeClient zeebeClient;

	@GetMapping(value = { "/", "" })
	public String showManager(Model model) throws TaskListException {
		// auth with tasklist
		SelfManagedAuthentication sma = new SelfManagedAuthentication().clientId("tasklist")
				.clientSecret("XALaRPl5qwTEItdwCMiPS62nVpKs7dL7").keycloakUrl("http://localhost:18080")
				.keycloakRealm("camunda-platform");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder().shouldReturnVariables()
				.taskListUrl("http://localhost:8082/").authentication(sma).build();

		// get list of all review leave request task from tasklist
		TaskList tasks = client.getGroupTasks("manager", TaskState.CREATED, 50);

//		client.getGroupTasks("manager", TaskState.CREATED, 10);

		for (Task task : tasks) {
			System.out.println("task ID: " + task.getId());
			System.out.println(task.getProcessDefinitionId());
			System.out.println(task.getTaskDefinitionId());
			System.out.println(task.getCandidateGroups());
			List<Variable> variables = task.getVariables();
			for (Variable v : variables) {
				System.out.println(v.getName() + " : " + v.getValue());

			}
			System.out.println("-----------------------");
		}

		model.addAttribute("tasks", tasks);

		// add list to model

		return "manager-review-request-list";
	}

	@GetMapping(value = { "/LeaveRequestList" })
	public String showLeaveRequestList() {

		return "leave-request-list";
	}

	@GetMapping("/ReviewLeaveRequest")
	public String showReviewLeaveRequest(Model theModel, @RequestParam("taskID") String taskID)
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

		return "manager-review-leave-request-page";
	}

	@PostMapping("/ProcessReviewLeaveRequest")
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
		Map<String, Object> map = Map.of("field_decision", leaveRequest.getDecision());

		// Finish Task
		client.completeTask(taskID, map);

		return "redirect:/Manager";
	}

}