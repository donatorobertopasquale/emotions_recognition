# Video Classifier API Reference

## Overview

The Video Classifier API provides emotion recognition capabilities through a FastAPI-based service. This service analyzes video files and images to detect facial expressions and classify emotions using advanced machine learning models.

## Table of Contents

- [Base URL](#base-url)
- [Authentication](#authentication)
- [Endpoints](#endpoints)
- [WebSocket Endpoints](#websocket-endpoints)
- [Data Models](#data-models)
- [Error Handling](#error-handling)
- [Rate Limiting](#rate-limiting)
- [SDK Examples](#sdk-examples)

## Base URL

```
Development: http://localhost:8000
Production: https://your-domain.com
```

## Authentication

The Video Classifier API uses JWT (JSON Web Tokens) for authentication. Tokens are validated using the same secret and issuer as the backend authentication service.

### Authentication Methods

#### 1. Bearer Token (Recommended)
```http
Authorization: Bearer <jwt-token>
```

#### 2. Cookie Authentication
```http
Cookie: access_token=<jwt-token>
```

### JWT Token Structure

```json
{
  "sub": "username",
  "userId": 12345,
  "roles": "USER",
  "iss": "emotion_recognition",
  "iat": 1640995200,
  "exp": 1641001200
}
```

### Required Claims

| Claim | Type | Description |
|-------|------|-------------|
| `sub` | string | Username |
| `userId` | integer | User identifier |
| `iss` | string | Token issuer (must be "emotion_recognition") |
| `exp` | integer | Expiration timestamp |

## Endpoints

### Health Check

Check the service health and availability.

```http
GET /api/health
```

#### Response

```json
{
  "status": "healthy",
  "service": "emotion-classifier"
}
```

**Status Codes:**
- `200 OK` - Service is healthy
- `503 Service Unavailable` - Service is down

---

### Emotion Prediction

Analyze uploaded video or image for emotion recognition.

```http
POST /api/predict/
```

#### Request

**Headers:**
- `Authorization: Bearer <jwt-token>` (required)
- `Content-Type: multipart/form-data`

**Body (multipart/form-data):**
- `file` (required): Video or image file

**Supported File Formats:**
- **Videos**: MP4, AVI, MOV, MKV
- **Images**: JPG, JPEG, PNG, BMP
- **Max File Size**: 50 MB (configurable)

#### Response

```json
{
  "emotion": "happy",
  "confidence": 0.87,
  "all_emotions": {
    "happy": 0.87,
    "surprise": 0.05,
    "sad": 0.03,
    "anger": 0.02,
    "disgust": 0.01,
    "fear": 0.01,
    "neutral": 0.01
  }
}
```

#### Response Fields

| Field | Type | Description |
|-------|------|-------------|
| `emotion` | string | Primary detected emotion |
| `confidence` | float | Confidence score (0.0-1.0) |
| `all_emotions` | object | Probability distribution for all emotions |

#### Emotion Categories

| Emotion | Description |
|---------|-------------|
| `happy` | Joy, satisfaction, contentment |
| `surprise` | Shock, amazement, astonishment |
| `sad` | Sorrow, melancholy, dejection |
| `anger` | Rage, frustration, annoyance |
| `disgust` | Revulsion, distaste, aversion |
| `fear` | Anxiety, worry, apprehension |
| `neutral` | Calm, composed, unexpressive |

#### Status Codes

- `200 OK` - Successful emotion prediction
- `401 Unauthorized` - Missing or invalid JWT token
- `413 Payload Too Large` - File size exceeds limit
- `422 Unprocessable Entity` - Invalid file format or corrupted file
- `500 Internal Server Error` - Model inference error

#### Example Request

```bash
curl -X POST "http://localhost:8000/api/predict/" \
  -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9..." \
  -F "file=@sample_video.mp4"
```

#### Example Response

```json
{
  "emotion": "happy",
  "confidence": 0.92,
  "all_emotions": {
    "happy": 0.92,
    "surprise": 0.03,
    "neutral": 0.02,
    "sad": 0.01,
    "anger": 0.01,
    "disgust": 0.01,
    "fear": 0.00
  }
}
```

---

### Active Connections Count

Get the current number of active WebSocket connections.

```http
GET /api/connections/count
```

#### Response

```json
{
  "active_connections": 3,
  "timestamp": 1748610712.473023
}
```

**Status Codes:**
- `200 OK` - Successful response
- `503 Service Unavailable` - Service is down

---

## WebSocket Endpoints

### Real-time Emotion Classification

Establish a WebSocket connection for real-time text-based emotion classification and active user tracking.

```
WS /api/ws
```

#### Authentication

WebSocket connections require JWT authentication via cookies:

```javascript
// Set JWT token as cookie before connecting
document.cookie = `access_token=${jwtToken}; path=/`;
// or
document.cookie = `token=${jwtToken}; path=/`;
```

#### Connection Process

1. **Authentication**: JWT token must be present in cookies
2. **Connection**: WebSocket connection is established
3. **Active User Tracking**: Connection count is broadcast to all clients
4. **Message Exchange**: Send text for emotion classification
5. **Cleanup**: Connection is removed and count updated on disconnect

#### Message Types

The WebSocket endpoint supports different message types:

##### 1. Emotion Classification Input
Send plain text for emotion analysis:

```javascript
ws.send("I'm feeling great today!");
```

##### 2. Emotion Classification Response

```json
{
  "type": "emotion_classification",
  "label": "joy",
  "score": 0.9887
}
```

##### 3. Connection Update Messages

Automatically sent when users connect/disconnect:

```json
{
  "type": "connection_update",
  "active_connections": 5,
  "timestamp": 1748610712.473023
}
```

##### 4. Error Messages

```json
{
  "type": "error",
  "message": "Classification failed"
}
```

#### WebSocket Connection Example

```javascript
// Set authentication cookie
document.cookie = `access_token=${jwtToken}; path=/`;

// Connect to WebSocket
const ws = new WebSocket('ws://localhost:8000/api/ws');

ws.onopen = () => {
    console.log('Connected to emotion classifier');
};

ws.onmessage = (event) => {
    const data = JSON.parse(event.data);
    
    switch (data.type) {
        case 'emotion_classification':
            console.log(`Emotion: ${data.label} (${data.score})`);
            break;
            
        case 'connection_update':
            console.log(`Active users: ${data.active_connections}`);
            break;
            
        case 'error':
            console.error(`Error: ${data.message}`);
            break;
    }
};

// Send text for classification
ws.send("This is an amazing day!");
```

#### WebSocket Error Codes

- **4001**: Authentication failed (missing or invalid JWT token)
- **1000**: Normal closure
- **1006**: Abnormal closure (connection lost)

#### Features

- **Real-time Processing**: Instant emotion classification from text
- **Active User Tracking**: Live count of connected users
- **Automatic Reconnection**: Client-side reconnection support
- **JWT Authentication**: Secure connection with token validation
- **Broadcasting**: Connection updates sent to all connected clients

## Data Models

### EmotionResponse

```python
class EmotionResponse(BaseModel):
    emotion: str
    confidence: float
    all_emotions: Dict[str, float]
```

**Field Constraints:**
- `emotion`: One of the 7 supported emotion categories
- `confidence`: Float between 0.0 and 1.0
- `all_emotions`: Dictionary with all 7 emotions as keys, probabilities sum to 1.0

### Error Response

```json
{
  "message": "Error description",
  "detail": "Detailed error information"
}
```

## Error Handling

### Authentication Errors

#### 401 Unauthorized - Missing Token
```json
{
  "message": "Malformed authorization header or missing token"
}
```

#### 401 Unauthorized - Invalid Token
```json
{
  "message": "Invalid JWT token"
}
```

#### 401 Unauthorized - Expired Token
```json
{
  "message": "Token has expired"
}
```

### File Processing Errors

#### 422 Unprocessable Entity - Invalid File
```json
{
  "message": "Could not open video source"
}
```

#### 413 Payload Too Large
```json
{
  "message": "File size exceeds maximum allowed size"
}
```

### Server Errors

#### 500 Internal Server Error
```json
{
  "message": "Emotion prediction failed",
  "detail": "Model inference error"
}
```

## Rate Limiting

Currently, no rate limiting is implemented at the API level. Consider implementing rate limiting based on your deployment requirements:

- **Recommended**: 100 requests per minute per user
- **Burst**: 10 requests per second
- **File size**: 50 MB maximum

## SDK Examples

### Python SDK

```python
import requests
import json
import websocket
import threading
from typing import Callable, Optional

class EmotionClassifierClient:
    def __init__(self, base_url, jwt_token):
        self.base_url = base_url
        self.jwt_token = jwt_token
        self.headers = {"Authorization": f"Bearer {jwt_token}"}
        self.ws = None
        self.ws_thread = None
        self.message_callback = None
        self.connection_callback = None
    
    def predict_emotion(self, file_path):
        """Predict emotion from video/image file"""
        url = f"{self.base_url}/api/predict/"
        
        with open(file_path, 'rb') as file:
            files = {"file": file}
            response = requests.post(url, headers=self.headers, files=files)
            
        if response.status_code == 200:
            return response.json()
        else:
            response.raise_for_status()
    
    def get_active_connections(self):
        """Get current number of active WebSocket connections"""
        url = f"{self.base_url}/api/connections/count"
        response = requests.get(url)
        return response.json()
    
    def health_check(self):
        """Check service health"""
        url = f"{self.base_url}/api/health"
        response = requests.get(url)
        return response.json()
    
    def connect_websocket(self, 
                         message_callback: Optional[Callable] = None,
                         connection_callback: Optional[Callable] = None):
        """Connect to WebSocket for real-time emotion classification"""
        self.message_callback = message_callback
        self.connection_callback = connection_callback
        
        # Convert HTTP URL to WebSocket URL
        ws_url = self.base_url.replace('http://', 'ws://').replace('https://', 'wss://')
        ws_url = f"{ws_url}/api/ws"
        
        # Set up WebSocket with cookie authentication
        cookie = f"access_token={self.jwt_token}"
        
        def on_message(ws, message):
            try:
                data = json.loads(message)
                if self.message_callback:
                    self.message_callback(data)
                else:
                    print(f"Received: {data}")
            except json.JSONDecodeError:
                print(f"Invalid JSON received: {message}")
        
        def on_error(ws, error):
            print(f"WebSocket error: {error}")
        
        def on_close(ws, close_status_code, close_msg):
            print("WebSocket connection closed")
        
        def on_open(ws):
            print("WebSocket connection established")
            if self.connection_callback:
                self.connection_callback("connected")
        
        # Create WebSocket connection
        self.ws = websocket.WebSocketApp(
            ws_url,
            header=[f"Cookie: {cookie}"],
            on_open=on_open,
            on_message=on_message,
            on_error=on_error,
            on_close=on_close
        )
        
        # Run WebSocket in separate thread
        self.ws_thread = threading.Thread(target=self.ws.run_forever)
        self.ws_thread.daemon = True
        self.ws_thread.start()
    
    def send_text(self, text: str):
        """Send text for emotion classification via WebSocket"""
        if self.ws and self.ws.sock:
            self.ws.send(text)
        else:
            raise ConnectionError("WebSocket not connected")
    
    def disconnect_websocket(self):
        """Disconnect from WebSocket"""
        if self.ws:
            self.ws.close()
            self.ws = None
        if self.ws_thread:
            self.ws_thread.join(timeout=1)
            self.ws_thread = None

# Usage example
def on_emotion_result(data):
    if data.get('type') == 'emotion_classification':
        print(f"Emotion: {data['label']} (confidence: {data['score']:.2f})")
    elif data.get('type') == 'connection_update':
        print(f"Active users: {data['active_connections']}")

def on_connection_status(status):
    print(f"Connection status: {status}")

client = EmotionClassifierClient("http://localhost:8000", "your-jwt-token")

# File-based prediction
result = client.predict_emotion("sample_video.mp4")
print(f"Detected emotion: {result['emotion']} (confidence: {result['confidence']:.2f})")

# Real-time WebSocket prediction
client.connect_websocket(on_emotion_result, on_connection_status)
client.send_text("I'm feeling great today!")

# Get active connections
connections = client.get_active_connections()
print(f"Current active connections: {connections['active_connections']}")
```
```

### JavaScript SDK

```javascript
class EmotionClassifierClient {
    constructor(baseUrl, jwtToken) {
        this.baseUrl = baseUrl;
        this.jwtToken = jwtToken;
        this.headers = {
            'Authorization': `Bearer ${jwtToken}`
        };
        this.ws = null;
        this.messageCallback = null;
        this.connectionCallback = null;
    }

    async predictEmotion(file) {
        const formData = new FormData();
        formData.append('file', file);

        const response = await fetch(`${this.baseUrl}/api/predict/`, {
            method: 'POST',
            headers: this.headers,
            body: formData
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
    }

    async getActiveConnections() {
        const response = await fetch(`${this.baseUrl}/api/connections/count`);
        return await response.json();
    }

    async healthCheck() {
        const response = await fetch(`${this.baseUrl}/api/health`);
        return await response.json();
    }

    connectWebSocket(messageCallback = null, connectionCallback = null) {
        this.messageCallback = messageCallback;
        this.connectionCallback = connectionCallback;

        // Set JWT token as cookie for authentication
        document.cookie = `access_token=${this.jwtToken}; path=/; SameSite=Lax`;

        // Convert HTTP URL to WebSocket URL
        const wsUrl = this.baseUrl
            .replace('http://', 'ws://')
            .replace('https://', 'wss://') + '/api/ws';

        this.ws = new WebSocket(wsUrl);

        this.ws.onopen = () => {
            console.log('WebSocket connected');
            if (this.connectionCallback) {
                this.connectionCallback('connected');
            }
        };

        this.ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                if (this.messageCallback) {
                    this.messageCallback(data);
                } else {
                    console.log('Received:', data);
                }
            } catch (error) {
                console.error('Error parsing WebSocket message:', error);
            }
        };

        this.ws.onclose = (event) => {
            console.log('WebSocket disconnected:', event.code, event.reason);
            if (this.connectionCallback) {
                this.connectionCallback('disconnected');
            }
        };

        this.ws.onerror = (error) => {
            console.error('WebSocket error:', error);
            if (this.connectionCallback) {
                this.connectionCallback('error');
            }
        };
    }

    sendText(text) {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
            this.ws.send(text);
        } else {
            throw new Error('WebSocket not connected');
        }
    }

    disconnectWebSocket() {
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
    }
}

// Usage example
const client = new EmotionClassifierClient('http://localhost:8000', 'your-jwt-token');

// File-based prediction
document.getElementById('file-input').addEventListener('change', async (event) => {
    const file = event.target.files[0];
    if (file) {
        try {
            const result = await client.predictEmotion(file);
            console.log(`Detected emotion: ${result.emotion} (confidence: ${result.confidence.toFixed(2)})`);
        } catch (error) {
            console.error('Prediction failed:', error);
        }
    }
});

// Real-time WebSocket prediction
function onMessage(data) {
    switch (data.type) {
        case 'emotion_classification':
            console.log(`Emotion: ${data.label} (confidence: ${data.score.toFixed(2)})`);
            break;
        case 'connection_update':
            console.log(`Active users: ${data.active_connections}`);
            break;
        case 'error':
            console.error(`Error: ${data.message}`);
            break;
    }
}

function onConnection(status) {
    console.log(`Connection status: ${status}`);
}

// Connect to WebSocket
client.connectWebSocket(onMessage, onConnection);

// Send text for classification
setTimeout(() => {
    client.sendText("I'm feeling amazing today!");
}, 1000);

// Get active connections
client.getActiveConnections().then(data => {
    console.log(`Current active connections: ${data.active_connections}`);
});
```

        return await response.json();
    }

    async healthCheck() {
        const response = await fetch(`${this.baseUrl}/api/health`);
        return await response.json();
    }
}

// Usage example
const client = new EmotionClassifierClient('http://localhost:8000', 'your-jwt-token');

// For file input
document.getElementById('file-input').addEventListener('change', async (event) => {
    const file = event.target.files[0];
    if (file) {
        try {
            const result = await client.predictEmotion(file);
            console.log(`Detected emotion: ${result.emotion} (confidence: ${result.confidence.toFixed(2)})`);
        } catch (error) {
            console.error('Prediction failed:', error);
        }
    }
});
```

### cURL Examples

#### Health Check
```bash
curl -X GET "http://localhost:8000/api/health"
```

#### Emotion Prediction with Bearer Token
```bash
curl -X POST "http://localhost:8000/api/predict/" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "file=@path/to/your/video.mp4"
```

#### Emotion Prediction with Cookie
```bash
curl -X POST "http://localhost:8000/api/predict/" \
  -H "Cookie: access_token=YOUR_JWT_TOKEN" \
  -F "file=@path/to/your/image.jpg"
```

### Response Processing

```python
# Process emotion prediction results
def process_emotion_result(result):
    primary_emotion = result['emotion']
    confidence = result['confidence']
    all_emotions = result['all_emotions']
    
    print(f"Primary Emotion: {primary_emotion}")
    print(f"Confidence: {confidence:.2%}")
    print("\nAll Emotions:")
    
    # Sort emotions by probability
    sorted_emotions = sorted(all_emotions.items(), key=lambda x: x[1], reverse=True)
    
    for emotion, probability in sorted_emotions:
        print(f"  {emotion}: {probability:.2%}")
    
    # Check if confidence is high enough
    if confidence > 0.7:
        print(f"\nHigh confidence detection: {primary_emotion}")
    elif confidence > 0.5:
        print(f"\nModerate confidence detection: {primary_emotion}")
    else:
        print(f"\nLow confidence detection, consider manual review")

# Usage
result = client.predict_emotion("test_video.mp4")
process_emotion_result(result)
```

## Interactive API Documentation

The Video Classifier API provides automatic interactive documentation:

- **Swagger UI**: `http://localhost:8000/docs`
- **ReDoc**: `http://localhost:8000/redoc`
- **OpenAPI Spec**: `http://localhost:8000/openapi.json`

These endpoints provide:
- Interactive API testing
- Request/response examples
- Model schema documentation
- Authentication testing interface

---

*For implementation details and deployment instructions, refer to the Video Classifier Architecture documentation.*