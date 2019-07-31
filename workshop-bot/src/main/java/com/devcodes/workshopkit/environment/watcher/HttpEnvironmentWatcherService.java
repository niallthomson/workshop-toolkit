package com.devcodes.workshopkit.environment.watcher;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

@Slf4j
public class HttpEnvironmentWatcherService implements IEnvironmentWatcherService {
	
	private IEnvironmentWatcherRepository repository;
	
	private HttpClient httpClient;
	
	private Supplier<Date> dateSupplier;

	private List<IEnvironmentWatchListener> listeners;

	private int timeout;

	private static final int TIMEOUT_DEFAULT = 240;

	@Builder
	static HttpEnvironmentWatcherService builderFactory(IEnvironmentWatcherRepository repository, HttpClient httpClient, Supplier<Date> dateSupplier, int timeout) {
		
		if(dateSupplier == null) {
			dateSupplier = new DateSupplier();
		}
		
		if(httpClient == null) {
			int httpTimeout = 5;
			RequestConfig config = RequestConfig.custom()
			  .setConnectTimeout(httpTimeout * 1000)
			  .setConnectionRequestTimeout(httpTimeout * 1000)
			  .setSocketTimeout(httpTimeout * 1000).build();
			httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
		}

		if(timeout == 0) {
			timeout = TIMEOUT_DEFAULT;
		}
		
		HttpEnvironmentWatcherService watcher = new HttpEnvironmentWatcherService();
		watcher.httpClient = httpClient;
		watcher.repository = repository;
		watcher.dateSupplier = dateSupplier;
		watcher.listeners = new ArrayList<>();
		watcher.timeout = timeout;
		
		return watcher;
	}

	@Override
	public void watch(String id, String url) {
		this.repository.add(new EnvironmentWatch(id, url, dateSupplier.get()));
	}

    public void tick() {
		log.debug("Tick");

		List<EnvironmentWatch> complete = new ArrayList<EnvironmentWatch>();
		
		Date now = dateSupplier.get();
		
        for(EnvironmentWatch watch : this.repository.list()) {
			String url = "https://"+watch.getFqdn()+"/ping";

			log.debug("Watching {}", url);
		
			try {
	    		HttpResponse response = httpClient.execute(new HttpGet(url));
			    int statusCode = response.getStatusLine().getStatusCode();
			    EntityUtils.consumeQuietly(response.getEntity());
			    
			    if(statusCode == HttpStatus.OK.value()) {
		    		log.debug("Environment passed health check: "+watch.getId());
		    		complete.add(watch);
		    		
		    		this.complete(watch);
			    }
			    else {
		    		log.debug("Environment failed health check: "+watch.getId()+" - "+statusCode);
		    		
		    		if(now.getTime() - watch.getSeeded().getTime() > this.timeout * 1000) {
		    			log.error("Environment took too long to become alive");
		    			
		    			complete.add(watch);
		    			
		    			this.fail(watch);
		    		}
			    }
			}
			catch(Exception e) {
				e.printStackTrace();
			}
        }
        
        this.repository.remove(complete);
    }

	@Override
	public void cancel(String id) {
		this.repository.remove(id);
	}

	private void complete(EnvironmentWatch watch) {
		for(IEnvironmentWatchListener listener : listeners) {
			listener.complete(watch.getId());
		}
	}
	
	private void fail(EnvironmentWatch watch) {
		for(IEnvironmentWatchListener listener : listeners) {
			listener.failed(watch.getId(), "Environment not healthy after threshold time");
		}
	}

	@Override
	public void addListener(IEnvironmentWatchListener listener) {
		this.listeners.add(listener);
	}

	static class DateSupplier implements Supplier<Date> {
		@Override
		public Date get() {
			return new Date();
		}
	}
}
