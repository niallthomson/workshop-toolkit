package com.devcodes.workshopkit.bot.commands.dm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.devcodes.workshopkit.bot.IMattermostEvent;
import com.devcodes.workshopkit.bot.MattermostBot;
import com.devcodes.workshopkit.bot.commands.IDirectBotCommand;
import com.devcodes.workshopkit.environment.EnvironmentService;

@Component
public class ResetCommand implements IDirectBotCommand {

	@Autowired
	private EnvironmentService environmentService;

	@Override
	public void execute(IMattermostEvent event, MattermostBot bot, String[] args) {
		String username = (String) event.getData().get("sender_name");

		bot.sendOperationsMessage("Resetting environment for @" + username);

		bot.sendMessage("I'm resetting your environment, I'll ping you when its complete...",
				event.getPost().get().getChannelId());

		try {
			environmentService.reset(username);
		} catch (Exception e) {
			bot.sendMessage("There was a problem resetting your workspace, and admin has been notified",
					event.getPost().get().getChannelId());

			bot.sendOperationsMessage("Failed to reset an environment for @" + username, e);
		}
	}

	@Override
	public String keyword() {
		return "reset";
	}

	@Override
	public String description() {
		return "Resets your environment to its initial state";
	}
}
