package com.example.todolist.form;

import lombok.Data;

@Data
public class TodoQuery {
	private String title;
	private Integer importance;
	private Integer urgency;
	private String deadlineFrom;
	private String deadlineTo;
	private String done;
	

	public TodoQuery() {
		this.title = "";
		this.importance = -1;
		this.urgency = -1;
		this.deadlineFrom = "";
		this.deadlineTo = "";
		this.done = "";
	}
	
	

}
