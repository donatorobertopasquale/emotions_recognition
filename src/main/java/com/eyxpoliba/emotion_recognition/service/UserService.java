package com.eyxpoliba.emotion_recognition.service;

import com.eyxpoliba.emotion_recognition.model.UserEntity;
import com.eyxpoliba.emotion_recognition.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public ResponseEntity<Object> login(UserEntity user) {
        userRepository.save(user);
        return ResponseEntity.ok("Login successful");
    }
}
