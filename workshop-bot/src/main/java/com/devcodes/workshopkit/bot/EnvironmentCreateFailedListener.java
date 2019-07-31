package com.devcodes.workshopkit.bot;

import com.devcodes.workshopkit.environment.EnvironmentCreateFailedEvent;
import lombok.extern.java.Log;
import org.springframework.context.ApplicationListener;

@Log
public class EnvironmentCreateFailedListener implements ApplicationListener<EnvironmentCreateFailedEvent> {

	private MattermostBot bot;

	public EnvironmentCreateFailedListener(MattermostBot bot) {
		this.bot = bot;
	}

	@Override
    public void onApplicationEvent(EnvironmentCreateFailedEvent event) {
		log.info("Received environment failed event "+event);
		
		bot.sendMessageByUsername("There was a problem creating your environment, I've let someone know for you", event.getUsername());
		
		bot.sendOperationsMessage("Failed to create environment for @"+event.getUsername(), event.getException());
    }
}
