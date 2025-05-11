package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.RepositoryNotificationTask;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ScheduledNotificationServiceTest {

    @InjectMocks
    private ScheduledNotificationService scheduledNotificationServiceTest;

    @Mock
    private RepositoryNotificationTask repositoryNotificationTaskMock;

    @Mock
    private TelegramBot telegramBotMock;

    @Test
    public void sendScheduledTasksTest() {

        LocalDateTime fixedNow = LocalDateTime.of(2025, 10, 1, 12, 0);
        LocalDateTime startTime = fixedNow.minusSeconds(30);
        LocalDateTime endTime = fixedNow.plusSeconds(30);

        try (MockedStatic<LocalDateTime> mockDateTime = mockStatic(LocalDateTime.class)) {
            mockDateTime.when(LocalDateTime::now).thenReturn(fixedNow);

            NotificationTask task1 = new NotificationTask(123L, "task1", fixedNow);
            NotificationTask task2 = new NotificationTask(456L, "task2", fixedNow);

            when(repositoryNotificationTaskMock.findByScheduledTimeBetween(
                    startTime,
                    endTime
            )).thenReturn(List.of(task1, task2));

            scheduledNotificationServiceTest.sendScheduledTasks();

            verify(repositoryNotificationTaskMock).findByScheduledTimeBetween(
                    startTime,
                    endTime
            );

            verify(repositoryNotificationTaskMock).delete(task1);
            verify(repositoryNotificationTaskMock).delete(task2);

            verifyNoMoreInteractions(repositoryNotificationTaskMock);
        }
    }

    @Test
    public void sendScheduledNoTasksTest() {

        LocalDateTime fixedNow = LocalDateTime.of(2025, 10, 1, 12, 0);
        LocalDateTime startTime = fixedNow.minusSeconds(30);
        LocalDateTime endTime = fixedNow.plusSeconds(30);

        try (MockedStatic<LocalDateTime> mockDateTime = mockStatic(LocalDateTime.class)) {
            mockDateTime.when(LocalDateTime::now).thenReturn(fixedNow);

            when(repositoryNotificationTaskMock.findByScheduledTimeBetween(
                    startTime,
                    endTime
            )).thenReturn(Collections.emptyList());

            scheduledNotificationServiceTest.sendScheduledTasks();

            verify(repositoryNotificationTaskMock, never()).delete(any());

            verifyNoMoreInteractions(repositoryNotificationTaskMock);
        }
    }

    @Test
    public void sendAndDeleteTaskTest() {
        NotificationTask task = new NotificationTask(123L, "task", LocalDateTime.now());
        scheduledNotificationServiceTest.sendAndDeleteTask(task);

        ArgumentCaptor<SendMessage> messageCaptor = ArgumentCaptor.forClass(SendMessage.class);
        verify(telegramBotMock).execute(messageCaptor.capture());

        SendMessage sendMessage = messageCaptor.getValue();

        assertEquals(123L, sendMessage.getParameters().get("chat_id"));
        assertEquals("Напоминание - task", sendMessage.getParameters().get("text"));
        verify(repositoryNotificationTaskMock).delete(task);
        verifyNoMoreInteractions(telegramBotMock, repositoryNotificationTaskMock);
    }

    @Test
    void sendAndDeleteNoTaskTest() {
        LocalDateTime now = LocalDateTime.of(2023, 12, 1, 12, 0);

        try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class)) {
            mockedTime.when(LocalDateTime::now).thenReturn(now);

            when(repositoryNotificationTaskMock.findByScheduledTimeBetween(any(), any()))
                    .thenReturn(Collections.emptyList());

            scheduledNotificationServiceTest.sendScheduledTasks();

            verify(repositoryNotificationTaskMock).findByScheduledTimeBetween(
                    now.minusSeconds(30),
                    now.plusSeconds(30)
            );
            verify(repositoryNotificationTaskMock, never()).delete(any());

            verifyNoInteractions(telegramBotMock);
            verifyNoMoreInteractions(repositoryNotificationTaskMock);
        }
    }

}
