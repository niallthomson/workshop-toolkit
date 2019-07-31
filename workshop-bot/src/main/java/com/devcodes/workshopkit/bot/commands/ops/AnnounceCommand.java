package com.devcodes.workshopkit.bot.commands.ops;

import org.springframework.stereotype.Component;
import org.stringtemplate.v4.ST;

import com.devcodes.workshopkit.bot.IMattermostEvent;
import com.devcodes.workshopkit.bot.MattermostBot;
import com.devcodes.workshopkit.bot.commands.IOpsBotCommand;

@Component
public class AnnounceCommand implements IOpsBotCommand {
	
	private ST messageTemplate;
	
	private static final String DEFAULT_TEMPLATE = "@channel **Announcement** <message>";

	public AnnounceCommand() {
		this(DEFAULT_TEMPLATE);
	}
	
	public AnnounceCommand(String messageTemplateString) {
		this.messageTemplate = new ST(messageTemplateString);
	}

	@Override
	public void execute(IMattermostEvent event, MattermostBot bot, String[] args) {
		bot.sendMessageByChannelName(this.messageTemplate.add("message", String.join(" ", args)).render(), "town-square");
	}

	@Override
	public String keyword() {
		return "announce";
	}

	@Override
	public String description() {
		return "Make an announcement in the Town Square";
	}
}
