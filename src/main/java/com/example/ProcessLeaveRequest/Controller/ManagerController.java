package com.example.ProcessLeaveRequest.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
@RequestMapping(value = { "/Manager" })
public class ManagerController {

	@Autowired
	private ZeebeClient zeebeClient;

	@GetMapping(value = { "/", "" })
	public String showManager() throws TaskListException {
		// auth with tasklist
		SelfManagedAuthentication sma = new SelfManagedAuthentication().clientId("tasklist")
				.clientSecret("XALaRPl5qwTEItdwCMiPS62nVpKs7dL7").keycloakUrl("http://localhost:18080")
				.keycloakRealm("camunda-platform");

		CamundaTaskListClient client = new CamundaTaskListClient.Builder().shouldReturnVariables()
				.taskListUrl("http://localhost:8082/").authentication(sma).build();

		// get list of all review leave request task from tasklist
		TaskList tasks = client.getTasks(false, TaskState.CREATED, 50);

//				client.getGroupTasks("manager", TaskState.CREATED, 10);

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

		// add list to model

		return "manager-review-request-list";
	}

	@GetMapping(value = { "/LeaveRequestList" })
	public String showLeaveRequestList() {

		return "leave-request-list";
	}

	@GetMapping("/ReviewRequestLeave")
	public String showReviewLeaveRequest(Model theModel) {

		// define variablies
		LeaveRequest leaveRequest = new LeaveRequest();

		// add variablies to Model
		theModel.addAttribute("leaveRequest", leaveRequest);

		return "employee-request-leave-page";
	}

	@PostMapping("/RequestLeave")
	public String processReviewLeaveRequest(@ModelAttribute("LeaveRequest") LeaveRequest leaveRequest)
			throws TaskListException {

		Map<String, Object> map = Map.of("user_submitted", "demo", "field_startDate", leaveRequest.getStartDate(),
				"field_endDate", leaveRequest.getEndDate(), "field_comment", leaveRequest.getComment()); // need to
																											// implement
																											// get
		// authentication which
		// user submitted

		// Start Process
		final ProcessInstanceEvent event = zeebeClient.newCreateInstanceCommand().bpmnProcessId("Process_RequestLeave")
				.latestVersion().variables(map).send().join();

		return "redirect:/EmployeeRequestLeave";
	}

}