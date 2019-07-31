package com.devcodes.workshopkit.environment.watcher;

public interface IEnvironmentWatchListener {
	public void complete(String id);
	
	public void failed(String id, String message);
}
