package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.RepositoryNotificationTask;

import java.time.LocalDateTime;

@Service
public class ScheduledNotificationService {

    private final Logger logger = LoggerFactory.getLogger(ScheduledNotificationService.class);

    private final RepositoryNotificationTask repositoryNotificationTask;

    private final TelegramBot telegramBot;

    public ScheduledNotificationService(TelegramBot telegramBot,
                                        RepositoryNotificationTask repositoryNotificationTask) {
        this.telegramBot = telegramBot;
        this.repositoryNotificationTask = repositoryNotificationTask;
    }

    @Scheduled(cron = "0 * * * * *")
    public void sendScheduledTasks() {
        LocalDateTime now = LocalDateTime.now();
        repositoryNotificationTask.findByScheduledTimeBetween(
                now.minusSeconds(30),
                now.plusSeconds(30)
        ).forEach(this::sendAndDeleteTask);
    }

    void sendAndDeleteTask(NotificationTask task) {
        try {

            SendMessage message = new SendMessage(task.getChatId(), "Напоминание - " +
                    task.getMessageText());
            telegramBot.execute(message);
            repositoryNotificationTask.delete(task);
            logger.info("Sent notification for task: {}", task.getId());
        } catch (Exception e) {
            logger.error("Failed to send notification for task {}", task.getId(), e);
        }
    }
}
