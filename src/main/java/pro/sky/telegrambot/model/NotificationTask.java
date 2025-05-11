package pro.sky.telegrambot.model;

import javax.persistence.Id;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_task")
public class NotificationTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(name = "message_text", nullable = false)
    private String messageText;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    public NotificationTask() {
    }

    public NotificationTask(Long chatId, String messageText, LocalDateTime scheduledTime) {
        this.chatId = chatId;
        this.messageText = messageText;
        this.scheduledTime = scheduledTime;
    }

    public Long getId() {
        return id;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getMessageText() {
        return messageText;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    @Override
    public String toString() {
        return "NotificationTask{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", messageText='" + messageText + '\'' +
                ", scheduledTime=" + scheduledTime +
                '}';
    }
}
