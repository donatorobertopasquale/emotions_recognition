package com.eyxpoliba.emotion_recognition.service;

import com.eyxpoliba.emotion_recognition.model.UserEntity;
import com.eyxpoliba.emotion_recognition.repository.UserRepository;
import com.eyxpoliba.emotion_recognition.responses.LoginResponse;
import com.eyxpoliba.emotion_recognition.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final AzureStorageService azureStorageService;
    private final JwtProvider jwtProvider;

    public ResponseEntity<LoginResponse> login(UserEntity user) {
        UserEntity newUser = userRepository.save(user);

        HashMap<String, String> tokens = packJwts(newUser.getNickname(), newUser.getId());
        List<String> imagesName = azureStorageService.getRandomBlobNames(10);
        return ResponseEntity.created(URI.create("/api/login"))
                .body(new LoginResponse(newUser.getId(),
                        tokens.get("access_token"), tokens.get("refresh_token"), imagesName));
    }

    private HashMap<String, String> packJwts(String username, long userId) {
        String accessToken = jwtProvider.generateToken(username, userId, true);
        String refreshToken = jwtProvider.generateToken(username, userId, false);
        HashMap<String, String> payload = new HashMap<>();
        payload.put("access_token", accessToken);
        payload.put("refresh_token", refreshToken);

        return payload;
    }
}
