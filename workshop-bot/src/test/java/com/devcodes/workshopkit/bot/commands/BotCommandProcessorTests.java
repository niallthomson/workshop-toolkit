package com.devcodes.workshopkit.bot.commands;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.devcodes.workshopkit.bot.IMattermostEvent;
import com.devcodes.workshopkit.bot.MattermostBot;
import com.devcodes.workshopkit.bot.MattermostPost;

public class BotCommandProcessorTests {
	
	BotCommandProcessor processor;
	
	@Mock
	MattermostBot bot;
	
	@Mock
	IBotCommand sampleCommand;
	
	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		
		this.processor = new BotCommandProcessor(bot);
	}
	
	@Test
	public void testSampleCommand() {
		when(sampleCommand.keyword()).thenReturn("sample");
		
		processor.register(sampleCommand);
		
		MattermostPost post = new MattermostPost();
		post.setMessage("sample");
		
		IMattermostEvent event = Mockito.mock(IMattermostEvent.class);
		when(event.getPost()).thenReturn(Optional.of(post));
		
		processor.execute(event);
		
		verify(sampleCommand).execute(eq(event), eq(bot), any(String[].class));
	}
	
	@Test
	public void testSampleCommandWithArg() {
		when(sampleCommand.keyword()).thenReturn("sample");
		
		processor.register(sampleCommand);
		
		MattermostPost post = new MattermostPost();
		post.setMessage("sample arg1");
		
		IMattermostEvent event = Mockito.mock(IMattermostEvent.class);
		when(event.getPost()).thenReturn(Optional.of(post));
		
		processor.execute(event);
		
		verify(sampleCommand).execute(eq(event), eq(bot), eq(new String[] {"arg1"}));
	}
	
	@Test
	public void testMissingCommand() {
		when(sampleCommand.keyword()).thenReturn("sample");
		
		processor.register(sampleCommand);
		
		MattermostPost post = new MattermostPost();
		post.setMessage("sample1");
		
		IMattermostEvent event = Mockito.mock(IMattermostEvent.class);
		when(event.getPost()).thenReturn(Optional.of(post));
		
		processor.execute(event);
		
		verify(sampleCommand, never()).execute(eq(event), eq(bot), any(String[].class));
	}
}
