package com.eyxpoliba.emotion_recognition.service;

import com.eyxpoliba.emotion_recognition.model.ReactionsEntity;
import com.eyxpoliba.emotion_recognition.payload.ImageDescriptionAndReactionPayload;
import com.eyxpoliba.emotion_recognition.payload.ResultPayload;
import com.eyxpoliba.emotion_recognition.repository.ReactionsRepository;
import com.eyxpoliba.emotion_recognition.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@AllArgsConstructor
public class ReactionsService {
    private final ReactionsRepository reactionsRepository;
    private final UserRepository userRepository;

    public ResponseEntity<Object> registerResult(ResultPayload payload) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getCredentials();

        for (ImageDescriptionAndReactionPayload imageDescrAndReac: payload.imagesDescriptionsAndReactions()) {
            ReactionsEntity newResult = ReactionsEntity.builder()
                    .userId(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")))
                    .image(imageDescrAndReac.image())
                    .imageDescription(imageDescrAndReac.description())
                    .imageReaction(imageDescrAndReac.reaction())
                    .aiComment(imageDescrAndReac.aiComment())
                    .build();

            reactionsRepository.save(newResult);
        }

            return ResponseEntity.created(URI.create("/api/register-result")).body("{\"message\": \"Result registered successfully\"}");
    }
}
