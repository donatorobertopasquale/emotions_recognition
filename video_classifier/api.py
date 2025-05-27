from fastapi import FastAPI, UploadFile, File, Request
from fastapi.middleware.cors import CORSMiddleware
import sys
import os
import logging
sys.path.append(os.path.join(os.path.dirname(__file__), "emotion_recognizer"))
from emotion_recognizer import EmotionRecognizer, EmotionResponse
from jwt_middleware import JWTAuthMiddleware
from config import Config

# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Load environment variables
Config.load_env_file(os.path.join(os.path.dirname(__file__), "..", ".env"))
logger.info("Environment variables loaded")

app = FastAPI(
    title="Emotion Recognition API",
    description="API for video-based emotion recognition with JWT authentication",
    version="1.0.0"
)
emotion_recognizer = EmotionRecognizer()
logger.info("Emotion recognizer initialized")

# Add JWT authentication middleware
app.add_middleware(JWTAuthMiddleware)
logger.info("JWT authentication middleware added")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allows all origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/api/health")
async def health_check():
    """Health check endpoint - no authentication required"""
    logger.info("Health check requested")
    return {"status": "healthy", "service": "emotion-classifier"}

@app.post("/api/predict/", response_model=EmotionResponse)
async def predict_emotion(request: Request, file: UploadFile = File(...)):
    """Endpoint to predict emotion from uploaded image/video"""
    # User info is available in request.state.user_info (set by middleware)
    user_info = getattr(request.state, 'user_info', {})
    username = user_info.get('username', 'unknown')
    user_id = user_info.get('user_id', 'unknown')
    
    logger.info(f"Emotion prediction requested by user {username} (ID: {user_id})")
    logger.debug(f"File: {file.filename}, Content-Type: {file.content_type}")
    
    try:
        contents = await file.read()
        result = emotion_recognizer.process_video(contents)
        
        logger.info(f"Emotion prediction successful: {result.emotion} (confidence: {result.confidence:.4f})")
        return result
    except Exception as e:
        logger.error(f"Emotion prediction failed for user {username}: {str(e)}")
        raise e

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)