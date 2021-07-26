package com.hypeli.io.telegramChannelBotForApps.repo;

import com.hypeli.io.telegramChannelBotForApps.model.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaChannelRepository extends JpaRepository<Channel, String> {
}
