package com.devcodes.workshopkit.bot.commands.ops;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.devcodes.workshopkit.bot.IMattermostEvent;
import com.devcodes.workshopkit.bot.MattermostBot;
import com.devcodes.workshopkit.bot.commands.IOpsBotCommand;
import com.devcodes.workshopkit.environment.EnvironmentException;
import com.devcodes.workshopkit.environment.EnvironmentService;

@Component
public class DeleteOpsCommand implements IOpsBotCommand {
	
	@Autowired
	private EnvironmentService environmentService;

	@Override
	public void execute(IMattermostEvent event, MattermostBot bot, String[] args) {
		if(args.length != 1) {
			return;
		}
		
		String username = args[0];
		
		bot.sendOperationsMessage("Deleting environment for @"+username);

		bot.sendMessageByUsername("An admin has initiated a deletion of your workspace, and your environment will be terminating soon", username);
		
		try {
			environmentService.delete(username);
			
			bot.sendOperationsMessage("Environment deleted for @"+username);
		} catch (EnvironmentException e) {
			bot.sendOperationsMessage("Failed to delete environment for @"+username, e);
		}
	}

	@Override
	public String keyword() {
		return "delete";
	}

	@Override
	public String description() {
		return "Deletes a users environment";
	}
}
