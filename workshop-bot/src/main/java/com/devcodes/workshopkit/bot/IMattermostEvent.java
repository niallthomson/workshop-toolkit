package com.devcodes.workshopkit.bot;

import java.util.Map;
import java.util.Optional;

public interface IMattermostEvent {
	public Optional<MattermostPost> getPost();
	
	public Map<String, Object> getData();
}
