package com.eyxpoliba.emotion_recognition.repository;

import com.eyxpoliba.emotion_recognition.model.ReactionsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReactionsRepository extends JpaRepository<ReactionsEntity, Long> {
}
