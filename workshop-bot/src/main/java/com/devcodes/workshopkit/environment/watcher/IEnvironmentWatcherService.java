package com.devcodes.workshopkit.environment.watcher;

public interface IEnvironmentWatcherService {
	public void watch(String id, String fqdn);
	
	public void addListener(IEnvironmentWatchListener listener);

	public void tick();

	public void cancel(String id);
}
