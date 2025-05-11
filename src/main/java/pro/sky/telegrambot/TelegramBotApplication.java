package pro.sky.telegrambot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TelegramBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelegramBotApplication.class, args);
	}

}
/*
Добавление переменных окружения
Открой Run/Debug Configurations

В верхней панели нажми на выпадающий список рядом с кнопкой запуска → Edit Configurations...
(Или: Alt + Shift + F10 → Edit Configurations).

Выбери свою конфигурацию (Spring Boot или Java Application)

Если её нет, создай новую (+ → Spring Boot или Application).

В поле Environment variables добавь переменные

Найди раздел Environment → Environment variables.

Нажми на значок ... (или Alt + Insert).

Добавь переменные в формате KEY=VALUE, например:
 */