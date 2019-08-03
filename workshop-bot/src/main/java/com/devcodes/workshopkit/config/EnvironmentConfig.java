package com.devcodes.workshopkit.config;

import com.devcodes.workshopkit.environment.EnvironmentService;
import com.devcodes.workshopkit.environment.watcher.HttpEnvironmentWatcherService;
import com.devcodes.workshopkit.environment.watcher.IEnvironmentWatcherRepository;
import com.devcodes.workshopkit.environment.watcher.IEnvironmentWatcherService;
import com.devcodes.workshopkit.environment.watcher.InMemoryEnvironmentWatcherRepository;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EnvironmentConfig {

	@Value("${workshop.creation.timeout:240}")
	private int timeout;

	@Value("${kubernetes.namespace")
	private String kubernetesNamespace;

	@Bean
	public EnvironmentService environmentService(ApplicationEventPublisher publisher,
			IEnvironmentWatcherService watcher, WorkshopConfig workshopConfig) {
		return new EnvironmentService(new DefaultKubernetesClient(), publisher, watcher, workshopConfig.getDnsSuffix(), workshopConfig.getGitRepo(), this.kubernetesNamespace);
	}
	
	@Bean
	public IEnvironmentWatcherService environmentWatcher(IEnvironmentWatcherRepository repository) {
		return HttpEnvironmentWatcherService.builder().repository(repository).timeout(this.timeout).build();
	}
	
	@Bean
	public IEnvironmentWatcherRepository environmentWatcherRepository() {
		return new InMemoryEnvironmentWatcherRepository();
	}
}
