from fastapi import FastAPI, UploadFile, File
from model import EmotionRecognizer, EmotionResponse
from PIL import Image
import io

app = FastAPI()
emotion_recognizer = EmotionRecognizer()

@app.post("/predict/", response_model=EmotionResponse)
async def predict_emotion(file: UploadFile = File(...)):
    """Endpoint to predict emotion from uploaded image"""
    contents = await file.read()
    image = Image.open(io.BytesIO(contents))
    result = emotion_recognizer.predict_from_image(image, file.filename)
    return result

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)