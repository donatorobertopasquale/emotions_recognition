from fastapi import FastAPI, UploadFile, File
from fastapi.middleware.cors import CORSMiddleware
import sys
import os
sys.path.append(os.path.join(os.path.dirname(__file__), "emotion_recognizer"))
from emotion_recognizer import EmotionRecognizer, EmotionResponse

app = FastAPI()
emotion_recognizer = EmotionRecognizer()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allows all origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.post("/api/predict/", response_model=EmotionResponse)
async def predict_emotion(file: UploadFile = File(...)):
    """Endpoint to predict emotion from uploaded image"""
    contents = await file.read()
    result = emotion_recognizer.process_video(contents)
    return result

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)