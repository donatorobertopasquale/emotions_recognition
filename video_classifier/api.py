from fastapi import FastAPI, UploadFile, File
import sys
import os
sys.path.append(os.path.join(os.path.dirname(__file__), "emotion_recognizer"))
from emotion_recognizer import EmotionRecognizer, EmotionResponse

app = FastAPI()
emotion_recognizer = EmotionRecognizer()

@app.post("/predict/", response_model=EmotionResponse)
async def predict_emotion(file: UploadFile = File(...)):
    """Endpoint to predict emotion from uploaded image"""
    contents = await file.read()
    result = emotion_recognizer.process_video(contents)
    return result

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)