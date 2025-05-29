# Video Classifier Development Setup

## Overview

This guide provides comprehensive instructions for setting up a development environment for the Video Classifier service, including prerequisites, environment configuration, testing strategies, and debugging techniques.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Environment Setup](#environment-setup)
- [Development Workflow](#development-workflow)
- [Testing](#testing)
- [Debugging](#debugging)
- [Performance Optimization](#performance-optimization)
- [Troubleshooting](#troubleshooting)

## Prerequisites

### System Requirements

#### Hardware
- **CPU**: Modern multi-core processor (Intel i5/AMD Ryzen 5 or better)
- **Memory**: 8 GB RAM minimum, 16 GB recommended
- **Storage**: 10 GB free space for models and dependencies
- **GPU**: Optional but recommended for faster inference (NVIDIA with CUDA support or Apple Silicon with MPS)

#### Operating System
- **Linux**: Ubuntu 20.04 LTS or newer (recommended for production)
- **macOS**: macOS 11.0 or newer (M1/M2 Macs supported with MPS acceleration)
- **Windows**: Windows 10/11 with WSL2 for best compatibility

### Software Dependencies

#### Python Environment
```bash
# Required Python version
Python 3.11+ (recommended: 3.11.x)

# Verify installation
python --version
```

#### System Libraries (Linux/Ubuntu)
```bash
# Install system dependencies for OpenCV and dlib
sudo apt-get update
sudo apt-get install -y \
    build-essential \
    cmake \
    libsm6 \
    libxext6 \
    libxrender-dev \
    libgl1-mesa-glx \
    libglib2.0-0 \
    python3-dev \
    python3-pip
```

#### System Libraries (macOS)
```bash
# Install Homebrew if not already installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install dependencies
brew install cmake
brew install python@3.11
```

#### System Libraries (Windows/WSL2)
```bash
# In WSL2 Ubuntu environment
sudo apt-get update
sudo apt-get install -y \
    build-essential \
    cmake \
    libsm6 \
    libxext6 \
    libxrender-dev \
    python3-dev \
    python3-pip
```

## Environment Setup

### 1. Clone Repository

```bash
git clone <repository-url>
cd emotions_recognition/video_classifier
```

### 2. Virtual Environment Setup

#### Using venv (Recommended)
```bash
# Create virtual environment
python -m venv venv

# Activate virtual environment
# Linux/macOS:
source venv/bin/activate
# Windows:
venv\Scripts\activate

# Verify activation
which python  # Should point to venv/bin/python
```

#### Using conda (Alternative)
```bash
# Create conda environment
conda create -n emotion-classifier python=3.11
conda activate emotion-classifier

# Verify activation
conda info --envs
```

### 3. Install Dependencies

```bash
# Install Python dependencies
pip install --upgrade pip
pip install -r requirements.txt

# Verify key packages
python -c "import torch; print(f'PyTorch: {torch.__version__}')"
python -c "import cv2; print(f'OpenCV: {cv2.__version__}')"
python -c "import fastapi; print('FastAPI installed successfully')"
```

### 4. Environment Configuration

Create environment file:
```bash
# Create .env file in video_classifier directory
cat > .env << EOF
# JWT Configuration
SECURITY_SECRET=c2VjcmV0  # base64 encoded "secret"
SECURITY_ISSUER=emotion_recognition

# Server Configuration
HOST=0.0.0.0
PORT=8000

# Development Settings
DEBUG=true
LOG_LEVEL=INFO
EOF
```

### 5. Model Setup

#### Download ML Models
```bash
# Download pre-trained ResEmoteNet model
python download_models.py

# Verify model files
ls -la emotion_recognizer/models/ResEmoteNet/
# Should contain: ResEmoteNetBS32.pth
```

#### Model Verification
```bash
# Test model loading
python -c "
from emotion_recognizer import EmotionRecognizer
recognizer = EmotionRecognizer()
print('Model loaded successfully')
print(f'Device: {recognizer.device}')
print(f'Emotions: {recognizer.emotions}')
"
```

## Development Workflow

### 1. Starting the Development Server

```bash
# Method 1: Direct uvicorn
uvicorn api:app --host 0.0.0.0 --port 8000 --reload

# Method 2: Using Python
python api.py

# Method 3: FastAPI with auto-reload
fastapi dev api.py --host 0.0.0.0 --port 8000
```

#### Development Server Features
- **Auto-reload**: Automatically restarts on code changes
- **Interactive docs**: Available at `http://localhost:8000/docs`
- **Debug mode**: Enhanced error messages and tracebacks

### 2. API Documentation

Access interactive documentation:
```bash
# Swagger UI
open http://localhost:8000/docs

# ReDoc
open http://localhost:8000/redoc

# OpenAPI JSON
curl http://localhost:8000/openapi.json
```

### 3. Code Structure

```
video_classifier/
├── api.py                    # FastAPI application entry point
├── config.py                 # Configuration management
├── jwt_auth.py              # JWT authentication logic
├── jwt_middleware.py        # Authentication middleware
├── requirements.txt         # Python dependencies
├── Dockerfile              # Container configuration
├── download_models.py      # Model download script
├── emotion_recognizer/     # ML pipeline
│   ├── __init__.py
│   ├── emotion_predictor.py # Main emotion recognition logic
│   ├── ResEmoteNet.py      # Neural network architecture
│   └── models/             # Pre-trained models
└── tests/                  # Test suite
    ├── test_api.py         # API endpoint tests
    └── test_jwt.py         # Authentication tests
```

### 4. Development Best Practices

#### Code Formatting
```bash
# Install development tools
pip install black isort flake8 mypy

# Format code
black *.py
isort *.py

# Lint code
flake8 *.py

# Type checking
mypy *.py
```

#### Git Hooks (Optional)
```bash
# Install pre-commit
pip install pre-commit

# Create .pre-commit-config.yaml
cat > .pre-commit-config.yaml << EOF
repos:
  - repo: https://github.com/psf/black
    rev: 23.3.0
    hooks:
      - id: black
  - repo: https://github.com/pycqa/isort
    rev: 5.12.0
    hooks:
      - id: isort
  - repo: https://github.com/pycqa/flake8
    rev: 6.0.0
    hooks:
      - id: flake8
EOF

# Install hooks
pre-commit install
```

## Testing

### 1. Unit Tests

#### Running Tests
```bash
# Run all tests
python -m pytest tests/ -v

# Run specific test file
python -m pytest tests/test_jwt.py -v

# Run with coverage
pip install pytest-cov
python -m pytest tests/ --cov=. --cov-report=html
```

#### JWT Authentication Tests
```bash
# Test JWT functionality
python tests/test_jwt.py

# Expected output:
# Testing JWT functionality...
# Security Secret: c2VjcmV0
# Security Issuer: emotion_recognition
# Generated test token: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9...
# Token validation: True
# Extracted username: testuser
# Extracted user ID: 12345
# ✅ JWT functionality test passed!
```

### 2. API Integration Tests

#### Manual API Testing
```bash
# Start the server
uvicorn api:app --host 0.0.0.0 --port 8000

# In another terminal, run API tests
python tests/test_api.py

# Expected output:
# Testing API endpoints...
# Health check: 200 - {'status': 'healthy', 'service': 'emotion-classifier'}
# Predict without auth: 401 - {'message': 'Malformed authorization header or missing token'}
# Predict with Bearer token: 200 - (emotion prediction result)
```

#### Generate Test JWT Token
```bash
# Generate a test token for manual testing
python tests/test_api.py token

# Output: Test JWT token: eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9...
```

### 3. Load Testing

#### Using curl for Basic Load Testing
```bash
# Health check load test
for i in {1..100}; do
  curl -s http://localhost:8000/api/health > /dev/null &
done
wait
echo "Health check load test completed"
```

#### Using Python for Emotion Prediction Testing
```python
# load_test.py
import asyncio
import aiohttp
import time

async def test_prediction(session, token, file_path):
    headers = {"Authorization": f"Bearer {token}"}
    
    with open(file_path, 'rb') as f:
        data = aiohttp.FormData()
        data.add_field('file', f, filename='test.jpg', content_type='image/jpeg')
        
        async with session.post(
            'http://localhost:8000/api/predict/',
            headers=headers,
            data=data
        ) as response:
            return await response.json()

async def load_test():
    token = "your-test-token"
    file_path = "test_image.jpg"
    
    async with aiohttp.ClientSession() as session:
        start_time = time.time()
        
        tasks = [test_prediction(session, token, file_path) for _ in range(10)]
        results = await asyncio.gather(*tasks, return_exceptions=True)
        
        end_time = time.time()
        print(f"Completed 10 requests in {end_time - start_time:.2f} seconds")

# Run load test
asyncio.run(load_test())
```

## Debugging

### 1. Logging Configuration

#### Enable Debug Logging
```python
# Add to api.py for detailed logging
import logging

logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler('debug.log'),
        logging.StreamHandler()
    ]
)
```

#### Log Analysis
```bash
# Monitor logs in real-time
tail -f debug.log

# Filter for errors
grep "ERROR" debug.log

# Filter for authentication issues
grep "JWT" debug.log
```

### 2. Common Debugging Scenarios

#### JWT Authentication Issues
```python
# Debug JWT token validation
from jwt_auth import JWTProvider
import jwt

provider = JWTProvider()
token = "your-problematic-token"

try:
    # Decode without verification to see payload
    unverified = jwt.decode(token, options={"verify_signature": False})
    print(f"Token payload: {unverified}")
    
    # Check expiration
    import datetime
    exp = datetime.datetime.fromtimestamp(unverified['exp'])
    now = datetime.datetime.now()
    print(f"Token expires: {exp}")
    print(f"Current time: {now}")
    print(f"Token valid: {exp > now}")
    
except Exception as e:
    print(f"Token decode error: {e}")
```

#### Model Loading Issues
```python
# Debug model loading
import torch
import os

# Check PyTorch installation
print(f"PyTorch version: {torch.__version__}")
print(f"CUDA available: {torch.cuda.is_available()}")
print(f"MPS available: {torch.backends.mps.is_available()}")

# Check model file
model_path = "./emotion_recognizer/models/ResEmoteNet/ResEmoteNetBS32.pth"
print(f"Model file exists: {os.path.exists(model_path)}")
print(f"Model file size: {os.path.getsize(model_path) if os.path.exists(model_path) else 'N/A'}")

# Test model loading
try:
    checkpoint = torch.load(model_path, map_location='cpu', weights_only=True)
    print("Model loaded successfully")
    print(f"Model keys: {list(checkpoint.keys())[:5]}...")  # Show first 5 keys
except Exception as e:
    print(f"Model loading error: {e}")
```

#### Video Processing Issues
```python
# Debug video processing
import cv2
import numpy as np

def debug_video_processing(file_path):
    """Debug video file processing"""
    print(f"Processing file: {file_path}")
    
    # Check file existence
    if not os.path.exists(file_path):
        print("❌ File does not exist")
        return
    
    # Try to open video
    cap = cv2.VideoCapture(file_path)
    if not cap.isOpened():
        print("❌ Cannot open video file")
        return
    
    # Get video properties
    width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    fps = cap.get(cv2.CAP_PROP_FPS)
    frame_count = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    
    print(f"✅ Video opened successfully")
    print(f"   Resolution: {width}x{height}")
    print(f"   FPS: {fps}")
    print(f"   Frame count: {frame_count}")
    
    # Test reading first frame
    ret, frame = cap.read()
    if ret:
        print(f"✅ First frame read successfully: {frame.shape}")
    else:
        print("❌ Cannot read first frame")
    
    cap.release()

# Usage
debug_video_processing("test_video.mp4")
```

### 3. Performance Profiling

#### Memory Profiling
```bash
# Install memory profiler
pip install memory-profiler

# Profile memory usage
python -m memory_profiler api.py
```

#### CPU Profiling
```python
# profile_emotion_prediction.py
import cProfile
import pstats
from emotion_recognizer import EmotionRecognizer

def profile_prediction():
    recognizer = EmotionRecognizer()
    
    # Read test video as bytes
    with open("test_video.mp4", "rb") as f:
        video_bytes = f.read()
    
    # Profile the prediction
    result = recognizer.process_video(video_bytes)
    return result

# Run profiling
cProfile.run('profile_prediction()', 'prediction_profile.stats')

# Analyze results
stats = pstats.Stats('prediction_profile.stats')
stats.sort_stats('cumulative').print_stats(20)
```

## Performance Optimization

### 1. Model Optimization

#### Device Selection
```python
# Optimize device selection in emotion_predictor.py
def get_optimal_device():
    """Select the best available device for inference"""
    if torch.cuda.is_available():
        device = torch.device("cuda")
        print(f"Using CUDA: {torch.cuda.get_device_name()}")
    elif torch.backends.mps.is_available():
        device = torch.device("mps")
        print("Using Apple Silicon MPS")
    else:
        device = torch.device("cpu")
        print("Using CPU")
    
    return device
```

#### Model Compilation (PyTorch 2.0+)
```python
# Add to EmotionRecognizer.__init__()
if hasattr(torch, 'compile'):
    self.model = torch.compile(self.model)
    print("Model compiled with PyTorch 2.0")
```

### 2. Processing Optimization

#### Batch Processing
```python
# Optimize frame processing
def process_frames_batch(self, frames, batch_size=8):
    """Process multiple frames in batches for better GPU utilization"""
    results = []
    
    for i in range(0, len(frames), batch_size):
        batch = frames[i:i+batch_size]
        batch_tensor = torch.stack([self.transform(frame) for frame in batch])
        
        with torch.no_grad():
            outputs = self.model(batch_tensor.to(self.device))
            probabilities = F.softmax(outputs, dim=1)
            
        results.extend(probabilities.cpu().numpy())
    
    return results
```

### 3. Memory Optimization

#### Cleanup and Garbage Collection
```python
import gc
import torch

def cleanup_memory():
    """Clean up GPU/CPU memory"""
    if torch.cuda.is_available():
        torch.cuda.empty_cache()
    gc.collect()

# Call after processing large videos
def process_video_with_cleanup(self, video_source):
    try:
        result = self.process_video(video_source)
        return result
    finally:
        cleanup_memory()
```

## Troubleshooting

### Common Issues and Solutions

#### 1. Model Download Failures
```bash
# Issue: Model download fails due to network issues
# Solution: Manual download and verification

# Check internet connectivity
curl -I https://huggingface.co

# Download model manually
wget -O ResEmoteNetBS32.pth "https://huggingface.co/GabrieleConte/ResEmoteNet/resolve/main/ResEmoteNetBS32.pth"

# Verify file integrity
ls -la ResEmoteNetBS32.pth
file ResEmoteNetBS32.pth
```

#### 2. OpenCV Installation Issues
```bash
# Issue: OpenCV not working properly
# Solution: Reinstall with proper backend support

pip uninstall opencv-python
pip install opencv-python-headless  # For servers without GUI

# Test OpenCV
python -c "import cv2; print(cv2.__version__); print(cv2.getBuildInformation())"
```

#### 3. JWT Token Issues
```bash
# Issue: JWT validation fails
# Solution: Check token format and secret

# Verify token structure
python -c "
import jwt
token = 'your-token-here'
decoded = jwt.decode(token, options={'verify_signature': False})
print(decoded)
"

# Check secret encoding
python -c "
import base64
secret = 'c2VjcmV0'
decoded = base64.b64decode(secret)
print(f'Decoded secret: {decoded}')
"
```

#### 4. Memory Issues
```bash
# Issue: Out of memory during processing
# Solutions:

# 1. Reduce batch size
# Edit emotion_predictor.py: evaluation_frequency = 10  # Process every 10th frame

# 2. Use CPU instead of GPU for large videos
export CUDA_VISIBLE_DEVICES=""

# 3. Monitor memory usage
htop  # Linux
Activity Monitor  # macOS
Task Manager  # Windows
```

#### 5. Port Already in Use
```bash
# Issue: Port 8000 already in use
# Solution: Find and kill process or use different port

# Find process using port 8000
lsof -i :8000  # macOS/Linux
netstat -ano | findstr :8000  # Windows

# Kill process (replace PID with actual process ID)
kill -9 <PID>

# Or use different port
uvicorn api:app --host 0.0.0.0 --port 8001
```

### Debug Mode Development

Create a debug configuration:
```python
# debug_config.py
import logging
import os

# Set debug environment
os.environ['DEBUG'] = 'true'
os.environ['LOG_LEVEL'] = 'DEBUG'

# Enhanced logging
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(funcName)s:%(lineno)d - %(message)s',
    handlers=[
        logging.FileHandler('debug.log'),
        logging.StreamHandler()
    ]
)

# Disable some verbose loggers
logging.getLogger('urllib3').setLevel(logging.WARNING)
logging.getLogger('requests').setLevel(logging.WARNING)
```

Run in debug mode:
```bash
# Import debug config before starting
python -c "import debug_config; exec(open('api.py').read())"
```

---

*This development setup guide provides comprehensive instructions for setting up, developing, testing, and debugging the Video Classifier service. For production deployment, refer to the Video Classifier Architecture documentation.*
