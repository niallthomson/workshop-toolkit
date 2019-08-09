package com.devcodes.workshopkit.bot.commands.ops;

import com.devcodes.workshopkit.bot.MattermostBot;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

public class AnnounceCommandTests {
    @Mock
    MattermostBot bot;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void test() {
        String template = "test <message>";

        AnnounceCommand command = new AnnounceCommand(template);

        command.execute(null, bot, new String[] {"my message"});

        verify(bot).sendMessageByChannelName(eq("test my message"), eq(AnnounceCommand.CHANNEL));
    }
}
