package com.eyxpoliba.emotion_recognition.repository;

import com.eyxpoliba.emotion_recognition.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

}
