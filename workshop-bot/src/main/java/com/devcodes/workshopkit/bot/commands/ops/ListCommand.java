package com.devcodes.workshopkit.bot.commands.ops;

import com.devcodes.workshopkit.bot.IMattermostEvent;
import com.devcodes.workshopkit.bot.MattermostBot;
import com.devcodes.workshopkit.bot.commands.IOpsBotCommand;
import com.devcodes.workshopkit.environment.EnvironmentDetails;
import com.devcodes.workshopkit.environment.EnvironmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListCommand implements IOpsBotCommand {
	
	@Autowired
	private EnvironmentService environmentService;

	@Override
	public void execute(IMattermostEvent event, MattermostBot bot, String[] args) {
		List<EnvironmentDetails> environments = environmentService.list();
		
		if(environments.size() > 0) {
			StringBuilder sb = new StringBuilder();
			
			sb.append("| Username  | URL | Status |\n");
			sb.append("| :--- | :--- | :--- |\n");
			
			for(EnvironmentDetails env : environments) {
				sb.append("| @").append(env.getUsername())
				.append(" | https://").append(env.getUrl())
				.append(" | ").append(env.getStatus())
				.append(" |\n");
			}
			
			bot.sendMessage("These workspaces currently exist:", event.getPost().get().getChannelId());
			
			bot.sendMessage(sb.toString(), event.getPost().get().getChannelId());
		}
		else {
			bot.sendMessage("There are no workspaces currently running", event.getPost().get().getChannelId());
		}
	}

	@Override
	public String keyword() {
		return "list";
	}

	@Override
	public String description() {
		return "Lists all environments running";
	}
}
