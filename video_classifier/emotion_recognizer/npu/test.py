import cv2
import cv2.data
import torch
import torch.nn.functional as F
import torchvision.transforms as transforms
from PIL import Image
import numpy as np
import os
from pydantic import BaseModel
from typing import Dict
from rknn.api import RKNN


class EmotionResponse(BaseModel):
    """Pydantic model for emotion recognition response"""
    emotion: str
    confidence: float
    all_emotions: Dict[str, float]


class EmotionRecognizer:
    def __init__(self, model_path='ResEmoteNetBS32.rknn'):
        
        self.device = torch.device("mps" if torch.backends.mps.is_available() else "cpu")
        
        # Emotions labels
        self.emotions = ['happy', 'surprise', 'sad', 'anger', 'disgust', 'fear', 'neutral']
        
        # Load model
        self.model = RKNN()
        self.model.config(
        mean_values=[[0.485*255, 0.456*255, 0.406*255]],  # Match your normalization values
        std_values=[[0.229*255, 0.224*255, 0.225*255]],   # Multiply by 255 for 0-255 range
        target_platform='rk3588'
        )
        model_path = os.path.join(os.path.dirname(__file__), model_path)
        self.model.load_rknn(model_path)
        self.model.init_runtime(target="rk3588")
        
        # Image transformation pipeline
        self.transform = transforms.Compose([
            transforms.Resize((64, 64)),
            transforms.Grayscale(num_output_channels=3),
            transforms.ToTensor(),
            transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
        ])
        
        # Load face classifier
        self.face_classifier = cv2.CascadeClassifier(
            cv2.data.haarcascades + 'haarcascade_frontalface_default.xml'
        )
        
        # Settings for text
        self.font = cv2.FONT_HERSHEY_SIMPLEX
        self.font_scale = 1.2
        self.font_color = (0, 255, 0)  # BGR color
        self.thickness = 3
        self.line_type = cv2.LINE_AA
        
        # Evaluation settings
        self.evaluation_frequency = 5
    
    def detect_emotion(self, image):
        """Detect emotion from a cropped face image."""
        try:
            # Convert PIL image to numpy array if not already
            if isinstance(image, Image.Image):
                img_np = np.array(image)
            else:
                img_np = np.array(image)
                
            # Apply resize using OpenCV
            img_np = cv2.resize(img_np, (64, 64))
            
            # Convert to RGB if grayscale
            if len(img_np.shape) == 2:
                img_np = cv2.cvtColor(img_np, cv2.COLOR_GRAY2RGB)
            elif img_np.shape[2] == 1:
                img_np = cv2.cvtColor(img_np, cv2.COLOR_GRAY2RGB)
            
            # IMPORTANT: OpenCV uses BGR by default, convert to RGB for consistent processing
            if img_np.shape[2] == 3:  # Only convert if it's a color image
                img_np = cv2.cvtColor(img_np, cv2.COLOR_BGR2RGB)
            
            img_np = img_np.astype(np.float32) 
                    
            # Ensure format is NHWC by adding batch dimension
            img_np = np.expand_dims(img_np, axis=0)  # Add batch dimension
            
            # Debug print to verify input shape and range
            print(f"Input shape: {img_np.shape}, Range: [{np.min(img_np):.3f}, {np.max(img_np):.3f}]")
            
            # Try inference with explicit data_format
            outputs = self.model.inference(inputs=[img_np], data_format='nhwc')
            
            # Process output
            if outputs and len(outputs) > 0:
                logits = outputs[0]
                # Print raw logits for debugging
                print(f"Raw logits: {logits}")
                
                # Apply softmax to get probabilities
                exp_logits = np.exp(logits - np.max(logits, axis=1, keepdims=True))
                probabilities = exp_logits / np.sum(exp_logits, axis=1, keepdims=True)
                scores = probabilities.flatten()
                rounded_scores = [round(float(score), 2) for score in scores]
                
                # Print scores for debugging
                print(f"Emotion scores: {list(zip(self.emotions, rounded_scores))}")
                return rounded_scores
            else:
                print("Warning: Model returned empty output")
                return [0.0] * len(self.emotions)
                    
        except Exception as e:
            print(f"Error in detect_emotion: {str(e)}")
            import traceback
            traceback.print_exc()
            return [0.0] * len(self.emotions)
    
    def get_max_emotion(self, face_crop):
        """Get the most likely emotion for a face crop."""
        rounded_scores = self.detect_emotion(face_crop)
        max_index = np.argmax(rounded_scores)
        max_emotion = self.emotions[max_index]
        return max_emotion, rounded_scores
    
    def process_frame(self, frame, counter=0):
        """Process a single video frame, detecting faces and emotions."""
        # Convert to grayscale for face detection
        gray_image = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
        
        # Detect faces
        faces = self.face_classifier.detectMultiScale(gray_image, 1.1, 5, minSize=(40, 40))
        
        face_emotions = []
        
        # Process each detected face
        for (x, y, w, h) in faces:
            # Draw bounding box
            cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 2)
            
            # Crop the face
            face_crop = frame[y:y+h, x:x+w]
            
            # Convert to PIL Image
            try:
                pil_face = Image.fromarray(face_crop)
                
                # Detect emotion (only on specified frames or for all if counter==0)
                if counter % self.evaluation_frequency == 0:
                    max_emotion, scores = self.get_max_emotion(pil_face)
                else:
                    # Use previous emotion if not recalculating
                    max_emotion = "Processing..."
                    scores = [0] * len(self.emotions)
                
                # Add emotion to result
                face_emotions.append({
                    "position": (x, y, w, h),
                    "emotion": max_emotion,
                    "scores": scores
                })
                
                # Display the predicted emotion
                org = (x, y - 15)
                cv2.putText(frame, max_emotion, org, self.font, self.font_scale, 
                           self.font_color, self.thickness, self.line_type)
                
                # Display all emotion scores
                org = (x + w + 10, y - 20)
                for index, emotion in enumerate(self.emotions):
                    emotion_str = f"{emotion}: {scores[index]:.2f}" if isinstance(scores[index], float) else f"{emotion}: N/A"
                    y_pos = org[1] + 40 * (index + 1)
                    cv2.putText(frame, emotion_str, (org[0], y_pos), self.font, 0.7, 
                               self.font_color, 2, self.line_type)
                
            except Exception as e:
                print(f"Error processing face: {e}")
        
        return frame, face_emotions
    
    def process_video(self, video_source, output_path=None, display=False):
        """Process a video from either a file path or a byte stream."""
        
        # Handle different input types
        if isinstance(video_source, str) and os.path.exists(video_source):
            # Open video file
            cap = cv2.VideoCapture(video_source)
        elif isinstance(video_source, bytes) or isinstance(video_source, bytearray):
            # Convert bytes to numpy array
            nparr = np.frombuffer(video_source, np.uint8)
            # Create temporary file
            temp_file = "temp_video.mp4"
            with open(temp_file, "wb") as f:
                f.write(video_source)
            cap = cv2.VideoCapture(temp_file)
        else:
            raise ValueError("Invalid video source. Provide either a file path or byte stream.")
        
        if not cap.isOpened():
            raise ValueError("Could not open video source")
        
        # Get video properties
        width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
        height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
        fps = cap.get(cv2.CAP_PROP_FPS)
        
        # Setup video writer if output path provided
        writer = None
        if output_path:
            fourcc = cv2.VideoWriter_fourcc(*'mp4v')
            writer = cv2.VideoWriter(output_path, fourcc, fps, (width, height))
        
        counter = 0
        all_results = []
        scores_sum = [0] * len(self.emotions)
        
        while True:
            ret, frame = cap.read()
            if not ret:
                break
            
            # Process the frame
            processed_frame, face_emotions = self.process_frame(frame, counter)
            for face_emotion in face_emotions:
                for index, score in enumerate(face_emotion["scores"]):
                    scores_sum[index] += score
                        
            
            # Store results
            frame_result = {
                "frame_number": counter,
                "faces": face_emotions
            }
            all_results.append(frame_result)
            
            # Write to output video
            if writer:
                writer.write(processed_frame)
            
            # Display the frame
            if display:
                cv2.imshow('Emotion Recognition', processed_frame)
                if cv2.waitKey(1) & 0xFF == ord('q'):
                    break
            
            counter += 1
        
        # Clean up
        cap.release()
        if writer:
            writer.release()
        if display:
            cv2.destroyAllWindows()
        
        # Remove temporary file if created
        if isinstance(video_source, bytes) or isinstance(video_source, bytearray):
            if os.path.exists(temp_file):
                os.remove(temp_file)
        self.model.release()
        # Apply softmax to normalize the scores between 0 and 1 with sum = 1
        if counter:  # Only normalize if we have processed frames
            # Apply penalty to neutral emotion (index 6)
            neutral_penalty = 0.7  # Adjust this factor as needed
            scores_sum[self.emotions.index('neutral')] *= neutral_penalty
            
            # Convert to torch tensor 
            scores_tensor = torch.tensor(scores_sum, dtype=torch.float32)
            # Apply softmax using torch
            softmax_scores = F.softmax(scores_tensor, dim=0).numpy()
            # Create dictionary mapping emotion labels to their softmax scores
            emotion_scores = {emotion: float(score) for emotion, score in zip(self.emotions, softmax_scores)}
            # Find most likely emotion
            max_emotion = max(emotion_scores, key=emotion_scores.get)
            max_confidence = emotion_scores[max_emotion]
            
            return EmotionResponse(
            emotion=max_emotion,
            confidence=max_confidence,
            all_emotions=emotion_scores
            )
        else:
            # Return empty response if no frames processed
            return EmotionResponse(
            emotion="unknown",
            confidence=0.0,
            all_emotions={emotion: 0.0 for emotion in self.emotions}
            )


# Example usage
if __name__ == "__main__":
    import argparse
    import os
    os.chdir(os.path.dirname(__file__))
    
    parser = argparse.ArgumentParser(description="Emotion Recognition from Video")
    parser.add_argument("--video", type=str, default=None, help="Path to video file (default: use webcam)")
    parser.add_argument("--output", type=str, default=None, help="Output video path")
    parser.add_argument("--model", type=str, default='ResEmoteNetBS32.rknn', help="Path to model checkpoint")
    parser.add_argument("--no-display", action="store_true", help="Don't display video while processing")
    
    args = parser.parse_args()
    
    recognizer = EmotionRecognizer(model_path=args.model)
    
    if args.video:
        # Process video file
        results = recognizer.process_video(args.video, args.output, not args.no_display)
        # print(f"Processed {len(results)} frames")
        print(f"Emotion: {results.emotion} (Confidence: {results.confidence:.2f})")
        print("All Emotions:")
        for emotion, score in results.all_emotions.items():
            print(f"{emotion}: {score:.2f}")
    else:
        # Use webcam
        cap = cv2.VideoCapture(0)
        if not cap.isOpened():
            print("Error: Could not open webcam")
            exit()
        
        counter = 0
        while True:
            ret, frame = cap.read()
            if not ret:
                break
            
            processed_frame, emotions = recognizer.process_frame(frame, counter)
            cv2.imshow('Emotion Recognition', processed_frame)
            print([emotion.get("emotion") for emotion in emotions if emotion.get("emotion")!='Processing...'])
            
            if cv2.waitKey(1) & 0xFF == ord('q'):
                break
            
            counter += 1
        
        cap.release()
        cv2.destroyAllWindows()