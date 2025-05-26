package com.eyxpoliba.emotion_recognition.controller;

import com.eyxpoliba.emotion_recognition.model.UserEntity;
import com.eyxpoliba.emotion_recognition.payload.ResultPayload;
import com.eyxpoliba.emotion_recognition.responses.LoginResponse;
import com.eyxpoliba.emotion_recognition.service.AzureStorageService;
import com.eyxpoliba.emotion_recognition.service.ReactionsService;
import com.eyxpoliba.emotion_recognition.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AppController {
    private final UserService userService;
    private final AzureStorageService azureStorageService;
    private final ReactionsService reactionsService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody UserEntity user) {
        return userService.login(user);
    }

    @GetMapping("/download-image")
    public ResponseEntity<byte[]> downloadImage(@RequestParam String imageName) throws IOException {
        return ResponseEntity.ok(azureStorageService.downloadImage(imageName));
    }

    @PostMapping("/register-result")
    public ResponseEntity<Object> registerResult(@RequestBody ResultPayload resultPayload) {
        return reactionsService.registerResult(resultPayload);
    }
}
