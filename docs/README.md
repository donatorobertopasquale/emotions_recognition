# Emotion Recognition System Documentation

This directory contains comprehensive documentation for the complete Emotion Recognition system, including both the Backend service and Video Classifier service.

## Documentation Overview

### Backend Service (Spring Boot)

#### 📋 [Backend Architecture & Technical Documentation](./backend-architecture.md)
Complete technical overview covering:
- **System Architecture**: High-level design and component relationships
- **Technology Stack**: Detailed breakdown of frameworks and libraries
- **Design Patterns**: Implementation patterns used throughout the codebase
- **Security Implementation**: JWT authentication and authorization
- **Database Design**: Entity relationships and data modeling
- **Deployment Strategy**: Docker containerization and multi-stage builds

#### 🔌 [Backend API Reference](./api-reference.md)
Detailed API documentation including:
- **Authentication Endpoints**: Login/logout functionality
- **Data Management**: Image download and reaction storage
- **Request/Response Examples**: Complete cURL and JavaScript examples
- **Error Handling**: Standardized error responses
- **Security Considerations**: CORS, rate limiting, and input validation

#### 🛠️ [Backend Development Setup Guide](./development-setup.md)
Complete development environment setup:
- **Prerequisites & Installation**: Required tools and software
- **Environment Configuration**: Database and Azure Storage setup
- **Development Workflows**: Local development and Docker workflows
- **Testing Strategies**: Unit testing and API testing approaches
- **Debugging & Troubleshooting**: Common issues and solutions
- **Code Quality & Best Practices**: Development guidelines

### Video Classifier Service (FastAPI + ML)

#### 🧠 [Video Classifier Architecture](./video-classifier-architecture.md)
Comprehensive ML service architecture:
- **System Architecture**: FastAPI service design and ML pipeline
- **Technology Stack**: PyTorch, OpenCV, FastAPI integration
- **ML Model Architecture**: ResEmoteNet CNN and emotion classification
- **Authentication & Security**: JWT middleware and token validation
- **Performance Optimization**: Model optimization and scaling strategies

#### 🔍 [Video Classifier API Reference](./video-classifier-api-reference.md)
Complete ML API documentation:
- **Emotion Prediction Endpoints**: Video/image emotion analysis
- **Authentication Integration**: JWT token-based security
- **Response Models**: Emotion classification results and confidence scores
- **SDK Examples**: Python, JavaScript, and cURL integration examples
- **Error Handling**: ML-specific error responses and debugging

#### ⚙️ [Video Classifier Development Setup](./video-classifier-development-setup.md)
ML development environment guide:
- **Prerequisites**: Python, PyTorch, OpenCV setup
- **Model Management**: Pre-trained model download and validation
- **Development Workflow**: FastAPI development server and testing
- **Performance Profiling**: Memory and CPU optimization techniques
- **Troubleshooting**: Common ML deployment issues and solutions

## Quick Start

### For Backend Development:
1. **Read** the [Backend Architecture](./backend-architecture.md) for system understanding
2. **Follow** the [Backend Development Setup Guide](./development-setup.md) to configure your environment
3. **Reference** the [Backend API Documentation](./api-reference.md) for integration details

### For Video Classifier Development:
1. **Read** the [Video Classifier Architecture](./video-classifier-architecture.md) for ML pipeline understanding
2. **Follow** the [Video Classifier Development Setup](./video-classifier-development-setup.md) for ML environment setup
3. **Reference** the [Video Classifier API Documentation](./video-classifier-api-reference.md) for ML integration

### For Full System Integration:
1. Start with **Backend** setup for user authentication and data management
2. Set up **Video Classifier** for emotion recognition capabilities
3. Use both API references for end-to-end integration

## System Architecture Summary

The Emotion Recognition system consists of two main microservices:

### Backend Service (Spring Boot)
```
User Management & Data Storage
├── Spring Boot 3.4.4 Framework
├── PostgreSQL Database
├── Azure Blob Storage Integration
├── JWT Authentication
└── RESTful API Design
```

### Video Classifier Service (FastAPI + ML)
```
Emotion Recognition Engine
├── FastAPI Framework
├── PyTorch Deep Learning
├── ResEmoteNet CNN Model
├── OpenCV Computer Vision
└── Real-time Video Processing
```

### Complete System Flow
```
Client App → Backend API → JWT Token → Video Classifier API → ML Model → Emotion Results
    ↓             ↓                          ↓                      ↓
Database    Azure Storage              Face Detection        Emotion Classification
```

## Key Technologies

### Backend Service
- **Framework**: Spring Boot 3.4.4
- **Security**: Spring Security + JWT
- **Database**: PostgreSQL + JPA/Hibernate
- **Cloud Storage**: Azure Blob Storage
- **Build Tool**: Maven
- **Containerization**: Docker

### Video Classifier Service
- **Framework**: FastAPI (Python)
- **ML Framework**: PyTorch
- **Computer Vision**: OpenCV
- **Model**: Custom ResEmoteNet CNN
- **Authentication**: JWT Middleware
- **Containerization**: Docker

## API Endpoints Overview

### Backend Service (Port 8080)
| Endpoint | Method | Purpose | Auth Required |
|----------|--------|---------|---------------|
| `/api/login` | POST | User authentication | No |
| `/api/logout` | POST | Session termination | Yes |
| `/api/download-image` | GET | Image retrieval | Yes |
| `/api/register-result` | POST | Store user reactions | Yes |

### Video Classifier Service (Port 8000)
| Endpoint | Method | Purpose | Auth Required |
|----------|--------|---------|---------------|
| `/api/health` | GET | Service health check | No |
| `/api/predict/` | POST | Emotion prediction | Yes |
| `/docs` | GET | Interactive API docs | No |
| `/redoc` | GET | Alternative API docs | No |

## Development Environment

### Backend Service
```bash
# Quick start with Docker
docker-compose --profile core up -d

# Local development
cd backend
./mvnw spring-boot:run

# API available at
http://localhost:8080/api
```

### Video Classifier Service
```bash
# Setup Python environment
cd video_classifier
python -m venv venv
source venv/bin/activate  # macOS/Linux
pip install -r requirements.txt

# Download ML models
python download_models.py

# Start development server
uvicorn api:app --host 0.0.0.0 --port 8000 --reload

# API available at
http://localhost:8000/api
```

### Full System (Docker Compose)
```bash
# Start all services
docker-compose up -d

# Backend: http://localhost:8080
# Video Classifier: http://localhost:8000
# PostgreSQL: localhost:5432
```

## Project Structure

### Backend Service
```
backend/
├── src/main/java/com/eyxpoliba/emotion_recognition/
│   ├── controller/     # REST API endpoints
│   ├── service/        # Business logic
│   ├── repository/     # Data access layer
│   ├── model/          # JPA entities
│   ├── security/       # Authentication & authorization
│   ├── payload/        # Request/response DTOs
│   └── dto/            # Data transfer objects
├── src/main/resources/
│   └── application.properties
├── Dockerfile
└── pom.xml
```

### Video Classifier Service
```
video_classifier/
├── api.py                    # FastAPI application
├── config.py                 # Configuration management
├── jwt_auth.py              # JWT authentication
├── jwt_middleware.py        # Authentication middleware
├── requirements.txt         # Python dependencies
├── Dockerfile              # Container configuration
├── emotion_recognizer/     # ML pipeline
│   ├── emotion_predictor.py # Main emotion recognition
│   ├── ResEmoteNet.py      # Neural network model
│   └── models/             # Pre-trained models
└── tests/                  # Test suite
    ├── test_api.py         # API tests
    └── test_jwt.py         # Authentication tests
```

## Contributing

### Code Standards
1. **Backend (Java)**:
   - Follow established Java conventions
   - Use Spring Boot best practices
   - Write unit tests for new functionality
   - Update API documentation for changes

2. **Video Classifier (Python)**:
   - Follow PEP 8 style guidelines
   - Use type hints for function signatures
   - Test ML model changes thoroughly
   - Document model architecture changes

### Development Workflow
1. **Feature Development**: Create feature branches from main
2. **Testing**: Ensure all tests pass for both services
3. **Documentation**: Update relevant documentation files
4. **Security**: Never commit secrets, tokens, or credentials
5. **Integration**: Test service-to-service communication

### Testing Strategy
- **Backend**: Unit tests with JUnit, integration tests with TestContainers
- **Video Classifier**: Unit tests with pytest, API tests with FastAPI TestClient
- **End-to-End**: Full system integration tests with both services

## Support

For questions or issues:

### Backend Service
- Check the [Backend Development Setup](./development-setup.md) troubleshooting section
- Review [Backend API Reference](./api-reference.md) for integration questions
- Consult [Backend Architecture](./backend-architecture.md) for system design

### Video Classifier Service
- Check the [Video Classifier Development Setup](./video-classifier-development-setup.md) for ML environment issues
- Review [Video Classifier API Reference](./video-classifier-api-reference.md) for ML integration
- Consult [Video Classifier Architecture](./video-classifier-architecture.md) for ML pipeline design

### System Integration
- Ensure both services are properly configured with the same JWT secret
- Verify network connectivity between services
- Check Docker Compose configuration for multi-service deployments

### Common Issues
1. **JWT Token Mismatch**: Ensure `SECURITY_SECRET` is identical in both services
2. **Port Conflicts**: Default ports are 8080 (backend) and 8000 (video classifier)
3. **Model Loading**: Video classifier requires model download on first run
4. **Database Connection**: Backend requires PostgreSQL connection
5. **Azure Storage**: Backend requires valid Azure Storage configuration

---

*Last updated: 29 maggio 2025*
