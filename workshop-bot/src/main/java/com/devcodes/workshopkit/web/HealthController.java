package com.devcodes.workshopkit.web;

import com.devcodes.workshopkit.util.IHealthReporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
	
	@Autowired
	private IHealthReporter healthReporter;
	
	@GetMapping("/health")
	public ResponseEntity<String> health() {
		return healthReporter.isHealthy() ? new ResponseEntity<String>("OK", HttpStatus.OK) :
			new ResponseEntity<String>("STARTING", HttpStatus.SERVICE_UNAVAILABLE);
	}
}