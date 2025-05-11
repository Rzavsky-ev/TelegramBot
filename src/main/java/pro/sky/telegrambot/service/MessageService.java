package pro.sky.telegrambot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.RepositoryNotificationTask;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;


@Service
public class MessageService {

    private final TelegramBot telegramBot;

    private final RepositoryNotificationTask repositoryNotificationTask;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final Logger logger = LoggerFactory.getLogger(MessageService.class);

    public MessageService(TelegramBot telegramBot, RepositoryNotificationTask repositoryNotificationTask) {
        this.telegramBot = telegramBot;
        this.repositoryNotificationTask = repositoryNotificationTask;
    }

    public void sendMassageTask(Matcher matcher, long chatId) {
        logger.info("Processing message from chat {}: {}", chatId, matcher.group(0));

        try {
            LocalDateTime scheduledTime =
                    LocalDateTime.parse(matcher.group(1), DATE_FORMATTER);

            if (scheduledTime.isBefore(LocalDateTime.now())) {
                telegramBot.execute(new SendMessage(chatId, "Время должно быть в будущем"));
                return;
            }
            String messageText = matcher.group(3);
            if (repositoryNotificationTask.existsByChatIdAndMessageTextAndScheduledTime(chatId,
                    messageText, scheduledTime)) {
                telegramBot.execute(new SendMessage(chatId, "Напоминание уже существует."));
                return;
            }
            NotificationTask task = new NotificationTask(chatId, messageText, scheduledTime);
            repositoryNotificationTask.save(task);
            telegramBot.execute(new SendMessage(chatId, "Сохранено напоминание: " + messageText));
        } catch (DateTimeParseException e) {
            logger.error("Date parse error for: {}", matcher.group(1), e);
            telegramBot.execute(new SendMessage(chatId, "Некорректное время или дата."));
        }
    }

    public void answerMessageStart(long chatId) {
        SendMessage request = new SendMessage(chatId, "Привет! Я бот для напоминаний. 🚀\n" +
                "Отправьте задачу в формате: DD.MM.YYYY HH:MM Текст напоминания");
        SendResponse response = telegramBot.execute(request);
        if (response.isOk()) {
            logger.info("Sent welcome message to chat {}", chatId);
        } else {
            logger.error("Failed to send message: {}", response.description());
        }
    }

    public void sendMessageStart(long chatId) {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup(new String[]{"Start"})
                .resizeKeyboard(true)
                .oneTimeKeyboard(true);

        SendMessage welcomeMessage = new SendMessage(chatId,
                "Нажми кнопку **Start**, чтобы начать!")
                .replyMarkup(keyboard);

        telegramBot.execute(welcomeMessage);
    }

    public void replyToWrongMessage(long chatId) {
        logger.info("Incorrect reminder format {}", chatId);
        SendMessage message = new SendMessage(chatId,
                "Неправильный формат напоминания");
        telegramBot.execute(message);
    }
}
