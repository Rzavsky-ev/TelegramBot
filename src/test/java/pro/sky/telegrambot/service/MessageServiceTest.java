package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.RepositoryNotificationTask;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {

    private static final long TEST_CHAT_ID = 123L;

    private static final Pattern TASK_PATTERN = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");

    @Mock
    private TelegramBot telegramBotMock;

    @Mock
    private RepositoryNotificationTask repositoryNotificationTaskMock;

    @InjectMocks
    private MessageService messageServiceTest;

    @Test
    public void answerMessageStartTest() {

        SendResponse sendResponseMock = mock(SendResponse.class);

        when(sendResponseMock.isOk()).thenReturn(true);
        when(telegramBotMock.execute(any(SendMessage.class))).thenReturn(sendResponseMock);

        messageServiceTest.answerMessageStart(TEST_CHAT_ID);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBotMock).execute(messageCaptor.capture());

        SendMessage sendMessage = messageCaptor.getValue();
        assertEquals(TEST_CHAT_ID, sendMessage.getParameters().get("chat_id"));
        assertEquals("Привет! Я бот для напоминаний. 🚀\n" +
                        "Отправьте задачу в формате: DD.MM.YYYY HH:MM Текст напоминания",
                sendMessage.getParameters().get("text"));
    }

    @Test
    public void sendMessageCommandStartTest() {

        messageServiceTest.sendMessageStart(TEST_CHAT_ID);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBotMock).execute(messageCaptor.capture());

        SendMessage sendMessage = messageCaptor.getValue();
        assertEquals(TEST_CHAT_ID, sendMessage.getParameters().get("chat_id"));
        assertEquals("Нажми кнопку **Start**, чтобы начать!",
                sendMessage.getParameters().get("text"));
    }

    @Test
    public void replyToWrongMessageTest() {

        messageServiceTest.replyToWrongMessage(TEST_CHAT_ID);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBotMock).execute(messageCaptor.capture());

        SendMessage sendMessage = messageCaptor.getValue();
        assertEquals(TEST_CHAT_ID, sendMessage.getParameters().get("chat_id"));
        assertEquals("Неправильный формат напоминания",
                sendMessage.getParameters().get("text"));
    }

    @Test
    void taskMassageFutureTimeTest() {

        LocalDateTime testTime = LocalDateTime.of(2029, 1, 1, 12, 0);
        String testText = "01.01.2029 12:00 Тест";
        String expectedMessageText = "Тест";

        Matcher matcher = TASK_PATTERN.matcher(testText);
        assertTrue(matcher.find());

        when(repositoryNotificationTaskMock.existsByChatIdAndMessageTextAndScheduledTime(
                eq(TEST_CHAT_ID),
                eq(expectedMessageText),
                eq(testTime)))
                .thenReturn(false);

        messageServiceTest.sendMassageTask(matcher, TEST_CHAT_ID);

        NotificationTask expectedTask = new NotificationTask(TEST_CHAT_ID, expectedMessageText, testTime);

        verify(repositoryNotificationTaskMock).save(argThat(task ->
                task.getChatId().equals(expectedTask.getChatId()) &&
                        task.getMessageText().equals(expectedTask.getMessageText()) &&
                        task.getScheduledTime().equals(expectedTask.getScheduledTime())
        ));
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBotMock).execute(messageCaptor.capture());

        SendMessage sendMessage = messageCaptor.getValue();
        assertEquals(TEST_CHAT_ID, sendMessage.getParameters().get("chat_id"));
        assertEquals("Сохранено напоминание: Тест", sendMessage.getParameters().get("text"));
    }

    @Test
    void taskMassagePastTimeTest() {

        String testText = "01.01.2020 12:00 Тест";

        Matcher matcher = TASK_PATTERN.matcher(testText);
        assertTrue(matcher.find());

        messageServiceTest.sendMassageTask(matcher, TEST_CHAT_ID);

        verify(repositoryNotificationTaskMock, never()).save(any());

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBotMock).execute(messageCaptor.capture());

        SendMessage sendMessage = messageCaptor.getValue();
        assertEquals(TEST_CHAT_ID, sendMessage.getParameters().get("chat_id"));
        assertEquals("Время должно быть в будущем", sendMessage.getParameters().get("text"));
    }

    @Test
    void taskMassageReminderExistsTest() {

        String testText = "01.01.2029 12:00 Тест";

        Matcher matcher = TASK_PATTERN.matcher(testText);
        assertTrue(matcher.find());
        assertEquals("Тест", matcher.group(3));
        when(repositoryNotificationTaskMock.existsByChatIdAndMessageTextAndScheduledTime(
                eq(TEST_CHAT_ID),
                eq("Тест"),
                any(LocalDateTime.class)
        )).thenReturn(true);

        messageServiceTest.sendMassageTask(matcher, TEST_CHAT_ID);

        verify(repositoryNotificationTaskMock, never()).save(any());
        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBotMock).execute(messageCaptor.capture());

        SendMessage sendMessage = messageCaptor.getValue();
        assertEquals(TEST_CHAT_ID, sendMessage.getParameters().get("chat_id"));
        assertEquals("Напоминание уже существует.", sendMessage.getParameters().get("text"));
    }

    @Test
    void taskMassageWrongDateTest() {

        String testText = "01.30.2029 12:00 Тест";

        Matcher matcher = TASK_PATTERN.matcher(testText);
        assertTrue(matcher.find());

        messageServiceTest.sendMassageTask(matcher, TEST_CHAT_ID);

        verify(repositoryNotificationTaskMock, never()).save(any());

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBotMock).execute(messageCaptor.capture());

        SendMessage sendMessage = messageCaptor.getValue();
        assertEquals(TEST_CHAT_ID, sendMessage.getParameters().get("chat_id"));
        assertEquals("Некорректное время или дата.", sendMessage.getParameters().get("text"));
    }

    @Test
    void taskMassageWrongTimeTest() {

        String testText = "01.01.2029 32:00 Тест";

        Matcher matcher = TASK_PATTERN.matcher(testText);
        assertTrue(matcher.find());

        messageServiceTest.sendMassageTask(matcher, TEST_CHAT_ID);

        verify(repositoryNotificationTaskMock, never()).save(any());

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBotMock).execute(messageCaptor.capture());

        SendMessage sendMessage = messageCaptor.getValue();
        assertEquals(TEST_CHAT_ID, sendMessage.getParameters().get("chat_id"));
        assertEquals("Некорректное время или дата.", sendMessage.getParameters().get("text"));
    }
}
