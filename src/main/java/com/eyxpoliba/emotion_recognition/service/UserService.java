package com.eyxpoliba.emotion_recognition.service;

import com.eyxpoliba.emotion_recognition.model.UserEntity;
import com.eyxpoliba.emotion_recognition.repository.UserRepository;
import com.eyxpoliba.emotion_recognition.responses.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AzureStorageService azureStorageService;

    public ResponseEntity<LoginResponse> login(UserEntity user) {
        UserEntity newUser = userRepository.save(user);

        List<String> images = azureStorageService.getImagesForUserSession();
        return ResponseEntity.created(URI.create("/api/login"))
                .body(new LoginResponse(newUser.getId(), images));
    }
}
