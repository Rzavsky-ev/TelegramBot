package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.service.MessageService;


import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;


@ExtendWith(MockitoExtension.class)
public class TelegramBotUpdatesListenerTest {

    private static final long TEST_CHAT_ID = 123L;

    @Mock
    private MessageService messageServiceMock;

    @InjectMocks
    private TelegramBotUpdatesListener telegramBotUpdatesListenerTest;

    @Test
    void processStartCommandTest() {
        String text = "/start";
        Update update = createTestUpdate(text);

        List<Update> updates = List.of(update);

        int result = telegramBotUpdatesListenerTest.process(updates);

        verify(messageServiceMock).sendMessageStart(TEST_CHAT_ID);
        verifyNoMoreInteractions(messageServiceMock);
        assertEquals(UpdatesListener.CONFIRMED_UPDATES_ALL, result);
    }

    @Test
    void processMessageStartTest() {
        String text = "Start";

        Update update = createTestUpdate(text);

        List<Update> updates = List.of(update);

        int result = telegramBotUpdatesListenerTest.process(updates);

        verify(messageServiceMock).answerMessageStart(TEST_CHAT_ID);
        verifyNoMoreInteractions(messageServiceMock);
        assertEquals(UpdatesListener.CONFIRMED_UPDATES_ALL, result);
    }

    @Test
    void processMessageTaskWithDateTest() {
        String testText = "01.01.2028 12:00 Тест";

        Update update = createTestUpdate(testText);
        List<Update> updates = List.of(update);

        int result = telegramBotUpdatesListenerTest.process(updates);

        verify(messageServiceMock).sendMassageTask(
                argThat(m -> {
                    boolean matches = m.matches();
                    assertTrue(matches, "Pattern should match");
                    assertEquals("01.01.2028 12:00", m.group(1));
                    assertEquals("Тест", m.group(3));
                    return true;
                }),
                eq(TEST_CHAT_ID)
        );
        verifyNoMoreInteractions(messageServiceMock);
        assertEquals(UpdatesListener.CONFIRMED_UPDATES_ALL, result);
    }

    @Test
    void processMessageAnotherTextTest() {
        String text = "Test";

        Update update = createTestUpdate(text);

        List<Update> updates = List.of(update);

        int result = telegramBotUpdatesListenerTest.process(updates);

        verify(messageServiceMock).replyToWrongMessage(TEST_CHAT_ID);
        verifyNoMoreInteractions(messageServiceMock);
        assertEquals(UpdatesListener.CONFIRMED_UPDATES_ALL, result);
    }

    @Test
    void processMessageNullTextTest() {
        String text = null;

        Update update = createTestUpdate(text);

        List<Update> updates = List.of(update);

        int result = telegramBotUpdatesListenerTest.process(updates);

        verifyNoMoreInteractions(messageServiceMock);
        assertEquals(UpdatesListener.CONFIRMED_UPDATES_ALL, result);
    }


    private static Update createTestUpdate(String text) {
        Message message = new Message();
        setField(message, "text", text);

        Chat chat = new Chat();
        setField(chat, "id", TEST_CHAT_ID);
        setField(message, "chat", chat);

        Update update = new Update();
        setField(update, "update_id", 1);
        setField(update, "message", message);
        return update;
    }
}

