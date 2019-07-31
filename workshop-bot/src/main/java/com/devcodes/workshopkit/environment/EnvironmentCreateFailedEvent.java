package com.devcodes.workshopkit.environment;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class EnvironmentCreateFailedEvent extends ApplicationEvent {
	
	private String username;
	private EnvironmentDetails details;
	private Exception exception;

	public EnvironmentCreateFailedEvent(String username, EnvironmentDetails details, String message, EnvironmentService environmentService) {
		super(environmentService);
		
		this.username = username;
		this.details = details;
		this.exception = new Exception(message);
	}
}
