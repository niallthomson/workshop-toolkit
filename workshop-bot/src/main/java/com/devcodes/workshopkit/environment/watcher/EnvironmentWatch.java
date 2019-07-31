package com.devcodes.workshopkit.environment.watcher;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EnvironmentWatch {
	private String id;
	
	private String fqdn;
	
	private Date seeded;
}