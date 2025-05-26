package com.eyxpoliba.emotion_recognition.payload;

import java.util.List;

public record ResultPayload(List<ImageDescriptionAndReactionPayload> imagesDescriptionsAndReactions) {
}