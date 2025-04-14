package com.eyxpoliba.emotion_recognition.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "users")
public class UserEntity {
    @Id
    private Long id;
    private String nickname;
    private String email;
    private int age;
    private String gender;
    private String nationality;
}
