package com.eyxpoliba.emotion_recognition.payload;

import lombok.Getter;

public record ImageDescriptionAndReactionPayload(String imageId, String description, String reaction, String aiComment) {

}
