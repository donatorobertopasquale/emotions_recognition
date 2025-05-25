package com.eyxpoliba.emotion_recognition.service;

import com.eyxpoliba.emotion_recognition.model.ReactionsEntity;
import com.eyxpoliba.emotion_recognition.payload.ImageDescriptionAndReactionPayload;
import com.eyxpoliba.emotion_recognition.payload.ResultPayload;
import com.eyxpoliba.emotion_recognition.repository.ReactionsRepository;
import com.eyxpoliba.emotion_recognition.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;

@Service
@AllArgsConstructor
public class ReactionsService {
    private final ReactionsRepository reactionsRepository;
    private final UserRepository userRepository;

    public ResponseEntity<Object> registerResult(ResultPayload payload) {
        System.out.println(payload);
        for (ImageDescriptionAndReactionPayload imageDescrAndReac: payload.imagesDescriptionsAndReactions()) {
            ReactionsEntity newResult = ReactionsEntity.builder()
                    .userId(userRepository.findById(payload.userId()).orElseThrow(() -> new RuntimeException("User not found")))
                    .imageId(imageDescrAndReac.imageId())
                    .imageDescription(imageDescrAndReac.description())
                    .imageReaction(imageDescrAndReac.reaction().toString())
                    .aiComment(imageDescrAndReac.aiComment())
                    .build();

            reactionsRepository.save(newResult);
        }

        return ResponseEntity.created(URI.create("/api/register-result")).body("Result registered successfully");
    }
}
