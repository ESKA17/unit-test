package com.github.app.rest;

import static org.mockito.Mockito.times;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.github.app.api.MessageApi;
import com.github.domain.model.Message;
import com.github.domain.service.MessageService;
import com.github.domain.util.MessageMatcher;

/*
    Необходимо протестировать MessageController используя моки
    TODO
        создайте мок для MessageService
        создайте мок для MessageController
        протестируйте создание нового сообщения
*/

@RunWith(MockitoJUnitRunner.class)
public class MessageControllerUnitTest {

    @Mock
    private MessageService messageService;

    @InjectMocks
    private MessageController messageController;

    @Test
    public void createMessage_NewMessage_OK() {
        MessageApi messageApi = new MessageApi();
        messageApi.setFrom("Sabit");
        messageApi.setTo("Mentor");
        messageApi.setText("Hello, there!");

        messageController.createMessage(messageApi);

        Message message = new Message();
        message.setFrom("Sabit");
        message.setTo("Mentor");
        message.setText("Hello, there!");

        Mockito.verify(messageService, times(1)).deliverMessage(ArgumentMatchers.argThat(new MessageMatcher(message)));
    }
}