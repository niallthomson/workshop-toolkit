package com.devcodes.workshopkit.bot;

import java.util.Map;
import java.util.Optional;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MattermostEvent implements IMattermostEvent {
	private String event;
	
	private Map<String, Object> data;

	private MattermostEventBroadcast broadcast;
	
	private long seq;
	
	public Optional<MattermostPost> getPost()
	{
		if (null == data)
			return Optional.empty();

		final Object post = data.get("post");
		if (null == post)
			return Optional.empty();

		if (post instanceof String) {
			return Optional.of(MattermostPost.fromString((String)post));
		}

		return Optional.empty();
	}
}
	