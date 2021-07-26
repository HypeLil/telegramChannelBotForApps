package com.hypeli.io.telegramChannelBotForApps.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Getter
@Setter
@Table(name = "users")
public class User {

    @Id
    String id;
    String position = "exit";

    public User(String id) {
        this.id = id;
    }

    public User() {

    }
}
