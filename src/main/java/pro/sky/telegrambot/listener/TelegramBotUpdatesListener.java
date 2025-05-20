package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.service.MessageService;


import javax.annotation.PostConstruct;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private static final Pattern TASK_PATTERN = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");

    private final Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    private final TelegramBot telegramBot;

    private final MessageService messageService;

    public TelegramBotUpdatesListener(TelegramBot telegramBot,
                                      MessageService messageService) {
        this.telegramBot = telegramBot;
        this.messageService = messageService;
    }

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    public int process(List<Update> updates) {
        updates.forEach(update -> {
            logger.info("Processing update: {}", update);
            if (update.message() != null && update.message().text() != null) {
                String text = update.message().text();
                long chatId = update.message().chat().id();
                Matcher matcher = TASK_PATTERN.matcher(text);
                if (matcher.matches()) {
                    messageService.sendMassageTask(matcher, chatId);
                } else if (text.equals("/start")
                ) {
                    messageService.sendMessageStart(chatId); // Отправляем клавиатуру
                } else if (text.equals("Start")) {
                    messageService.answerMessageStart(chatId);
                } else {
                    messageService.replyToWrongMessage(chatId);
                }
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}
