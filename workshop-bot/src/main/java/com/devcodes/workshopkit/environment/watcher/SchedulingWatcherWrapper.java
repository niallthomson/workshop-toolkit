package com.devcodes.workshopkit.environment.watcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedulingWatcherWrapper {

    @Autowired
    private IEnvironmentWatcherService service;

    @Scheduled(fixedRate = 10000)
    public void tick() {
        service.tick();
    }
}
