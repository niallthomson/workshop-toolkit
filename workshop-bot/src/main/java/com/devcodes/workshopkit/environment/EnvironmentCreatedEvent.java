package com.devcodes.workshopkit.environment;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

@Getter
public class EnvironmentCreatedEvent extends ApplicationEvent {
	
	private String username;
	private EnvironmentDetails details;

	public EnvironmentCreatedEvent(String username, EnvironmentDetails details, Object source) {
		super(source);
		
		this.username = username;
		this.details = details;
	}
}
