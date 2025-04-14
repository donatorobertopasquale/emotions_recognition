package com.eyxpoliba.emotion_recognition.repository;

import com.eyxpoliba.emotion_recognition.model.ResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResultRepository extends JpaRepository<ResultEntity, Long> {
}
