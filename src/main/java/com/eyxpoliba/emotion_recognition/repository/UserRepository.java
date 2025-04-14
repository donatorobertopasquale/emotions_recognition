package com.eyxpoliba.emotion_recognition.repository;

import com.eyxpoliba.emotion_recognition.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
