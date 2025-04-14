package com.eyxpoliba.emotion_recognition.controller;

import com.eyxpoliba.emotion_recognition.model.UserEntity;
import com.eyxpoliba.emotion_recognition.service.ImageService;
import com.eyxpoliba.emotion_recognition.service.ResultService;
import com.eyxpoliba.emotion_recognition.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AppController {
    private final UserService userService;
    private final ImageService imageService;
    private final ResultService resultService;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody UserEntity user) {
        userService.login(user);
        return ResponseEntity.created(URI.create("/api/user/login")).body("Login successful");
    }

    @GetMapping("/request-photo/{image_number}")
    public ResponseEntity<Object> requestPhoto(@PathVariable("image_number") int n) {
        return ResponseEntity.ok(imageService.getImage(n));
    }

    @PostMapping("/register-result")
    public ResponseEntity<Object> registerResult(@RequestParam("user") Long userId) {
        return resultService.registerResult(userId);
    }
}
