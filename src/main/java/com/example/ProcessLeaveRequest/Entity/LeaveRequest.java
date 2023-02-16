package com.example.ProcessLeaveRequest.Entity;

public class LeaveRequest {
	// define fields
	private String startDate;

	private String endDate;

	private String comment;

	private String decision;

	private String employeeUsername;

	// constructors

	public LeaveRequest() {
	}

	public LeaveRequest(String startDate, String endDate, String comment, String decision, String employeeUsername) {
		super();
		this.startDate = startDate;
		this.endDate = endDate;
		this.comment = comment;
		this.decision = decision;
		this.employeeUsername = employeeUsername;
	}

	// getter and setter

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getDecision() {
		return decision;
	}

	public void setDecision(String decision) {
		this.decision = decision;
	}

	public String getEmployeeUsername() {
		return employeeUsername;
	}

	public void setEmployeeUsername(String employeeUsername) {
		this.employeeUsername = employeeUsername;
	}

	// toString

	@Override
	public String toString() {
		return "LeaveRequest [startDate=" + startDate + ", endDate=" + endDate + ", comment=" + comment + ", decision="
				+ decision + ", employeeUsername=" + employeeUsername + "]";
	}

}
