package com.eyxpoliba.emotion_recognition.dto;

import lombok.Data;

@Data
public class GoogleLoginRequest {
    private String googleCredential; // Google JWT token
    private String nickname;
    private int age;
    private String gender;
    private String nationality;
}
