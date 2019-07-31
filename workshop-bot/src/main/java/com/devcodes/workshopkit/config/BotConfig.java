package com.devcodes.workshopkit.config;

import com.devcodes.workshopkit.bot.EnvironmentCreateFailedListener;
import com.devcodes.workshopkit.bot.EnvironmentCreatedListener;
import com.devcodes.workshopkit.bot.MattermostBot;
import com.devcodes.workshopkit.bot.commands.IDirectBotCommand;
import com.devcodes.workshopkit.bot.commands.IOpsBotCommand;
import com.devcodes.workshopkit.environment.EnvironmentService;
import com.devcodes.workshopkit.util.IHealthReporter;
import com.devcodes.workshopkit.util.ILandingRedirectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConditionalOnProperty(
		value="mattermost.bot.enabled",
		havingValue = "true",
		matchIfMissing = true)
public class BotConfig {
	
	@Value("${mattermost.host}")
	private String host;

	@Value("${mattermost.secure:true}")
	private boolean secure;
	
	@Value("${mattermost.admin.user}")
	private String adminUser;
	
	@Value("${mattermost.admin.password}")
	private String adminPassword;
	
	@Value("${mattermost.operator.user}")
	private String operatorUser;
	
	@Value("${mattermost.bot.name:workshop-bot}")
	private String botName;
	
	@Value("${mattermost.bot.callback.url}")
	private String oauthCallbackUrl;
	
	@Bean
	public MattermostBot mattermostBot(List<IDirectBotCommand> privateCommands, List<IOpsBotCommand> opsCommands, EnvironmentService environmentService) {
		return new MattermostBot(botName, host, secure, adminUser, adminPassword, operatorUser, oauthCallbackUrl, privateCommands, opsCommands, environmentService);
	}

	@Bean
	public EnvironmentCreatedListener environmentCreatedListener(MattermostBot bot) {
		return new EnvironmentCreatedListener(bot);
	}

	@Bean
	public EnvironmentCreateFailedListener environmentCreateFailedListener(MattermostBot bot) {
		return new EnvironmentCreateFailedListener(bot);
	}

	@Bean
	public IHealthReporter botHealthReporter(MattermostBot bot) {
		return new IHealthReporter() {
			@Override
			public boolean isHealthy() {
				return bot.hasInitialized();
			}
		};
	}

	@Bean
	public ILandingRedirectProvider mattermostRedirectProvider(MattermostBot bot) {
		return new ILandingRedirectProvider() {
			@Override
			public String getRedirectUrl() {
				return bot.getTeamInviteUrl();
			}
		};
	}
}
