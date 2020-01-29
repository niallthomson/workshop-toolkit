package com.devcodes.workshopkit.bot.commands.dm;

import com.devcodes.workshopkit.bot.IMattermostEvent;
import com.devcodes.workshopkit.bot.MattermostBot;
import com.devcodes.workshopkit.bot.commands.IDirectBotCommand;
import com.devcodes.workshopkit.environment.EnvironmentDetails;
import com.devcodes.workshopkit.environment.EnvironmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatusCommand implements IDirectBotCommand {

	@Autowired
	private EnvironmentService environmentService;

	@Override
	public void execute(IMattermostEvent event, MattermostBot bot, String[] args) {
		String username = (String) event.getData().get("sender_name");

		EnvironmentDetails existing = environmentService.retrieve(username);

		String message = "You do not currently have an environment, please `request` if you require one.";

		if (existing != null) {
			switch(existing.getStatus()) {
				case "Active":
					message = "You have an existing environment active at https://" + existing.getUrl()+"/?folder=/home/coder/project";
					break;
				case "Creating":
					message = "Your environment is still being created, I'll let you know when its done...";
					break;
				case "Deleting":
					message = "Your environment is being deleted, I'll let you know when its done...";
					break;
			}
		}

		bot.sendMessage(message,
				event.getPost().get().getChannelId());
	}

	@Override
	public String keyword() {
		return "status";
	}

	@Override
	public String description() {
		return "Provides the current status of your environment";
	}
}
