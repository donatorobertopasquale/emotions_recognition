# Video Emotion Recognition

A deep learning-based video emotion recognition system that identifies emotions from videos using facial expressions. The system detects faces in video frames and classifies emotions into 7 categories: happy, surprise, sad, anger, disgust, fear, and neutral.

## Features

- Real-time emotion recognition from videos or webcam feed
- API endpoint for processing video uploads
- Support for processing pre-recorded video files
- Visualization of emotion predictions with bounding boxes and labels
- Output video generation with emotion annotations

## Installation

### Prerequisites

- Python 3.7+
- PyTorch
- OpenCV
- FastAPI (for API functionality)

### Setup

1. Clone the repository:
```bash
git clone <repository-url>
cd emotions_recognition/video_classifier
```

2. Install required packages:
```bash
pip install -r requirements.txt
```

3. Download the pre-trained model:
```bash
python download_models.py
```

## Usage

### Command Line Interface

Process a video file:
```bash
python emotion_recognizer/emotion_predictor.py --video path/to/video.mp4 --output path/to/output.mp4
```

Use webcam for real-time emotion recognition:
```bash
python emotion_recognizer/emotion_predictor.py
```

Additional options:
- `--model`: Specify a different model path
- `--no-display`: Process without displaying the video

### API

Start the API server:
```bash
python api.py
```

The API will be available at `http://localhost:8000` with the following endpoint:
- `POST /predict/`: Upload a video file for emotion analysis

Example request using curl:
```bash
curl -X POST -F "file=@path/to/video.mp4" http://localhost:8000/predict/
```

Example response:
```json
{
  "emotion": "happy",
  "confidence": 0.75,
  "all_emotions": {
    "happy": 0.75,
    "surprise": 0.05,
    "sad": 0.03,
    "anger": 0.02,
    "disgust": 0.01,
    "fear": 0.01,
    "neutral": 0.13
  }
}
```

## Technical Details

The system uses:
- ResEmoteNet architecture trained on multiple emotion datasets
- Haar cascade classifiers for face detection
- PyTorch for deep learning inference
- MPS acceleration on compatible Apple devices, with CPU fallback
- Softmax normalization with neutral emotion penalty for overall video classification

## Model

The pre-trained emotion recognition model is based on ResEmoteNet architecture and trained on multiple emotion datasets. The model is downloaded from the Hugging Face Hub when running the `download_models.py` script.

The pre-trained model is downloaded from https://huggingface.co/neilchouGTX/ResEmoteNet_Four_datasets_BatchSize32/tree/main
The original codebase to train and use the model is https://github.com/ArnabKumarRoy02/ResEmoteNet, which ranks #1 on FER tasks on https://paperswithcode.com/paper/resemotenet-bridging-accuracy-and-loss#code

