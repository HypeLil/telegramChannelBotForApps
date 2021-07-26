package com.hypeli.io.telegramChannelBotForApps.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(name = "messages")
public class Message {

    @Id
    @Column( name = "id")
    private String id;

    private String chatId;
}
