package com.devcodes.workshopkit.bot;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MattermostEventBroadcast {
	
	private Map<String, Boolean> omitUsers;
	
	private String userId;
	
	private String channelId;
	
	private String teamId;
}
