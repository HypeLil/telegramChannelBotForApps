package com.hypeli.io.telegramChannelBotForApps.service;

import com.hypeli.io.telegramChannelBotForApps.model.Message;
import com.hypeli.io.telegramChannelBotForApps.repo.JpaMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final JpaMessageRepository messageRepository;

    public Message save(Message message){
        return messageRepository.save(message);
    }

    public Message findById(String id){
        Optional<Message> message = messageRepository.findById(id);
        if (message.isPresent()){
            return message.get();
        }
        else {
            throw new EntityNotFoundException("Entity not found by id " + id);
        }
    }
}
