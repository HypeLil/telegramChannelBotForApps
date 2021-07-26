package com.hypeli.io.telegramChannelBotForApps.repo;

import com.hypeli.io.telegramChannelBotForApps.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserRepository extends JpaRepository<User, String> {
}
