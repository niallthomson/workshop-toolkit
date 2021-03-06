package com.devcodes.workshopkit.environment;

import com.devcodes.workshopkit.environment.watcher.IEnvironmentWatchListener;
import com.devcodes.workshopkit.environment.watcher.IEnvironmentWatcherService;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
public class EnvironmentService implements IEnvironmentWatchListener {

	private KubernetesClient client;
	
    private ApplicationEventPublisher applicationEventPublisher;
	
	private IEnvironmentWatcherService environmentWatcher;

	private String dnsSuffix;

	private String gitRepo;

	private String kubernetesNamespace;

	private String serverImage;

	private String lifecycleImage;

	private final STGroup stGroup;

	public EnvironmentService(KubernetesClient client, ApplicationEventPublisher applicationEventPublisher, IEnvironmentWatcherService environmentWatcher, String dnsSuffix, String gitRepo, String kubernetesNamespace, String serverImage, String lifecycleImage) {
		this.client = client;
		this.applicationEventPublisher = applicationEventPublisher;
		this.environmentWatcher = environmentWatcher;
		this.dnsSuffix = dnsSuffix;
		this.gitRepo = gitRepo;
		this.kubernetesNamespace = kubernetesNamespace;
		this.serverImage = serverImage;
		this.lifecycleImage = lifecycleImage;
		
		this.environmentWatcher.addListener(this);
		
		this.stGroup = new STGroupFile("yml/workspaces.stg");
	}
	
	public List<EnvironmentDetails> list() {
		List<EnvironmentDetails> environments = new ArrayList<EnvironmentDetails>();
		List<Namespace> namespaces = client.namespaces().withLabel("app", "coder").list().getItems();
		
		for(Namespace namespace : namespaces) {
			environments.add(details(namespace));
		}
		
		return environments;
	}

	private EnvironmentDetails details(Namespace namespace) {
		Map<String, String> labels = namespace.getMetadata().getLabels();

		return new EnvironmentDetails(labels.get("id"), labels.get("fqdn"), labels.get("user"), labels.get("status"));
	}

	public EnvironmentDetails request(String username) {
		EnvironmentDetails details = retrieve(username);

		if (details != null) {
			return details;
		}

		String id = UUID.randomUUID().toString();
		String fqdn = fqdn(username);

		Namespace namespace = client.namespaces().createNew()
                .withNewMetadata()
                  .withName(id)
                  .addToLabels("id", id)
                  .addToLabels("user", username)
                  .addToLabels("fqdn", fqdn)
                  .addToLabels("app", "coder")
				  .addToLabels("status", "Creating")
                .endMetadata()
                .done();

		try {
			this.copySecret("workspaces", "wildcard-tls-secret", id);
			this.copySecret(this.kubernetesNamespace, "coder-secrets", id);

			String ymlTemplate = createTemplate(id, username, fqdn, gitRepo);

			log.info(ymlTemplate);

			client.load(new ByteArrayInputStream(ymlTemplate.getBytes())).inNamespace(id).createOrReplace();

			waitForEnvironment(id, fqdn);

			return details(namespace);
		}
		catch(Exception e) {
			this.updateNamespaceStatus(id, "CreateFailed");

			throw e;
		}
	}

	private void copySecret(String sourceNamespace, String name, String targetNamespace) {
		Secret secret = client.secrets().inNamespace(sourceNamespace).withName(name).get();

		if(secret == null) {
			log.error("Failed to find secret {} in namespace {}", name, sourceNamespace);

			throw new RuntimeException("Failed to find secret "+name);
		}

		client.secrets().inNamespace(targetNamespace).createNew()
				.withNewMetadata()
				.withName(secret.getMetadata().getName())
				.withNamespace(targetNamespace)
				.endMetadata()
				.withData(secret.getData())
				.done();
	}

	public EnvironmentDetails retrieve(String username) {
		List<Namespace> namespaces = client.namespaces().withLabel("user", username).list().getItems();
		
		if(namespaces.size() == 0) {
			return null;
		}
		
		Namespace namespace = namespaces.get(0);

		if (namespace != null) {
			return details(namespace);
		}

		return null;
	}
	
	public EnvironmentDetails retrieveById(String id) {
		Namespace namespace = client.namespaces().withName(id).get();

		if (namespace != null) {
			return details(namespace);
		}

		return null;
	}

	public boolean delete(String username) throws EnvironmentException {
		try {
			EnvironmentDetails details = retrieve(username);
			
			if(details == null) {
				log.info("Skipping deleting environment for "+username);

				return false;
			}
			
			Namespace namespace = client.namespaces().withName(details.getId()).get();
			
			if(namespace == null) {
				String err = "Namespace not found to be deleted for "+username;
				log.error(err);
				
				throw new Exception(err);
			}
			
			// Relabel namespace to immediately deallocate from user
			Map<String, String> labels = namespace.getMetadata().getLabels();
			labels.put("user", "deallocated");
			labels.put("status", "Deleting");
			labels.put("fqdn", "none");
			
			client.namespaces().createOrReplace(namespace);
			
			String namespaceName = namespace.getMetadata().getName();
	
			client.apps().deployments().inNamespace(namespaceName).withName("coder-deployment").cascading(true).delete();
			
			log.info("Deleted deployment for {}", namespaceName);
			
			client.load(new ByteArrayInputStream(destroyTemplate(details.getId(), username).getBytes())).createOrReplace();
			
			log.info("Submitted destroy job for {}", namespaceName);

	        final CountDownLatch watchLatch = new CountDownLatch(1);
	        try (final Watch ignored = client.pods().inNamespace(namespaceName).withLabel("job-name").watch(new Watcher<Pod>() {
	            @Override
	            public void eventReceived(final Action action, Pod pod) {
	                if (pod.getStatus().getPhase().equals("Succeeded")) {
	                	log.info("Destroy job completed for {}", namespaceName);
	                    watchLatch.countDown();
	                }
	            }

	            @Override
	            public void onClose(final KubernetesClientException e) {
	            	log.info("Job close completed for {}", namespaceName);
	            }
	        })) {
	            watchLatch.await(20, TimeUnit.MINUTES);

                log.info("Deleting namespace {}", namespaceName);

                client.namespaces().withName(namespaceName).cascading(true).delete();
	        } catch (final KubernetesClientException | InterruptedException e) {
	            try {
                    client.batch().jobs().inNamespace(namespaceName).withName("coder-destroy").cascading(true).delete();
                }
                catch(Exception ee) {
	                log.error("Error cleaning up failed destroy", ee);
                }

	        	throw e;
	        }
		}
		catch(Exception e) {
			throw new EnvironmentException("Failed to delete environment", e);
		}
		
		return true;
	}
	
	public EnvironmentDetails reset(String username) throws EnvironmentException {
		this.delete(username);
		
		return this.request(username);
	}

	private String createTemplate(String id, String username, String fqdn, String gitRepo) {
		// Pick the correct template
		final ST template = stGroup.getInstanceOf("createTemplate");

		// Pass on values to use when rendering
		template.add("username", username);
		template.add("id", id);
		template.add("fqdn", fqdn);
		template.add("repo", gitRepo);
		template.add("oauth", "oauth."+this.dnsSuffix);
		template.add("serverImage", this.serverImage);
		template.add("lifecycleImage", this.lifecycleImage);

		return template.render();
	}
	
	private String destroyTemplate(String id, String username) {
		// Pick the correct template
		final ST template = stGroup.getInstanceOf("destroyTemplate");

		// Pass on values to use when rendering
		template.add("username", username);
		template.add("id", id);

		return template.render();
	}

	private String fqdn(String username) {
		return username + "-coder." + this.dnsSuffix;
	}
	
	public void waitForEnvironment(String id, String fqdn) {
		log.info("Adding watch on {} at {}", id, fqdn);
		
		this.environmentWatcher.watch(id, fqdn);
	}

	@Override
	public void complete(String id) {
		EnvironmentDetails details = retrieveById(id);
		
		if(details != null) {
			applicationEventPublisher.publishEvent(new EnvironmentCreatedEvent(details.getUsername(), details, this));

			this.updateNamespaceStatus(id, "Active");
		}
		
		log.warn("Received completion event for non-existent environment {}", id);
	}

	@Override
	public void failed(String id, String message) {
		EnvironmentDetails details = retrieveById(id);

		if(details != null) {
			applicationEventPublisher.publishEvent(new EnvironmentCreateFailedEvent(details.getUsername(), details, message, this));

			this.updateNamespaceStatus(id, "CreateFailed");
		}

		log.warn("Received failure event for non-existent environment {}", id);
	}

	private void updateNamespaceStatus(String id, String status) {
		Namespace namespace = client.namespaces().withName(id).get();

		if(namespace == null) {
			log.error("Failed to find namespace on creation complete for {}", id);

			return;
		}

		// Update namespace status
		Map<String, String> labels = namespace.getMetadata().getLabels();
		labels.put("status", status);
		client.namespaces().createOrReplace(namespace);
	}
}

