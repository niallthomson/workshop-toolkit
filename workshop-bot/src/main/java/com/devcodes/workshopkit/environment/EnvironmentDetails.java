package com.devcodes.workshopkit.environment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentDetails {
	private String id;
	
	private String url;
	
	private String username;

	private String status;
}
