package com.devcodes.workshopkit.environment.watcher;

import java.util.Collection;
import java.util.List;

public interface IEnvironmentWatcherRepository {
	public Collection<EnvironmentWatch> list();
	
	public void remove(EnvironmentWatch watch);
	
	public void remove(String id);
	
	public void remove(List<EnvironmentWatch> watches);

	public EnvironmentWatch add(EnvironmentWatch watch);
}
