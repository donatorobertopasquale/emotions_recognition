package com.eyxpoliba.emotion_recognition.service;

import com.eyxpoliba.emotion_recognition.model.ResultEntity;
import com.eyxpoliba.emotion_recognition.repository.ResultRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@AllArgsConstructor
public class ResultService {
    private final ResultRepository resultRepository;

    public ResponseEntity<Object> registerResult(Long userId) {
        resultRepository.save(new ResultEntity());
        return ResponseEntity.created(URI.create("/api/register-result")).body("Result registered successfully");
    }
}
