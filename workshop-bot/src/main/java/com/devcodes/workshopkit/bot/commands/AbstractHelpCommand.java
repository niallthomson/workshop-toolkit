package com.devcodes.workshopkit.bot.commands;

import java.util.List;

import com.devcodes.workshopkit.bot.IMattermostEvent;
import com.devcodes.workshopkit.bot.MattermostBot;

public abstract class AbstractHelpCommand<T extends IBotCommand> implements IBotCommand {

	private List<T> commands;

	public AbstractHelpCommand(List<T> commands) {
		this.commands = commands;
	}

	@Override
	public void execute(IMattermostEvent event, MattermostBot bot, String[] args) {
		StringBuilder sb = new StringBuilder();

		sb.append("I can do a few things for you: \n\n");

		for (IBotCommand command : this.commands) {
			sb.append("`").append(command.keyword()).append("`: ").append(command.description()).append("\n");
		}

		bot.sendMessage(sb.toString(), event.getPost().get().getChannelId());
	}

	@Override
	public String keyword() {
		return "help";
	}

	@Override
	public String description() {
		return "This help message";
	}

}
