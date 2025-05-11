package pro.sky.telegrambot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pro.sky.telegrambot.model.NotificationTask;

import java.time.LocalDateTime;
import java.util.List;


public interface RepositoryNotificationTask extends JpaRepository<NotificationTask, Long> {
    @Query("SELECT t FROM NotificationTask t WHERE t.scheduledTime BETWEEN :start AND :end")
    List<NotificationTask> findByScheduledTimeBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    boolean existsByChatIdAndMessageTextAndScheduledTime
            (long chatId, String message, LocalDateTime scheduledTime);
}


