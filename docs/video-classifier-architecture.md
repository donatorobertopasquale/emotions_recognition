# Video Classifier Service Architecture

## Overview

The Video Classifier service is a FastAPI-based microservice that provides comprehensive emotion recognition capabilities through multiple channels:

- **File-based Processing**: Real-time emotion recognition from video files and image uploads using state-of-the-art computer vision and deep learning techniques to analyze facial expressions
- **Text-based Processing**: Real-time emotion classification from text input using transformer-based natural language processing models  
- **WebSocket Communication**: Live bidirectional communication for real-time text emotion analysis and active user tracking
- **Connection Monitoring**: Real-time tracking and broadcasting of active WebSocket connections to all connected clients

The service implements advanced ML architectures including custom ResNet-based emotion recognition models and HuggingFace transformer pipelines for comprehensive emotion analysis across different media types.

## Table of Contents

- [System Architecture](#system-architecture)
- [Technology Stack](#technology-stack)
- [Core Components](#core-components)
- [ML Model Architecture](#ml-model-architecture)
- [Authentication & Security](#authentication--security)
- [API Design](#api-design)
- [Configuration Management](#configuration-management)
- [Deployment Strategy](#deployment-strategy)
- [Performance Optimization](#performance-optimization)

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Video Classifier Service                 │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │   FastAPI   │  │    JWT      │  │   CORS Middleware   │ │
│  │   Server    │  │ Middleware  │  │                     │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │ REST API    │  │  WebSocket  │  │   Connection        │ │
│  │ Endpoints   │  │  Endpoints  │  │   Tracking          │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │   Config    │  │ JWT Auth    │  │  Text Emotion       │ │
│  │  Manager    │  │  Filter     │  │  Classification     │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│                 Emotion Recognition Engine                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │ Face        │  │ ResEmoteNet │  │  Emotion Predictor  │ │
│  │ Detection   │  │   Model     │  │                     │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │   OpenCV    │  │   PyTorch   │  │  Transformers       │ │
│  │ Computer    │  │  Deep       │  │  Text Processing    │ │
│  │   Vision    │  │ Learning    │  │                     │ │
│  └─────────────┘  └─────────────┘  └─────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
```

### Component Interaction Flow

#### REST API Flow
```
Client Request → CORS Middleware → JWT Middleware → API Endpoint
                                                         ↓
                                                   Video/Image Upload
                                                         ↓
                                               Emotion Recognizer
                                                         ↓
                                    Face Detection (OpenCV) → ML Model (ResEmoteNet)
                                                         ↓
                                               Emotion Classification
                                                         ↓
                                                Response with Results
```

#### WebSocket Flow
```
Client WebSocket → Cookie Auth → JWT Validation → WebSocket Accept
                                                         ↓
                                           Connection Tracking (Add to Set)
                                                         ↓
                                        Broadcast Connection Count to All Clients
                                                         ↓
                                              Text Message Received
                                                         ↓
                                    Text Emotion Classification (Transformers)
                                                         ↓
                                           Send Classification Result
                                                         ↓
                                  Connection Close → Remove from Set → Broadcast Update
```

## Technology Stack

### Core Technologies

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| **Web Framework** | FastAPI | Latest | Async web framework with automatic API documentation |
| **ML Framework** | PyTorch | Latest | Deep learning model inference |
| **Computer Vision** | OpenCV | 4.9.0.80 | Face detection and image processing |
| **Image Processing** | Pillow | 10.3.0 | Image manipulation and format handling |
| **Text Processing** | Transformers | Latest | Text-based emotion classification |
| **Authentication** | PyJWT | Latest | JWT token validation and processing |
| **HTTP Server** | Uvicorn | Latest | ASGI server for FastAPI |
| **WebSocket Support** | FastAPI WebSocket | Latest | Real-time bidirectional communication |

### Development Dependencies

| Component | Technology | Version | Purpose |
|-----------|------------|---------|---------|
| **Data Science** | NumPy | 1.26.4 | Numerical computations |
| **Data Analysis** | Pandas | 2.2.2 | Data manipulation |
| **Visualization** | Matplotlib | 3.8.3 | Plotting and visualization |
| **Progress Tracking** | tqdm | 4.66.1 | Progress bars |
| **Face Recognition** | dlib | ≥19.24.2 | Advanced face detection |

## Core Components

### 1. FastAPI Application (`api.py`)

The main application entry point that orchestrates all components:

```python
# Key Features:
- Async request handling
- Automatic OpenAPI documentation
- CORS middleware configuration
- JWT authentication middleware
- Health check endpoints
- File upload handling
```

**Responsibilities:**
- HTTP request routing
- Middleware configuration
- Response formatting
- Error handling
- Logging coordination

### 2. Emotion Recognition Engine (`emotion_recognizer/`)

The core ML pipeline for emotion detection:

#### `emotion_predictor.py`
- **EmotionRecognizer Class**: Main interface for emotion detection
- **Video Processing**: Frame-by-frame emotion analysis
- **Face Detection**: OpenCV-based face localization
- **Batch Processing**: Efficient processing of video streams

#### `ResEmoteNet.py`
- **Custom CNN Architecture**: ResNet-based emotion classification model
- **SE-Block Integration**: Squeeze-and-Excitation attention mechanism
- **Residual Connections**: Skip connections for improved gradient flow

### 3. WebSocket Real-time Communication (`api.py`)

Real-time text emotion classification and user tracking:

#### **Connection Management**
- **Active Connection Tracking**: Global set to track WebSocket connections
- **Connection Broadcasting**: Real-time updates to all connected clients
- **Automatic Cleanup**: Proper disconnection handling and resource cleanup

#### **Text Emotion Classification**
- **Transformers Pipeline**: HuggingFace emotion classification model
- **Real-time Processing**: Instant emotion analysis from text input
- **Message Type System**: Structured responses with type identification

#### **Features**
- **JWT Cookie Authentication**: Secure WebSocket connections
- **Active User Count**: Real-time tracking and broadcasting of connected users
- **Message Broadcasting**: Push notifications to all connected clients
- **Error Handling**: Graceful error responses and connection management

### 4. Authentication System

#### `jwt_middleware.py`
```python
class JWTAuthMiddleware(BaseHTTPMiddleware):
    # Features:
    - Path-based authentication exclusion
    - Request state management
    - Error handling with proper HTTP status codes
    - Performance logging
```

#### `jwt_auth.py`
```python
class JWTProvider:
    # Capabilities:
    - Base64 secret key decoding
    - Multi-algorithm support (HS512)
    - Claim extraction and validation
    - Token expiration checking

class JWTAuthFilter:
    # Features:
    - Multiple token sources (headers, cookies)
    - Request validation
    - User information extraction
```

#### **WebSocket Authentication**
- **Cookie-based Authentication**: JWT tokens read from `access_token` or `token` cookies
- **Pre-connection Validation**: Token validation before WebSocket acceptance
- **Connection Rejection**: Proper error codes (4001) for authentication failures
- **User Context**: Username and user ID extraction from validated tokens

### 5. Configuration Management (`config.py`)

Environment-based configuration system:
- JWT secret and issuer management
- Environment file loading
- Runtime configuration validation

## ML Model Architecture

### ResEmoteNet Architecture

```
Input (64x64x3) → Conv Layers → Residual Blocks → Global Pool → FC Layers → 7 Emotions
```

#### Network Components:

1. **Initial Convolution Layers**
   - 3 convolutional layers (64→128→256 channels)
   - Batch normalization and ReLU activation
   - Max pooling for spatial dimension reduction
   - Dropout for regularization

2. **SE-Block (Squeeze-and-Excitation)**
   - Channel attention mechanism
   - Adaptive feature recalibration
   - Improved feature representation

3. **Residual Blocks**
   - 3 residual blocks (256→512→1024→2048 channels)
   - Skip connections for gradient flow
   - Stride-based downsampling

4. **Classification Head**
   - Global adaptive average pooling
   - 4 fully connected layers (2048→1024→512→256→7)
   - Progressive dropout (0.2, 0.5)
   - Final softmax activation

### Emotion Classes

| Index | Emotion | Description |
|-------|---------|-------------|
| 0 | Happy | Joy, satisfaction, contentment |
| 1 | Surprise | Shock, amazement, astonishment |
| 2 | Sad | Sorrow, melancholy, dejection |
| 3 | Anger | Rage, frustration, annoyance |
| 4 | Disgust | Revulsion, distaste, aversion |
| 5 | Fear | Anxiety, worry, apprehension |
| 6 | Neutral | Calm, composed, unexpressive |

### Model Performance Features

- **Neutral Penalty**: 0.7x weight reduction for neutral classification
- **Softmax Normalization**: Probabilistic output distribution
- **Confidence Scoring**: Maximum probability as confidence measure
- **Frame Aggregation**: Temporal emotion averaging across video frames

## Authentication & Security

### JWT Token Validation

```python
# Token Structure
{
    "sub": "username",           # Subject (username)
    "userId": 12345,            # User identifier
    "roles": "USER",            # User roles
    "iss": "emotion_recognition", # Issuer
    "iat": 1640995200,          # Issued at
    "exp": 1641001200           # Expiration
}
```

### Security Features

1. **Base64 Secret Decoding**: Secure key handling
2. **Algorithm Specification**: HS512 for enhanced security
3. **Claim Validation**: Required field verification
4. **Expiration Checking**: Automatic token expiry
5. **Issuer Verification**: Origin validation
6. **Multiple Token Sources**: Headers and cookies support

### Protected Endpoints

- `/api/predict/` - Requires valid JWT token
- `/api/health` - Public endpoint (no authentication)
- `/docs`, `/redoc` - Documentation (excluded from authentication)

## API Design

### Endpoint Architecture

```python
# Health Check
GET /api/health
- No authentication required
- Returns service status
- Used for load balancer health checks

# Active Connections Count
GET /api/connections/count
- No authentication required
- Returns current WebSocket connection count
- Used for monitoring active users

# Emotion Prediction (File-based)
POST /api/predict/
- JWT authentication required
- File upload (multipart/form-data)
- Returns emotion classification results

# Real-time Emotion Classification (WebSocket)
WS /api/ws
- JWT cookie authentication required
- Real-time text emotion classification
- Active user tracking and broadcasting
- Bidirectional communication
```

### WebSocket Message Types

```python
# Emotion Classification Response
{
    "type": "emotion_classification",
    "label": "joy", 
    "score": 0.9887
}

# Connection Update Broadcast
{
    "type": "connection_update",
    "active_connections": 5,
    "timestamp": 1748610712.473023
}

# Error Response
{
    "type": "error",
    "message": "Classification failed"
}
```

### Response Models

```python
class EmotionResponse(BaseModel):
    emotion: str                    # Primary detected emotion
    confidence: float              # Confidence score (0.0-1.0)
    all_emotions: Dict[str, float] # All emotion probabilities
```

### Error Handling

- **401 Unauthorized**: Missing or invalid JWT token
- **422 Unprocessable Entity**: Invalid file format or corrupted upload
- **500 Internal Server Error**: Model inference or processing errors

## Configuration Management

### Environment Variables

```bash
# JWT Configuration
SECURITY_SECRET=<base64-encoded-secret>    # JWT signing secret
SECURITY_ISSUER=emotion_recognition        # JWT issuer identifier

# Server Configuration
HOST=0.0.0.0                              # Server bind address
PORT=8000                                 # Server port
```

### Configuration Loading

```python
# Hierarchical configuration loading:
1. Environment variables (highest priority)
2. .env file
3. Default values (fallback)
```

## Deployment Strategy

### Docker Containerization

#### Multi-Stage Build Process

```dockerfile
# Stage 1: Build environment
- Python 3.11 slim base
- System dependencies installation
- ML model downloading
- Virtual environment creation

# Stage 2: Production environment
- Runtime dependencies only
- Non-root user creation
- Security hardening
- Optimized for production
```

#### Container Features

- **Model Caching**: Pre-downloaded models in container
- **Dependency Optimization**: Minimal runtime footprint
- **Security**: Non-root user execution
- **Performance**: OpenGL support for OpenCV

### Kubernetes Deployment

```yaml
# Recommended deployment configuration:
- Replicas: 2-5 (based on load)
- CPU: 1-2 cores per replica
- Memory: 2-4 GB per replica (model loading requirement)
- GPU: Optional for enhanced inference speed
```

## Performance Optimization

### Model Optimization

1. **Device Selection**: Automatic GPU/MPS/CPU detection
2. **Batch Processing**: Efficient frame processing
3. **Model Caching**: Singleton pattern for model loading
4. **Memory Management**: Proper tensor cleanup

### Processing Optimizations

1. **Frame Sampling**: Configurable evaluation frequency (default: every 5th frame)
2. **Face Detection Caching**: Reuse face coordinates across frames
3. **Async Processing**: Non-blocking I/O operations
4. **Streaming Support**: Memory-efficient video processing

### Monitoring & Logging

```python
# Structured logging with:
- Request/response timing
- Authentication success/failure
- Model inference metrics
- Error tracking and debugging
```

### Scalability Considerations

1. **Stateless Design**: No session state storage
2. **Horizontal Scaling**: Multiple replica support
3. **Load Balancing**: Health check endpoint for load balancers
4. **Resource Management**: Configurable processing parameters

---

*This architecture documentation provides a comprehensive overview of the Video Classifier service design, implementation, and deployment strategies. For specific implementation details, refer to the API Reference and Development Setup guides.*