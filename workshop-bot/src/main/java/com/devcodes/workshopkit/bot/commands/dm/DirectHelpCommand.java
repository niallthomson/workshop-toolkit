package com.devcodes.workshopkit.bot.commands.dm;

import java.util.List;

import com.devcodes.workshopkit.bot.commands.AbstractHelpCommand;
import com.devcodes.workshopkit.bot.commands.IDirectBotCommand;

public class DirectHelpCommand extends AbstractHelpCommand<IDirectBotCommand> implements IDirectBotCommand {

	public DirectHelpCommand(List<IDirectBotCommand> commands) {
		super(commands);
	}

}
