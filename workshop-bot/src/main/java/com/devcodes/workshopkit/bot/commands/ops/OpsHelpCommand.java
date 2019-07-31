package com.devcodes.workshopkit.bot.commands.ops;

import java.util.List;

import com.devcodes.workshopkit.bot.commands.AbstractHelpCommand;
import com.devcodes.workshopkit.bot.commands.IBotCommand;
import com.devcodes.workshopkit.bot.commands.IDirectBotCommand;
import com.devcodes.workshopkit.bot.commands.IOpsBotCommand;

public class OpsHelpCommand extends AbstractHelpCommand<IOpsBotCommand> implements IOpsBotCommand {

	public OpsHelpCommand(List<IOpsBotCommand> commands) {
		super(commands);
	}

}
