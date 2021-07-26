package com.hypeli.io.telegramChannelBotForApps.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "channels")
public class Channel {

    @Id
    String id;

    String url;

    public Channel(String id) {
        this.id = id;
    }

    public Channel() {

    }
}
