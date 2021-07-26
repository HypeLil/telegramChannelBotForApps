package com.hypeli.io.telegramChannelBotForApps.service;

import com.hypeli.io.telegramChannelBotForApps.model.User;
import com.hypeli.io.telegramChannelBotForApps.repo.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JpaUserRepository repository;

    private Optional<User> get(String id){
        return repository.findById(id);
    }

    public User save(User user){
        return repository.save(user);
    }

    public User findById(String id){
        Optional<User> user = get(id);
        if (user.isEmpty()){
            save(new User(id));
        }
        return get(id).get();
    }

    public List<User> findAll(){
        return repository.findAll();
    }
}
