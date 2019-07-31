package com.devcodes.workshopkit.bot.commands.dm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.devcodes.workshopkit.bot.IMattermostEvent;
import com.devcodes.workshopkit.bot.MattermostBot;
import com.devcodes.workshopkit.bot.commands.IDirectBotCommand;
import com.devcodes.workshopkit.environment.EnvironmentDetails;
import com.devcodes.workshopkit.environment.EnvironmentService;

@Component
public class RequestCommand implements IDirectBotCommand {

	@Autowired
	private EnvironmentService environmentService;

	@Override
	public void execute(IMattermostEvent event, MattermostBot bot, String[] args) {
		String username = (String) event.getData().get("sender_name");

		EnvironmentDetails existing = environmentService.retrieve(username);

		if (existing != null) {
			bot.sendMessage("You have an existing environment at https://" + existing.getUrl(),
					event.getPost().get().getChannelId());

			bot.sendMessage(
					"If you want to reset it please send me a message saying `reset`, or delete it with `delete`",
					event.getPost().get().getChannelId());

			return;
		}

		bot.sendOperationsMessage("Creating an environment for @" + username);

		bot.sendMessage("Your workspace is being created. Sit back and wait, I'll ping you when its complete...",
				event.getPost().get().getChannelId());

		try {
			environmentService.request(username);
		} catch (Exception e) {
			bot.sendMessage("There was a problem provisioning your workspace, and admin has been notified",
					event.getPost().get().getChannelId());

			bot.sendOperationsMessage("Failed to create an environment for @" + username, e);
		}
	}

	@Override
	public String keyword() {
		return "request";
	}

	@Override
	public String description() {
		return "Requests an environment to work in";
	}
}
