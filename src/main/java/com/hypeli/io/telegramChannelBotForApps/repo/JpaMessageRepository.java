package com.hypeli.io.telegramChannelBotForApps.repo;

import com.hypeli.io.telegramChannelBotForApps.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaMessageRepository extends JpaRepository<Message, String> {

}
