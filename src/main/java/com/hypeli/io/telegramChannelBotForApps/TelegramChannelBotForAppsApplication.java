package com.hypeli.io.telegramChannelBotForApps;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TelegramChannelBotForAppsApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelegramChannelBotForAppsApplication.class, args);
	}

}
