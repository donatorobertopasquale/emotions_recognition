# Emotion Recognition Application

A multi-service application for emotion recognition featuring a Spring Boot backend, PostgreSQL database, and Python-based video classifier service.

## Architecture

- **Backend**: Java Spring Boot application (port 8080)
- **Database**: PostgreSQL (port 5432)
- **Video Classifier**: Python FastAPI service for emotion recognition (port 8000)

## Prerequisites

- Docker and Docker Compose installed
- `.env` file with required environment variables (see Environment Variables section)

## Environment Variables

Create a `.env` file in the root directory with the following variables:

```env
POSTGRESDB_USER=your_postgres_user
POSTGRESDB_ROOT_PASSWORD=your_postgres_password
POSTGRESDB_DATABASE=emotion_recognition
AZURE_STORAGE_CONN_STRING=your_azure_storage_connection_string
```

## Docker Compose Profiles

The application uses Docker Compose profiles to allow selective service startup:

### Available Profiles

- **`core`**: Essential services (database + backend)
- **`ml`**: Machine Learning service (video classifier)
- **`full`**: All services (core + ml)

## Usage

### Start Core Services Only (Database + Backend)
```powershell
docker-compose --profile core up -d
```
This starts:
- PostgreSQL database on port 5432
- Spring Boot backend on port 8080

### Start All Services Including Video Classifier
```powershell
docker-compose --profile full up -d
```
This starts:
- PostgreSQL database on port 5432
- Spring Boot backend on port 8080
- Video classifier API on port 8000

### Start Only Video Classifier
```powershell
docker-compose --profile ml up -d
```
This starts only the video classifier service on port 8000.

### Start Specific Services by Name
```powershell
# Start only database and backend
docker-compose up -d db backend

# Start only database
docker-compose up -d db

# Start all services (alternative method)
docker-compose up -d db backend video-classifier
```

## Service Endpoints

Once running, the services are available at:

- **Backend API**: http://localhost:8080
- **Video Classifier API**: http://localhost:8000
- **Database**: localhost:5432

## Development Commands

### View Running Services
```powershell
docker-compose ps
```

### View Service Logs
```powershell
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f backend
docker-compose logs -f video-classifier
docker-compose logs -f db
```

### Stop Services
```powershell
# Stop all running services
docker-compose down

# Stop specific profile
docker-compose --profile full down
```

### Rebuild Services
```powershell
# Rebuild all services
docker-compose --profile full build

# Rebuild specific service
docker-compose build backend
docker-compose build video-classifier
```

### Clean Up
```powershell
# Remove all containers, networks, and volumes
docker-compose down -v

# Remove images as well
docker-compose down -v --rmi all
```

## Video Classifier API

The video classifier service provides emotion recognition capabilities through a FastAPI interface. It includes:

- Pre-trained emotion recognition models
- OpenCV for face detection
- FastAPI server for REST API endpoints

### Key Features
- Dockerized Python 3.11 environment
- Multi-stage build for optimized image size
- Non-root user for security
- Pre-downloaded ML models

## Backend Service

The Spring Boot backend handles:
- User management
- Reaction data processing
- Azure Storage integration
- Database operations

## Database

PostgreSQL database with:
- Persistent data storage
- User and reaction entities
- Configurable through environment variables

## Troubleshooting

### Common Issues

1. **Port conflicts**: Ensure ports 5432, 8000, and 8080 are not in use by other applications
2. **Environment variables**: Verify `.env` file exists and contains all required variables
3. **Docker resources**: Ensure Docker has sufficient memory allocated (especially for ML service)

### Checking Service Health
```powershell
# Check if services are responding
curl http://localhost:8080/health  # Backend health check
curl http://localhost:8000/docs    # Video classifier API docs
```

### Database Connection
```powershell
# Connect to PostgreSQL (requires psql client)
docker-compose exec db psql -U $POSTGRESDB_USER -d $POSTGRESDB_DATABASE
```

## Project Structure

```
├── docker-compose.yml          # Multi-service orchestration
├── Dockerfile                  # Backend service container
├── pom.xml                     # Maven configuration
├── src/                        # Spring Boot source code
├── video_classifier/           # Python ML service
│   ├── Dockerfile             # ML service container
│   ├── api.py                 # FastAPI application
│   ├── requirements.txt       # Python dependencies
│   └── emotion_recognizer/    # ML models and logic
└── README.md                  # This file
```

## Contributing

1. Make changes to the appropriate service
2. Test locally using the appropriate profile
3. Rebuild the affected service
4. Verify all services work together with `--profile full`
