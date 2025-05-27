package com.eyxpoliba.emotion_recognition.responses;

import java.util.List;


public record LoginResponse(Long userId, List<String> imagesName) {
}
