package com.devcodes.workshopkit.bot;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.bis5.mattermost.model.Post;

public class MattermostPost extends Post {
	
	private static ObjectMapper mapper;
		
	public MattermostPost() {
		super();
	}

	public MattermostPost(String channelId, String message) {
		super(channelId, message);
	}

	static {
		mapper = new ObjectMapper();
	}
	
	public static MattermostPost fromString(String postString) {
		try {
			return mapper.readValue(postString, MattermostPost.class);
		} catch (IOException e) {
			return null;
		}
	}
}
