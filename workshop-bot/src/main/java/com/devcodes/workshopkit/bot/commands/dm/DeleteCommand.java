package com.devcodes.workshopkit.bot.commands.dm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.devcodes.workshopkit.bot.IMattermostEvent;
import com.devcodes.workshopkit.bot.MattermostBot;
import com.devcodes.workshopkit.bot.commands.IDirectBotCommand;
import com.devcodes.workshopkit.environment.EnvironmentService;

@Component
public class DeleteCommand implements IDirectBotCommand {
	
	@Autowired
	private EnvironmentService environmentService;

	@Override
	public void execute(IMattermostEvent event, MattermostBot bot, String[] args) {
		String username = (String) event.getData().get("sender_name");
		
		bot.sendMessage("Cleaning up your environment...", event.getPost().get().getChannelId());
		
		bot.sendOperationsMessage("Processing delete environment request for @"+username);
		
		try {
			environmentService.delete(username);
			
			bot.sendMessage("Your environment has been cleaned up", event.getPost().get().getChannelId());
			
			bot.sendOperationsMessage("Cleaned up environment for @"+username);
		} catch (Exception e) {
			e.printStackTrace();
			
			bot.sendMessage("There was a problem cleaning up your environment, an admin has been notified", event.getPost().get().getChannelId());
			
			bot.sendOperationsMessage("Failed to delete environment for @"+username, e);
		}
	}

	@Override
	public String keyword() {
		return "delete";
	}

	@Override
	public String description() {
		return "Deletes your environment completely";
	}
}
