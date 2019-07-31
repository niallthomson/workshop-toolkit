package com.devcodes.workshopkit.bot;

import com.devcodes.workshopkit.bot.commands.BotCommandProcessor;
import com.devcodes.workshopkit.bot.commands.IDirectBotCommand;
import com.devcodes.workshopkit.bot.commands.IOpsBotCommand;
import com.devcodes.workshopkit.bot.commands.dm.DirectHelpCommand;
import com.devcodes.workshopkit.bot.commands.ops.OpsHelpCommand;
import com.devcodes.workshopkit.environment.EnvironmentService;
import lombok.extern.slf4j.Slf4j;
import net.bis5.mattermost.client4.ApiResponse;
import net.bis5.mattermost.client4.MattermostClient;
import net.bis5.mattermost.model.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@Slf4j
public class MattermostBot implements MattermostEventListener {
	
	private String host;
	private boolean secure;

	private MattermostWebsocketDispatcher wsClient;

	private MattermostClient adminClient;
	private MattermostClient botClient;

	private BotCommandProcessor<IDirectBotCommand> privateProcessor;
	private BotCommandProcessor<IOpsBotCommand> opsProcessor;
	
	private STGroup stGroup;
	
	private EnvironmentService environmentService;

	private String botName;

	private User botUser;

	private UserAccessToken accessToken;

	private Team team;

	private Channel operationsChannel;

	private User operatorUser;

	private String operatorName;
	
	private String oauthCallbackUrl;
	
	private static final String BOT_PASSWORD="alwaysbekind";
	
	private static final String OPS_CHANNEL_NAME = "ops";
	
	private static final String OAUTH_APP_NAME = "Workshop Bot";
	
	private boolean hasInitialized = false;

	public MattermostBot(String botName, String host, boolean secure, String adminUsername, String adminPassword, String operatorUser, String oauthCallbackUrl, List<IDirectBotCommand> privateCommands, List<IOpsBotCommand> opsCommands, EnvironmentService environmentService) {
		this.botName = botName;
		this.host = host;
		this.secure = secure;
		this.operatorName = operatorUser;
		this.oauthCallbackUrl = oauthCallbackUrl;
		
		this.privateProcessor = new BotCommandProcessor<IDirectBotCommand>(this);
		this.privateProcessor.register(new DirectHelpCommand(privateCommands));
		
		for (IDirectBotCommand command : privateCommands) {
			log.info("Registering private command " + command.getClass().getName());

			this.privateProcessor.register(command);
		}
		
		this.opsProcessor = new BotCommandProcessor<IOpsBotCommand>(this);
		this.opsProcessor.register(new OpsHelpCommand(opsCommands));
		
		for (IOpsBotCommand command : opsCommands) {
			log.info("Registering ops command " + command.getClass().getName());

			this.opsProcessor.register(command);
		}
		
		this.adminClient = createMattermostClient(adminUsername, adminPassword);
		
		this.stGroup = new STGroupFile("yml/bot-messages.stg");
		
		this.environmentService = environmentService;
	}

	@PostConstruct
	public void init() {
		this.team = getTeam();
		
		createOAuthAppIfNeeded();
		
		this.botUser = createOrGetBotUser();
		
		this.operatorUser = getAdminUser();
		
		this.operationsChannel = this.createOrGetOperationsChannel();
		
		botClient = createMattermostClient(this.botUser.getUsername(), BOT_PASSWORD);
		
		this.accessToken = createBotAccessToken();
		
		this.wsClient = new MattermostWebsocketDispatcher(host, secure, this.accessToken.getToken(), this);
		this.wsClient.init();
		
		this.sendOperationsMessage("I'm alive! :rocket:");
		
		this.hasInitialized = true;
	}
	
	public boolean hasInitialized() {
		return this.hasInitialized;
	}
	
	private MattermostClient createMattermostClient(String username, String password) {
		String protocol = secure ? "https" : "http";

		MattermostClient client = MattermostClient.builder().url(protocol+"://" + host).logLevel(Level.INFO).ignoreUnknownProperties()
				.build();
		client.login(username, password);
		
		return client;
	}
	
	private Team getTeam() {
		return adminClient.getTeamByName("workshop").readEntity();
	}
	
	private User createOrGetBotUser() {
		User user = adminClient.getUserByUsername(this.botName).readEntity();
		
		if(user.getUsername() == null) {
			User newUser = new User();
			newUser.setBot(true);
			newUser.setBotDescription("Workshop Bot");
			newUser.setUsername(this.botName);
			newUser.setPassword(BOT_PASSWORD);
			newUser.setEmail(this.botName+"@localhost");
			newUser.setRoles("system_user");
			
			ApiResponse<User> createResponse = adminClient.createUser(newUser);
			
			user = createResponse.readEntity();
			
			if(user.getUsername() == null) {
				throw new RuntimeException("Failed to create user");
			}
		}
		
		String mappedTeamId = adminClient.getTeamMember(this.team.getId(), user.getId()).readEntity().getTeamId();
		
		if(mappedTeamId == null) {
			adminClient.addTeamMembers(this.team.getId(), user.getId());
		}
		
		return user;
	}
	
	private User getAdminUser() {
		User user = adminClient.getUserByUsername(this.operatorName).readEntity();
		
		if(user.getUsername() == null) {
			throw new RuntimeException("Failed to find operator user");
		}
		
		return user;
	}
	
	private UserAccessToken createBotAccessToken() {
		UserAccessToken token = adminClient.createUserAccessToken(this.botUser.getId(), "Generated token").readEntity();
		
		if(token.getDescription() == null) {
			throw new RuntimeException("Failed to create access token");
		}
		
		return token;
	}
	
	private void createOAuthAppIfNeeded() {
		List<OAuthApp> apps = adminClient.getOAuthApps().readEntity();
		
		for(Object app : apps) {
			Map<String, Object> cast = (Map<String, Object>)app;
			
			if(cast.get("name").toString().equals(OAUTH_APP_NAME)) {
				log.info("Found oauth app "+cast.get("name"));
				return;
			}
		}
		
		log.info("Creating ouath app "+OAUTH_APP_NAME);
		
		OAuthApp app = new OAuthApp();
		app.setName(OAUTH_APP_NAME);
		app.setDescription(OAUTH_APP_NAME);
		app.setHomepage(this.oauthCallbackUrl);
		app.setCallbackUrls(Arrays.asList(this.oauthCallbackUrl));
		app.setTrusted(true);
		
		adminClient.createOAuthApp(app);
	}
	
	private Channel createOrGetOperationsChannel() {
		Channel channel = this.adminClient.getChannelByName(OPS_CHANNEL_NAME, this.team.getId()).readEntity();
		
		if(channel.getName() == null) {
			Channel newChannel = new Channel();
			newChannel.setTeamId(this.team.getId());
			newChannel.setName(OPS_CHANNEL_NAME);
			newChannel.setDisplayName(OPS_CHANNEL_NAME);
			newChannel.setPurpose("Operating workshop bot");
			newChannel.setHeader(newChannel.getPurpose());
			newChannel.setType(ChannelType.Private);
			
			channel = this.adminClient.createChannel(newChannel).readEntity();
		}
		
        ChannelMember member = adminClient.getChannelMember(channel.getId(), this.botUser.getId()).readEntity();
		
		if(member.getUserId() == null) {
			this.adminClient.addChannelMember(channel.getId(), this.botUser.getId());
		}
		
		member = adminClient.getChannelMember(channel.getId(), this.operatorUser.getId()).readEntity();
		
		if(member.getUserId() == null) {
			this.adminClient.addChannelMember(channel.getId(), this.operatorUser.getId());
		}
		

		return channel;
	}

	@PreDestroy
	public void onDestroy() throws Exception {
		this.sendOperationsMessage("I'm shutting down now :sob:");
		
		this.wsClient.destroy();
		this.botClient.close();
	}

	@Override
	public void onEvent(MattermostEvent event) {
		log.info("Received event " + event.toString());

		if (event.getPost().isPresent()) {
			String sender = (String) event.getData().get("sender_name");

			if (sender.equals(this.botUser.getUsername())) {
				log.debug("Discarded self message");
				return;
			}
			
			BotCommandProcessor processor = this.privateProcessor;
			
			if(!event.getData().get("channel_type").toString().equals("D") 
					&& event.getPost().get().getChannelId().equals(this.operationsChannel.getId())) {
				processor = this.opsProcessor;
			}

			try {
				processor.execute(event);
			} catch (Exception uae) {
				log.error("Failed to execute command from MM: " + uae.getMessage());
			}
		}
		else if(event.getEvent().equals("new_user")) {
			String userId = event.getData().get("user_id").toString();
			
			ApiResponse<User> user = botClient.getUser(userId);
			
			String username = user.readEntity().getUsername();
			
			welcome(username);
			
			sendOperationsMessage("Auto-provisioning environment for @"+username);
			
			try {
				environmentService.request(username);
			} catch (Exception e) {
				log.error("Failed to provision user environment");
				
				sendOperationsMessage("Failed to auto-provision environment for @"+username, e);
			}
		}
	}
	
	public void sendOperationsMessage(String message) {
		Post post = new Post();
		post.setChannelId(this.operationsChannel.getId());
		post.setMessage(message);
		
		botClient.createPost(post);
	}
	
	public void sendOperationsMessage(String message, Exception e) {
		String stackTrace = ExceptionUtils.getStackTrace(e);
		
		sendOperationsMessage(message+": \n\n```"+stackTrace+"```");
	}

	public void sendMessage(String message, String channelId) {
		Post post = new Post();
		post.setChannelId(channelId);
		post.setMessage(message);

		botClient.createPost(post);
	}
	
	public void sendMessageByChannelName(String message, String channelName) {
		Channel channel = botClient.getChannelByName(channelName, this.operationsChannel.getTeamId()).readEntity();
		
		if(channel.getName() != null) {
			this.sendMessage(message, channel.getId());
		}
	}

	public void sendMessageByUsername(String message, String username) {
		ApiResponse<User> user = botClient.getUserByUsername(username);
		
		ApiResponse<Channel> channel = botClient.createDirectChannel(this.botUser.getId(), user.readEntity().getId());
		
		sendMessage(message, channel.readEntity().getId());
	}
	
	private void welcome(String username) {
		final ST yamlTemplate = stGroup.getInstanceOf("welcomeTemplate");

		yamlTemplate.add("botName", this.botUser.getUsername());
		yamlTemplate.add("workshopName", "Sample");
		yamlTemplate.add("username", username);

		String message = yamlTemplate.render();
		
		sendMessageByUsername(message, username);
	}

	public String getTeamInviteUrl() {
		if(!this.hasInitialized) {
			throw new RuntimeException("Bot not initialized");
		}

		return "https://"+MattermostBot.this.host+"/signup_user_complete/?id="+MattermostBot.this.team.getInviteId();
	}
}
