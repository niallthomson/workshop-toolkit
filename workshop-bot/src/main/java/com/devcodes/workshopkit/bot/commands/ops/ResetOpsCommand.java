package com.devcodes.workshopkit.bot.commands.ops;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.devcodes.workshopkit.bot.IMattermostEvent;
import com.devcodes.workshopkit.bot.MattermostBot;
import com.devcodes.workshopkit.bot.commands.IOpsBotCommand;
import com.devcodes.workshopkit.environment.EnvironmentException;
import com.devcodes.workshopkit.environment.EnvironmentService;

@Component
public class ResetOpsCommand implements IOpsBotCommand {
	
	@Autowired
	private EnvironmentService environmentService;

	@Override
	public void execute(IMattermostEvent event, MattermostBot bot, String[] args) {
		if(args.length != 1) {
			return;
		}
		
		String username = args[0];
		
		bot.sendOperationsMessage("Resetting environment for @"+username);

		bot.sendMessageByUsername("An admin has initiated a reset of your environment, you workspace will be unavailable while this is completed", username);
		
		try {
			environmentService.reset(username);
			
			bot.sendOperationsMessage("Environment reset for @"+username);
		} catch (EnvironmentException e) {
			bot.sendOperationsMessage("Failed to reset environment for @"+username, e);
		}
	}

	@Override
	public String keyword() {
		return "reset";
	}

	@Override
	public String description() {
		return "Resets a users environment completely";
	}
}
