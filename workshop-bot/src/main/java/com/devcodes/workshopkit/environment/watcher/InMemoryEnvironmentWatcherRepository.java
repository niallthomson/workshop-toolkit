package com.devcodes.workshopkit.environment.watcher;

import io.fabric8.zjsonpatch.internal.guava.Lists;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryEnvironmentWatcherRepository implements IEnvironmentWatcherRepository {
	
	private Map<String, EnvironmentWatch> watches;
	
	public InMemoryEnvironmentWatcherRepository() {
		watches = new HashMap<>();
	}

	@Override
	public EnvironmentWatch add(EnvironmentWatch watch) {
		watches.put(watch.getId(), watch);
		
		return watch;
	}

	@Override
	public Collection<EnvironmentWatch> list() {
		return Lists.newArrayList(watches.values());
	}

	@Override
	public void remove(EnvironmentWatch watch) {
		watches.remove(watch.getId());
	}

	@Override
	public void remove(String id) {
		if(this.watches.containsKey(id)) {
			watches.remove(id);
		}
	}

	@Override
	public void remove(List<EnvironmentWatch> watches) {
		for(EnvironmentWatch watch : watches) {
			this.watches.remove(watch.getId());
		}
	}

}
