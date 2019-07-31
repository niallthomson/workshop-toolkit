package com.devcodes.workshopkit.environment.watcher;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.devcodes.workshopkit.environment.watcher.EnvironmentWatch;
import com.devcodes.workshopkit.environment.watcher.HttpEnvironmentWatcherService;
import com.devcodes.workshopkit.environment.watcher.IEnvironmentWatchListener;
import com.devcodes.workshopkit.environment.watcher.IEnvironmentWatcherRepository;

public class HttpEnvironmentWatcherServiceTests {
	
	private HttpEnvironmentWatcherService watcher;
	
	@Mock
	HttpClient httpClient;
	
	@Mock
	IEnvironmentWatcherRepository repository;
	
	@Mock
	Supplier<Date> dateSupplier;
	
	@Mock
	IEnvironmentWatchListener listener;
	
	@Before
    public void testInitialize() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        watcher = HttpEnvironmentWatcherService.builder().httpClient(httpClient).repository(repository).dateSupplier(dateSupplier).build();
		watcher.addListener(listener);
	}
	
	@Test
	public void testWatch() {
		watcher.watch("123", "someurl");
		
		ArgumentCaptor<EnvironmentWatch> argument = ArgumentCaptor.forClass(EnvironmentWatch.class);
		verify(repository, times(1)).add(argument.capture());
		
		assertEquals("123", argument.getValue().getId());
	}
	
	@Test
	public void testSuccess() throws ClientProtocolException, IOException {
		get(httpClient, response200());

		when(repository.list()).thenReturn(Lists.list(defaultWatch()));
		when(dateSupplier.get()).thenReturn(new Date());
		
		watcher.tick();
		
		ArgumentCaptor<List<EnvironmentWatch>> argument = ArgumentCaptor.forClass(List.class);
		
		verify(repository).remove(argument.capture());
		assertEquals(1, argument.getValue().size());
		
		verify(listener, times(1)).complete("123");
	}
	
	@Test
	public void testFailed() throws ClientProtocolException, IOException {
		get(httpClient, response502());
		
		when(repository.list()).thenReturn(Lists.list(defaultWatch()));
		
		when(dateSupplier.get()).thenReturn(new Date());
		
		watcher.tick();
		
		ArgumentCaptor<List<EnvironmentWatch>> argument = ArgumentCaptor.forClass(List.class);
		
		verify(repository).remove(argument.capture());
		assertEquals(0, argument.getValue().size());
		
		verify(listener, never()).complete(anyString());
	}
	
	private EnvironmentWatch defaultWatch() {
		return new EnvironmentWatch("123", "someurl", new Date());
	}
	
	private void get(HttpClient client, HttpResponse response) throws ClientProtocolException, IOException {
		when(client.execute(any(HttpGet.class))).thenReturn(response);
	}
	
	private HttpResponse response200() {
		StatusLine statusLine = Mockito.mock(StatusLine.class);
		when(statusLine.getStatusCode()).thenReturn(200);
		
		HttpResponse response = Mockito.mock(HttpResponse.class);
		when(response.getStatusLine()).thenReturn(statusLine);
		
		return response;
	}
	
	private HttpResponse response502() {
		StatusLine statusLine = Mockito.mock(StatusLine.class);
		when(statusLine.getStatusCode()).thenReturn(502);
		
		HttpResponse response = Mockito.mock(HttpResponse.class);
		when(response.getStatusLine()).thenReturn(statusLine);
		
		return response;
	}
}
