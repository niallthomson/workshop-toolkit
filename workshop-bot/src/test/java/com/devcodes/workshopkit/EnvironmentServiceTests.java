package com.devcodes.workshopkit;

import static org.hamcrest.CoreMatchers.any;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.fabric8.kubernetes.api.model.*;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import com.devcodes.workshopkit.environment.EnvironmentCreateFailedEvent;
import com.devcodes.workshopkit.environment.EnvironmentCreatedEvent;
import com.devcodes.workshopkit.environment.EnvironmentDetails;
import com.devcodes.workshopkit.environment.EnvironmentService;
import com.devcodes.workshopkit.environment.watcher.IEnvironmentWatcherService;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;

public class EnvironmentServiceTests {
	
	@Mock
	KubernetesClient kubernetesClient;
	
	@Mock
	ApplicationEventPublisher applicationEventPublisher;
	
	@Mock
	IEnvironmentWatcherService environmentWatcher;
	
	@Mock
	NonNamespaceOperation< Namespace, NamespaceList, DoneableNamespace, Resource<Namespace, DoneableNamespace>> namespaceApi;

	@Mock
	FilterWatchListDeletable<Namespace, NamespaceList, Boolean, Watch, Watcher<Namespace>> namespaceFilter;

	@Mock
	Resource<Namespace, DoneableNamespace> namespaceResource;

	@Mock
	Resource<Namespace, DoneableNamespace> missingNamespaceResource;

	@Mock
	FilterWatchListDeletable<Namespace, NamespaceList, Boolean, Watch, Watcher<Namespace>> missingNamespaceFilter;
	
	Namespace ns;
	NamespaceList nsList;
	
	private static final String NAMESPACE_ID = "123";
	
	private static final String MISSING_NAMESPACE_ID = "122";
	
	String dnsSuffix = "mydomain.org";
	
	String gitRepo = "https://github.com/org/repo.git";
	
	@Before
    public void testInitialize() throws Exception{
        MockitoAnnotations.initMocks(this);
        
        ns = new NamespaceBuilder()
        	.withNewMetadata()
        		.withName(NAMESPACE_ID)
        		.addToLabels("id", NAMESPACE_ID)
        		.addToLabels("fqdn", "user."+dnsSuffix)
        		.addToLabels("user", "user")
        	.endMetadata()
        .build();

        nsList = new NamespaceList();
        nsList.setItems(Lists.list(ns));

		when(missingNamespaceResource.get()).thenReturn(ns);
		when(namespaceApi.withName(MISSING_NAMESPACE_ID)).thenReturn(missingNamespaceResource);

        when(missingNamespaceFilter.list()).thenReturn(new NamespaceList());
        when(namespaceApi.withLabel("id", MISSING_NAMESPACE_ID)).thenReturn(missingNamespaceFilter);

        when(namespaceResource.get()).thenReturn(ns);
		when(namespaceApi.withName(NAMESPACE_ID)).thenReturn(namespaceResource);

        when(namespaceFilter.list()).thenReturn(nsList);
        when(namespaceApi.withLabel("id", NAMESPACE_ID)).thenReturn(namespaceFilter);
        
        when(kubernetesClient.namespaces()).thenReturn(namespaceApi);
	}
	
	@Test
	public void testRetrieveById() {
		EnvironmentService service = new EnvironmentService(kubernetesClient, applicationEventPublisher, environmentWatcher, dnsSuffix, gitRepo, "workshop");
		
		EnvironmentDetails details = service.retrieveById(NAMESPACE_ID);
		
		assertEquals(NAMESPACE_ID, details.getId());
	}
	
	@Test
	public void testRetrieveByIdMissing() {
		EnvironmentService service = new EnvironmentService(kubernetesClient, applicationEventPublisher, environmentWatcher, dnsSuffix, gitRepo, "workshop");
		
		EnvironmentDetails details = service.retrieveById(MISSING_NAMESPACE_ID);
		
		assertNull(details);
	}

	@Test
	public void testEnvironmentCreated() {
		EnvironmentService service = new EnvironmentService(kubernetesClient, applicationEventPublisher, environmentWatcher, dnsSuffix, gitRepo, "workshop");
		
		service.complete(NAMESPACE_ID);
		
		ArgumentCaptor<EnvironmentCreatedEvent> argument = ArgumentCaptor.forClass(EnvironmentCreatedEvent.class);
		verify(applicationEventPublisher).publishEvent(argument.capture());
		
		assertEquals(NAMESPACE_ID, argument.getValue().getDetails().getId());
	}
	
	@Test
	public void testMissingEnvironmentCreated() {
		EnvironmentService service = new EnvironmentService(kubernetesClient, applicationEventPublisher, environmentWatcher, dnsSuffix, gitRepo, "workshop");
		
		service.complete(MISSING_NAMESPACE_ID);
		
		verify(applicationEventPublisher, never()).publishEvent(any(ApplicationEvent.class));
	}
	
	@Test
	public void testEnvironmentCreateFailed() {
		EnvironmentService service = new EnvironmentService(kubernetesClient, applicationEventPublisher, environmentWatcher, dnsSuffix, gitRepo, "workshop");
		
		service.failed(NAMESPACE_ID, "error");
		
		ArgumentCaptor<EnvironmentCreateFailedEvent> argument = ArgumentCaptor.forClass(EnvironmentCreateFailedEvent.class);
		verify(applicationEventPublisher).publishEvent(argument.capture());
		
		assertEquals(NAMESPACE_ID, argument.getValue().getDetails().getId());
		assertEquals("error", argument.getValue().getException().getMessage());
	}
	
	@Test
	public void testMissingEnvironmentCreateFailed() {
		EnvironmentService service = new EnvironmentService(kubernetesClient, applicationEventPublisher, environmentWatcher, dnsSuffix, gitRepo, "workshop");
		
		service.failed(MISSING_NAMESPACE_ID, "error");
		
		verify(applicationEventPublisher, never()).publishEvent(any(ApplicationEvent.class));
	}
}
