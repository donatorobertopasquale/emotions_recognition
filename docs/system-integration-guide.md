# System Integration Guide

## Overview

This guide explains how the Backend service and Video Classifier service integrate to provide a complete emotion recognition system. It covers the authentication flow, data exchange patterns, and deployment strategies for the full system.

## Table of Contents

- [System Overview](#system-overview)
- [Authentication Integration](#authentication-integration)
- [Service Communication](#service-communication)
- [Data Flow](#data-flow)
- [Deployment Strategies](#deployment-strategies)
- [Configuration Management](#configuration-management)
- [Monitoring & Logging](#monitoring--logging)
- [Troubleshooting Integration Issues](#troubleshooting-integration-issues)

## System Overview

### Architecture Components

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Application                       │
└─────────────────────────────────────────────────────────────────┘
                                  │
                         HTTP Requests (JWT)
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Load Balancer / API Gateway               │
└─────────────────────────────────────────────────────────────────┘
                    │                           │
            Authentication &               Emotion Analysis
            Data Management                      │
                    │                           │
                    ▼                           ▼
┌──────────────────────────────┐    ┌──────────────────────────────┐
│      Backend Service         │    │   Video Classifier Service   │
│    (Spring Boot - :8080)     │    │     (FastAPI - :8000)        │
├──────────────────────────────┤    ├──────────────────────────────┤
│ • User Authentication        │    │ • JWT Token Validation       │
│ • Session Management         │    │ • Video/Image Processing     │
│ • Data Storage/Retrieval     │    │ • Emotion Recognition        │
│ • Azure Blob Integration     │    │ • ML Model Inference         │
└──────────────────────────────┘    └──────────────────────────────┘
                    │                           │
                    ▼                           ▼
┌──────────────────────────────┐    ┌──────────────────────────────┐
│      PostgreSQL Database     │    │    ResEmoteNet ML Model      │
│     (User & Reaction Data)   │    │   (Emotion Classification)   │
└──────────────────────────────┘    └──────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Azure Blob Storage                          │
│                   (Image Storage)                              │
└─────────────────────────────────────────────────────────────────┘
```

### Service Responsibilities

#### Backend Service (Spring Boot)
- **User Management**: Registration, authentication, profile management
- **Session Management**: JWT token issuance and validation
- **Data Persistence**: User data, reaction storage, session tracking
- **File Management**: Image upload/download via Azure Blob Storage
- **Business Logic**: User workflows, data validation, authorization

#### Video Classifier Service (FastAPI)
- **Authentication Validation**: JWT token verification (shared secret)
- **Media Processing**: Video and image file handling
- **Computer Vision**: Face detection and preprocessing
- **ML Inference**: Emotion classification using ResEmoteNet
- **Result Processing**: Confidence scoring and response formatting

## Authentication Integration

### Shared JWT Configuration

Both services must use identical JWT configuration for seamless authentication:

#### Backend Service (`application.properties`)
```properties
# JWT Configuration
security.secret=c2VjcmV0  # Base64 encoded secret
security.issuer=emotion_recognition
security.expiration=3600000  # 1 hour in milliseconds
```

#### Video Classifier Service (`.env`)
```bash
# JWT Configuration (MUST MATCH BACKEND)
SECURITY_SECRET=c2VjcmV0  # Same base64 encoded secret
SECURITY_ISSUER=emotion_recognition
```

### Authentication Flow

```
1. Client → Backend: POST /api/login {username, password}
2. Backend → Database: Validate credentials
3. Backend → Client: JWT token {token, expiration}
4. Client → Video Classifier: POST /api/predict/ (Bearer token)
5. Video Classifier → JWT Validation: Verify token signature & claims
6. Video Classifier → ML Pipeline: Process video/image
7. Video Classifier → Client: Emotion results
```

### Token Structure

```json
{
  "sub": "username",           // Subject (username)
  "userId": 12345,            // User ID from backend database
  "roles": "USER",            // User roles/permissions
  "iss": "emotion_recognition", // Issuer (must match)
  "iat": 1640995200,          // Issued at timestamp
  "exp": 1641001200           // Expiration timestamp
}
```

## Service Communication

### Direct Client Integration

The recommended architecture has clients communicate directly with both services:

```javascript
// Client-side integration example
class EmotionRecognitionClient {
    constructor(backendUrl, classifierUrl) {
        this.backendUrl = backendUrl;
        this.classifierUrl = classifierUrl;
        this.token = null;
    }

    // Step 1: Authenticate with backend
    async login(username, password) {
        const response = await fetch(`${this.backendUrl}/api/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });
        
        const data = await response.json();
        this.token = data.token;
        return data;
    }

    // Step 2: Analyze emotion with classifier
    async analyzeEmotion(videoFile) {
        const formData = new FormData();
        formData.append('file', videoFile);

        const response = await fetch(`${this.classifierUrl}/api/predict/`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${this.token}` },
            body: formData
        });

        return await response.json();
    }

    // Step 3: Store results in backend
    async storeReaction(imageId, emotion, confidence) {
        const response = await fetch(`${this.backendUrl}/api/register-result`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${this.token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                imageId,
                emotion,
                confidence,
                timestamp: new Date().toISOString()
            })
        });

        return await response.json();
    }
}
```

### Service-to-Service Communication (Optional)

For advanced use cases, the backend can communicate with the video classifier:

```java
// Backend service calling video classifier
@Service
public class EmotionAnalysisService {
    
    @Value("${video.classifier.url}")
    private String classifierUrl;
    
    @Autowired
    private RestTemplate restTemplate;
    
    public EmotionResult analyzeEmotion(String userToken, MultipartFile videoFile) {
        // Prepare request
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        
        // Create multipart request
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", videoFile.getResource());
        
        HttpEntity<MultiValueMap<String, Object>> request = 
            new HttpEntity<>(body, headers);
        
        // Call video classifier
        ResponseEntity<EmotionResponse> response = restTemplate.postForEntity(
            classifierUrl + "/api/predict/",
            request,
            EmotionResponse.class
        );
        
        return response.getBody();
    }
}
```

## Data Flow

### Complete User Journey

```
1. User Registration/Login
   Client → Backend: User credentials
   Backend → Database: Store/validate user
   Backend → Client: JWT token

2. Video Upload & Analysis
   Client → Video Classifier: Video file + JWT token
   Video Classifier: Validate JWT token
   Video Classifier: Extract frames and detect faces
   Video Classifier: Run ML inference on faces
   Video Classifier → Client: Emotion results

3. Result Storage
   Client → Backend: Emotion results + JWT token
   Backend: Validate JWT token and user
   Backend → Database: Store reaction data
   Backend → Client: Confirmation

4. Data Retrieval
   Client → Backend: Request user history + JWT token
   Backend → Database: Query user reactions
   Backend → Azure Storage: Retrieve associated images
   Backend → Client: Historical data
```

### Data Models Integration

#### Backend Models
```java
// User entity
@Entity
public class UserEntity {
    private Long id;
    private String username;
    private String email;
    // ... other fields
}

// Reaction entity
@Entity
public class ReactionsEntity {
    private Long id;
    private Long userId;  // Links to UserEntity
    private String emotion;
    private Double confidence;
    private String imageUrl;
    private LocalDateTime timestamp;
    // ... other fields
}
```

#### Video Classifier Models
```python
# Response model
class EmotionResponse(BaseModel):
    emotion: str                    # Primary emotion
    confidence: float              # Confidence score
    all_emotions: Dict[str, float] # All emotion probabilities

# Request context (from JWT)
class UserInfo:
    username: str
    user_id: int
    token: str
```

## Deployment Strategies

### Docker Compose Deployment

```yaml
# docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: emotion_recognition
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/emotion_recognition
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: password
      SECURITY_SECRET: c2VjcmV0
      SECURITY_ISSUER: emotion_recognition
      AZURE_STORAGE_CONNECTION_STRING: ${AZURE_STORAGE_CONNECTION_STRING}
    depends_on:
      - postgres

  video-classifier:
    build: ./video_classifier
    ports:
      - "8000:8000"
    environment:
      SECURITY_SECRET: c2VjcmV0  # MUST MATCH BACKEND
      SECURITY_ISSUER: emotion_recognition
    volumes:
      - model_cache:/app/emotion_recognizer/models

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
    depends_on:
      - backend
      - video-classifier

volumes:
  postgres_data:
  model_cache:
```

### Kubernetes Deployment

```yaml
# backend-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: backend
  template:
    metadata:
      labels:
        app: backend
    spec:
      containers:
      - name: backend
        image: emotion-recognition/backend:latest
        ports:
        - containerPort: 8080
        env:
        - name: SECURITY_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: secret
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:postgresql://postgres-service:5432/emotion_recognition"

---
# video-classifier-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: video-classifier-service
spec:
  replicas: 2
  selector:
    matchLabels:
      app: video-classifier
  template:
    metadata:
      labels:
        app: video-classifier
    spec:
      containers:
      - name: video-classifier
        image: emotion-recognition/video-classifier:latest
        ports:
        - containerPort: 8000
        env:
        - name: SECURITY_SECRET
          valueFrom:
            secretKeyRef:
              name: jwt-secret
              key: secret
        resources:
          requests:
            memory: "2Gi"
            cpu: "1"
          limits:
            memory: "4Gi"
            cpu: "2"
```

## Configuration Management

### Environment-Specific Configuration

#### Development
```bash
# .env.development
# Backend
SPRING_PROFILES_ACTIVE=development
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/emotion_recognition_dev
SECURITY_SECRET=c2VjcmV0
LOG_LEVEL=DEBUG

# Video Classifier
SECURITY_SECRET=c2VjcmV0
DEBUG=true
LOG_LEVEL=DEBUG
```

#### Production
```bash
# .env.production
# Backend
SPRING_PROFILES_ACTIVE=production
SPRING_DATASOURCE_URL=${DATABASE_URL}
SECURITY_SECRET=${JWT_SECRET}
LOG_LEVEL=INFO

# Video Classifier
SECURITY_SECRET=${JWT_SECRET}
DEBUG=false
LOG_LEVEL=INFO
```

### Configuration Validation

#### Backend Validation
```java
@Component
public class ConfigurationValidator {
    
    @Value("${security.secret}")
    private String jwtSecret;
    
    @PostConstruct
    public void validateConfiguration() {
        if (jwtSecret == null || jwtSecret.isEmpty()) {
            throw new IllegalStateException("JWT secret is not configured");
        }
        
        // Validate base64 encoding
        try {
            Base64.getDecoder().decode(jwtSecret);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("JWT secret is not valid base64");
        }
        
        log.info("Configuration validation successful");
    }
}
```

#### Video Classifier Validation
```python
# config_validator.py
import base64
import os
import logging

def validate_configuration():
    """Validate critical configuration settings"""
    
    # Check JWT secret
    secret = os.getenv('SECURITY_SECRET')
    if not secret:
        raise ValueError("SECURITY_SECRET environment variable is required")
    
    try:
        base64.b64decode(secret)
    except Exception:
        raise ValueError("SECURITY_SECRET must be valid base64")
    
    # Check issuer
    issuer = os.getenv('SECURITY_ISSUER', 'emotion_recognition')
    if issuer != 'emotion_recognition':
        logging.warning(f"Non-standard issuer: {issuer}")
    
    logging.info("Configuration validation successful")

# Call during startup
validate_configuration()
```

## Monitoring & Logging

### Centralized Logging

#### Log Format Standardization
```json
{
  "timestamp": "2025-05-29T10:30:00Z",
  "service": "backend|video-classifier",
  "level": "INFO|ERROR|DEBUG",
  "message": "Log message",
  "userId": 12345,
  "requestId": "uuid",
  "endpoint": "/api/predict/",
  "duration": 150,
  "extra": {}
}
```

#### Backend Logging Configuration
```java
// logback-spring.xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <message/>
                <mdc/>
                <arguments/>
            </providers>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

#### Video Classifier Logging
```python
import logging
import json
from datetime import datetime

class JSONFormatter(logging.Formatter):
    def format(self, record):
        log_entry = {
            "timestamp": datetime.utcnow().isoformat(),
            "service": "video-classifier",
            "level": record.levelname,
            "message": record.getMessage(),
            "module": record.module,
            "function": record.funcName,
            "line": record.lineno
        }
        
        # Add user context if available
        if hasattr(record, 'user_id'):
            log_entry['userId'] = record.user_id
            
        return json.dumps(log_entry)

# Configure logging
handler = logging.StreamHandler()
handler.setFormatter(JSONFormatter())
logging.getLogger().addHandler(handler)
```

### Health Checks

#### Backend Health Check
```java
@RestController
public class HealthController {
    
    @Autowired
    private DataSource dataSource;
    
    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "healthy");
        health.put("service", "backend");
        health.put("timestamp", Instant.now());
        
        // Check database connectivity
        try {
            dataSource.getConnection().close();
            health.put("database", "connected");
        } catch (Exception e) {
            health.put("database", "disconnected");
            health.put("status", "unhealthy");
        }
        
        return ResponseEntity.ok(health);
    }
}
```

#### Video Classifier Health Check
```python
@app.get("/api/health")
async def health_check():
    """Enhanced health check with model status"""
    health_info = {
        "status": "healthy",
        "service": "video-classifier",
        "timestamp": datetime.utcnow().isoformat()
    }
    
    # Check model availability
    try:
        # Quick model inference test
        test_tensor = torch.randn(1, 3, 64, 64).to(emotion_recognizer.device)
        with torch.no_grad():
            _ = emotion_recognizer.model(test_tensor)
        health_info["model"] = "loaded"
    except Exception as e:
        health_info["model"] = "error"
        health_info["status"] = "unhealthy"
        health_info["error"] = str(e)
    
    status_code = 200 if health_info["status"] == "healthy" else 503
    return JSONResponse(content=health_info, status_code=status_code)
```

## Troubleshooting Integration Issues

### Common Issues and Solutions

#### 1. JWT Token Mismatch
**Symptoms**: 401 errors from video classifier despite valid backend login

**Diagnosis**:
```bash
# Check JWT secrets match
# Backend
grep SECURITY_SECRET backend/src/main/resources/application.properties

# Video Classifier
grep SECURITY_SECRET video_classifier/.env

# Decode token to check issuer
python -c "
import jwt
token = 'your-token-here'
decoded = jwt.decode(token, options={'verify_signature': False})
print(f'Issuer: {decoded.get(\"iss\")}')
print(f'Subject: {decoded.get(\"sub\")}')
print(f'User ID: {decoded.get(\"userId\")}')
"
```

**Solution**:
```bash
# Ensure identical configuration
echo "SECURITY_SECRET=c2VjcmV0" >> backend/.env
echo "SECURITY_SECRET=c2VjcmV0" >> video_classifier/.env
echo "SECURITY_ISSUER=emotion_recognition" >> both .env files
```

#### 2. Service Discovery Issues
**Symptoms**: Services can't communicate in Docker/Kubernetes

**Diagnosis**:
```bash
# Test network connectivity
docker exec backend_container ping video-classifier-service
docker exec video-classifier_container ping backend-service

# Check service resolution
nslookup backend-service
nslookup video-classifier-service
```

**Solution**:
```yaml
# docker-compose.yml - use service names
services:
  backend:
    environment:
      VIDEO_CLASSIFIER_URL: http://video-classifier:8000
  
  video-classifier:
    environment:
      BACKEND_URL: http://backend:8080
```

#### 3. Model Loading Failures
**Symptoms**: Video classifier starts but fails on prediction

**Diagnosis**:
```bash
# Check model files
ls -la video_classifier/emotion_recognizer/models/
python -c "
import torch
model_path = './video_classifier/emotion_recognizer/models/ResEmoteNet/ResEmoteNetBS32.pth'
try:
    checkpoint = torch.load(model_path, map_location='cpu')
    print('Model loaded successfully')
except Exception as e:
    print(f'Model loading failed: {e}')
"
```

**Solution**:
```bash
# Re-download model
cd video_classifier
python download_models.py

# Verify model integrity
python -c "
from emotion_recognizer import EmotionRecognizer
recognizer = EmotionRecognizer()
print('Model initialized successfully')
"
```

#### 4. Database Connection Issues
**Symptoms**: Backend fails to start or throws database errors

**Diagnosis**:
```bash
# Test database connectivity
psql -h localhost -p 5432 -U postgres -d emotion_recognition

# Check backend logs
docker logs backend_container | grep -i database
```

**Solution**:
```bash
# Verify database configuration
docker-compose up postgres -d
sleep 10  # Wait for postgres to be ready

# Test connection
docker exec -it postgres_container psql -U postgres -c "SELECT version();"

# Restart backend
docker-compose restart backend
```

### Performance Troubleshooting

#### High Memory Usage (Video Classifier)
```bash
# Monitor memory usage
docker stats video-classifier_container

# Profile memory usage
python -m memory_profiler video_classifier/api.py

# Optimize settings
export PYTORCH_CUDA_ALLOC_CONF=max_split_size_mb:512
```

#### Slow Response Times
```bash
# Backend performance
curl -w "@curl-format.txt" -s -o /dev/null http://localhost:8080/api/health

# Video classifier performance
time curl -X POST -H "Authorization: Bearer $TOKEN" \
  -F "file=@test_video.mp4" \
  http://localhost:8000/api/predict/
```

### Debugging Tools

#### Log Aggregation
```bash
# Collect logs from all services
docker-compose logs backend > backend.log
docker-compose logs video-classifier > classifier.log

# Real-time monitoring
docker-compose logs -f --tail=100
```

#### Request Tracing
```python
# Add to both services for request correlation
import uuid

@middleware("http")
async def add_correlation_id(request: Request, call_next):
    correlation_id = request.headers.get("X-Correlation-ID", str(uuid.uuid4()))
    request.state.correlation_id = correlation_id
    
    response = await call_next(request)
    response.headers["X-Correlation-ID"] = correlation_id
    return response
```

---

*This integration guide provides comprehensive instructions for deploying and managing the complete Emotion Recognition system. For service-specific details, refer to the individual architecture and setup documentation.*
