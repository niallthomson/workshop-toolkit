package com.devcodes.workshopkit.bot.commands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.devcodes.workshopkit.bot.IMattermostEvent;
import com.devcodes.workshopkit.bot.MattermostBot;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BotCommandProcessor<T extends IBotCommand> {
	private Map<String, T> commands;
	
	private MattermostBot bot;
	
	public BotCommandProcessor(MattermostBot bot) {
		this.bot = bot;
		this.commands = new HashMap<>();
	}
	
	public void register(T command) {
		this.commands.put(command.keyword(), command);
	}
	
	public void execute(IMattermostEvent event) {
		String[] split = event.getPost().get().getMessage().split(" ");
		
		if(split.length > 0) {
			String keyword = split[0];
			
			IBotCommand command = commands.get(keyword);
			
			if(command != null) {
				command.execute(event, bot, Arrays.copyOfRange(split, 1, split.length));
			}
			else {
				log.debug("No command for {}", keyword);
			}
		}
		
	}
}
