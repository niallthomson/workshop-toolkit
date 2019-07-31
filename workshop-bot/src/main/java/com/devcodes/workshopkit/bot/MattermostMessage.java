package com.devcodes.workshopkit.bot;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MattermostMessage {
	
	private long seq;
	
	private String action;
	
	private Map<String, String> data;
	
	public MattermostMessage(long seq, String action) {
		this.seq = seq;
		this.action = action;
	}
}
