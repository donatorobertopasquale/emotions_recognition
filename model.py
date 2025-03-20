from transformers import AutoImageProcessor, AutoModelForImageClassification
from PIL import Image
import os
import sys
from pydantic import BaseModel
from typing import Dict, List, Optional
import torch

sys.path.append(os.path.dirname(os.path.abspath(os.path.dirname(__file__))))

class EmotionResponse(BaseModel):
    """Pydantic model for emotion recognition response"""
    emotion: str
    confidence: float
    all_emotions: Dict[str, float]
    image_name: Optional[str] = None

class EmotionRecognizer:
    """Class for emotion recognition from images"""
    
    def __init__(self):
        self.classes = ["sad", "disgust", "angry", "neutral", "fear", "surprise", "happy"]
        self.processor = AutoImageProcessor.from_pretrained(
            "dima806/facial_emotions_image_detection", 
            cache_dir=os.path.join(os.getcwd(), 'huggingface')
        )
        self.model = AutoModelForImageClassification.from_pretrained(
            "dima806/facial_emotions_image_detection", 
            cache_dir=os.path.join(os.getcwd(), 'huggingface')
        )
    
    def predict_from_path(self, image_path: str) -> EmotionResponse:
        """Predict emotion from an image file path"""
        try:
            image = Image.open(image_path)
            return self.predict_from_image(image, os.path.basename(image_path))
        except Exception as e:
            raise ValueError(f"Error processing image {image_path}: {str(e)}")
    
    def predict_from_image(self, image: Image.Image, image_name: Optional[str] = None) -> EmotionResponse:
        """Predict emotion from a PIL Image object"""
        inputs = self.processor(images=image, return_tensors="pt")
        outputs = self.model(**inputs)
        
        # Get probabilities with softmax
        probs = torch.nn.functional.softmax(outputs.logits, dim=1)[0]
        
        # Get the predicted class index and confidence
        predicted_idx = outputs.logits.argmax().item()
        predicted_emotion = self.classes[predicted_idx]
        confidence = probs[predicted_idx].item()
        
        # Create dictionary with all emotion confidences
        all_emotions = {emotion: probs[i].item() for i, emotion in enumerate(self.classes)}
        
        return EmotionResponse(
            emotion=predicted_emotion,
            confidence=confidence,
            all_emotions=all_emotions,
            image_name=image_name
        )

# Example usage:
# emotion_recognizer = EmotionRecognizer()
# for image_name in os.listdir("images"):
#     image_path = os.path.join("images", image_name)
#     result = emotion_recognizer.predict_from_path(image_path)
#     print(f"Image: {result.image_name}, Emotion: {result.emotion}, Confidence: {result.confidence:.2f}")