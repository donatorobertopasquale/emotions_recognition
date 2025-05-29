# Video Classifier API Reference

## Overview

The Video Classifier API provides emotion recognition capabilities through a FastAPI-based service. This service analyzes video files and images to detect facial expressions and classify emotions using advanced machine learning models.

## Table of Contents

- [Base URL](#base-url)
- [Authentication](#authentication)
- [Endpoints](#endpoints)
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

class EmotionClassifierClient:
    def __init__(self, base_url, jwt_token):
        self.base_url = base_url
        self.headers = {"Authorization": f"Bearer {jwt_token}"}
    
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
    
    def health_check(self):
        """Check service health"""
        url = f"{self.base_url}/api/health"
        response = requests.get(url)
        return response.json()

# Usage example
client = EmotionClassifierClient("http://localhost:8000", "your-jwt-token")
result = client.predict_emotion("sample_video.mp4")
print(f"Detected emotion: {result['emotion']} (confidence: {result['confidence']:.2f})")
```

### JavaScript SDK

```javascript
class EmotionClassifierClient {
    constructor(baseUrl, jwtToken) {
        this.baseUrl = baseUrl;
        this.headers = {
            'Authorization': `Bearer ${jwtToken}`
        };
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