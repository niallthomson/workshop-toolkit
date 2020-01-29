package com.devcodes.workshopkit.bot;

import com.devcodes.workshopkit.environment.EnvironmentCreatedEvent;
import com.devcodes.workshopkit.environment.EnvironmentDetails;
import lombok.extern.java.Log;
import org.springframework.context.ApplicationListener;

@Log
public class EnvironmentCreatedListener implements ApplicationListener<EnvironmentCreatedEvent> {

	private MattermostBot bot;

	public EnvironmentCreatedListener(MattermostBot bot) {
		this.bot = bot;
	}

	@Override
    public void onApplicationEvent(EnvironmentCreatedEvent event) {
		log.info("Received environment created event "+event);
	
		EnvironmentDetails details = event.getDetails();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("Your environment is available at https://")
		  .append(details.getUrl())
		  .append("/?folder=/home/coder/project\n")
		  .append("If prompted, allow the workspace access to your Mattermost profile. Only you have access to this environment.");
		
		bot.sendMessageByUsername(sb.toString(), event.getUsername());
		
		bot.sendOperationsMessage("Successfully created environment for @"+event.getUsername()+" - https://"+details.getUrl());
    }
}
