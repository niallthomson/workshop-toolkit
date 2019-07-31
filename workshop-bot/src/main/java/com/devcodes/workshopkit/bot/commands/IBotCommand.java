package com.devcodes.workshopkit.bot.commands;

import com.devcodes.workshopkit.bot.IMattermostEvent;
import com.devcodes.workshopkit.bot.MattermostBot;

public interface IBotCommand {
	public void execute(IMattermostEvent event, MattermostBot bot, String[] args);
	
	public String keyword();
	
	public String description();
}
