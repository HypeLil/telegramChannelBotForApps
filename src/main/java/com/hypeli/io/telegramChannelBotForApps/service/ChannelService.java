package com.hypeli.io.telegramChannelBotForApps.service;

import com.hypeli.io.telegramChannelBotForApps.model.Channel;
import com.hypeli.io.telegramChannelBotForApps.repo.JpaChannelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final JpaChannelRepository channelRepository;

    public Channel save(Channel channel){
        return channelRepository.save(channel);
    }

    private Optional<Channel> get(String id){
        return channelRepository.findById(id);
    }

    @Cacheable(value = "channels")
    public Channel findById(String id){
        Optional<Channel> channel = get(id);
        if (channel.isPresent()){
            return channel.get();
        }
        else {
            channelRepository.save(new Channel(id));
            return get(id).orElseThrow(() -> new EntityNotFoundException("User not found by id " + id));
        }
    }

    @CacheEvict("channels")
    public boolean delete(String id){
        Optional<Channel> channel = get(id);
        if (channel.isPresent()){
            channelRepository.delete(channel.get());
            return true;
        }
        else {
            return false;
        }
    }
    public Channel[] findAll(){
        List<Channel> channels = channelRepository.findAll();
        return channels.toArray(new Channel[0]);
    }
}
