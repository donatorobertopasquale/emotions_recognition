package com.eyxpoliba.emotion_recognition.repository;

import com.eyxpoliba.emotion_recognition.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByGoogleId(String googleId);
    Optional<UserEntity> findByEmail(String email);
}
