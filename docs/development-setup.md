# Backend Development Setup Guide

## Prerequisites

Before setting up the development environment, ensure you have the following installed:

### Required Software

- **Java JDK 18+**: OpenJDK or Oracle JDK
- **Maven 3.6+**: For dependency management and building
- **PostgreSQL 12+**: Database server
- **Docker & Docker Compose**: For containerized development
- **Git**: Version control

### Optional Tools

- **IntelliJ IDEA** or **VS Code**: Recommended IDEs
- **Postman** or **Insomnia**: API testing
- **DBeaver** or **pgAdmin**: Database management
- **Azure Storage Explorer**: For Azure Blob Storage management

## Environment Setup

### 1. Clone the Repository

```bash
git clone <repository-url>
cd emotions_recognition/backend
```

### 2. Environment Variables

Create a `.env` file in the project root with the following variables:

```env
# Database Configuration
POSTGRESDB_USER=emotion_user
POSTGRESDB_ROOT_PASSWORD=your_secure_password
POSTGRESDB_DATABASE=emotion_recognition
DB_URL=jdbc:postgresql://localhost:5432/emotion_recognition
DB_USERNAME=emotion_user
DB_PASSWORD=your_secure_password

# Azure Storage Configuration
AZURE_STORAGE_CONN_STRING=your_azure_storage_connection_string
AZURE_STORAGE_CONTAINER_NAME=emotion-images
AZURE_STORAGE_SAS_TOKEN=your_sas_token

# Security Configuration
SECURITY_ISSUER=emotion-recognition-app
SECURITY_SECRET=your_jwt_secret_key_at_least_256_bits_long
```

### 3. Database Setup

#### Option A: Using Docker (Recommended)

```bash
# Start PostgreSQL container
docker-compose up -d db
```

#### Option B: Local PostgreSQL Installation

```sql
-- Connect to PostgreSQL as superuser
CREATE DATABASE emotion_recognition;
CREATE USER emotion_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE emotion_recognition TO emotion_user;
```

### 4. Azure Storage Setup

1. **Create Azure Storage Account**:
   - Go to Azure Portal
   - Create a new Storage Account
   - Create a container named `emotion-images`
   - Generate SAS token with read/list permissions

2. **Upload Sample Images**:
   - Upload test images to the container
   - Ensure images are accessible via SAS token

## Development Workflows

### Local Development

#### 1. Running the Application

```bash
# Using Maven Wrapper (Recommended)
./mvnw spring-boot:run

# Using installed Maven
mvn spring-boot:run

# Using Docker
docker-compose --profile core up -d
```

#### 2. Hot Reload Development

For development with automatic restart on file changes:

```bash
./mvnw spring-boot:run -Dspring-boot.run.jvmArguments="-Dspring.devtools.restart.enabled=true"
```

### Building the Application

```bash
# Compile and package
./mvnw clean package

# Skip tests (for faster builds)
./mvnw clean package -DskipTests

# Run tests only
./mvnw test
```

### Docker Development

#### Building Docker Image

```bash
# Build backend image
docker build -t emotion-backend .

# Build with Docker Compose
docker-compose build backend
```

#### Running with Docker Compose

```bash
# Core services (database + backend)
docker-compose --profile core up -d

# Full stack (all services)
docker-compose --profile full up -d

# View logs
docker-compose logs -f backend
```

## IDE Configuration

### IntelliJ IDEA

1. **Import Project**:
   - File → Open → Select `backend/pom.xml`
   - Import as Maven project

2. **Configure Run Configuration**:
   - Run → Edit Configurations
   - Add new Application configuration
   - Main class: `com.eyxpoliba.emotion_recognition.EmotionRecognitionApplication`
   - Environment variables: Set all required variables

3. **Enable Lombok**:
   - Install Lombok plugin
   - Enable annotation processing

### VS Code

1. **Extensions**:
   - Extension Pack for Java
   - Spring Boot Extension Pack
   - Lombok Annotations Support

2. **Settings**:
   - Configure Java SDK path
   - Enable auto-import for Maven dependencies

## Testing

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=UserServiceTest

# Run tests with coverage
./mvnw test jacoco:report
```

### Manual API Testing

#### Using cURL

```bash
# Test login
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{
    "nickname": "testuser",
    "email": "test@example.com",
    "age": 25,
    "gender": "Other",
    "nationality": "Test"
  }'

# Test image download
curl -X GET "http://localhost:8080/api/download-image?imageName=test.jpg" \
  -b cookies.txt \
  -o test_image.jpg
```

#### Using Postman

1. **Create Collection**: "Emotion Recognition API"
2. **Environment Variables**:
   - `base_url`: `http://localhost:8080`
3. **Authentication**: Configure cookie handling in Postman settings

## Debugging

### Application Debugging

#### IntelliJ IDEA
1. Set breakpoints in source code
2. Run application in Debug mode
3. Use debugger controls to step through code

#### VS Code
1. Install Java debugger extension
2. Configure launch.json:

```json
{
  "type": "java",
  "name": "Debug Spring Boot",
  "request": "launch",
  "mainClass": "com.eyxpoliba.emotion_recognition.EmotionRecognitionApplication",
  "projectName": "emotion_recognition"
}
```

### Database Debugging

```sql
-- Check user creation
SELECT * FROM users ORDER BY id DESC LIMIT 10;

-- Check reaction storage
SELECT 
  ur.id,
  u.nickname,
  ur.image,
  ur.image_reaction,
  ur.ai_comment
FROM user_reactions ur
JOIN users u ON ur.user_id = u.id
ORDER BY ur.id DESC;

-- Check token blacklist
SELECT COUNT(*) FROM blacklist_tokens;
```

### Log Analysis

```bash
# View application logs
docker-compose logs -f backend

# Filter for specific log levels
docker-compose logs backend | grep ERROR

# Follow logs with timestamp
docker-compose logs -f -t backend
```

## Common Issues and Solutions

### 1. Database Connection Issues

**Problem**: `Connection to localhost:5432 refused`

**Solutions**:
```bash
# Check if PostgreSQL is running
docker-compose ps db

# Restart database service
docker-compose restart db

# Check database logs
docker-compose logs db
```

### 2. JWT Token Issues

**Problem**: `JWT token not found in cookies`

**Solutions**:
- Ensure login request is successful
- Check cookie settings in browser/client
- Verify JWT secret configuration
- Check token expiration times

### 3. Azure Storage Issues

**Problem**: `Blob not found` or authentication errors

**Solutions**:
- Verify Azure Storage connection string
- Check SAS token permissions and expiration
- Ensure container exists and is accessible
- Test connection with Azure Storage Explorer

### 4. Maven Build Issues

**Problem**: Dependency resolution or compilation errors

**Solutions**:
```bash
# Clean and rebuild
./mvnw clean install

# Force update dependencies
./mvnw clean install -U

# Clear local Maven repository (last resort)
rm -rf ~/.m2/repository
./mvnw clean install
```

### 5. Port Conflicts

**Problem**: `Port 8080 already in use`

**Solutions**:
```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>

# Or change application port
export SERVER_PORT=8081
./mvnw spring-boot:run
```

## Performance Monitoring

### Application Metrics

Add Spring Boot Actuator for monitoring:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**Endpoints**:
- `/actuator/health` - Application health
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application information

### Database Performance

```sql
-- Monitor active connections
SELECT count(*) FROM pg_stat_activity;

-- Check slow queries
SELECT query, mean_exec_time, calls 
FROM pg_stat_statements 
ORDER BY mean_exec_time DESC 
LIMIT 10;
```

## Code Quality

### Code Formatting

```bash
# Format code with Maven plugin
./mvnw spotless:apply

# Check code formatting
./mvnw spotless:check
```

### Static Analysis

```bash
# Run SpotBugs
./mvnw spotbugs:check

# Run PMD
./mvnw pmd:check
```

### Security Scanning

```bash
# Check for vulnerabilities
./mvnw org.owasp:dependency-check-maven:check
```

## Deployment Preparation

### Building for Production

```bash
# Build optimized JAR
./mvnw clean package -Pprod -DskipTests

# Build Docker image for production
docker build -t emotion-backend:prod .
```

### Environment-Specific Configuration

Create different property files:
- `application-dev.properties` - Development
- `application-test.properties` - Testing
- `application-prod.properties` - Production

```bash
# Run with specific profile
./mvnw spring-boot:run -Dspring.profiles.active=prod
```

## Best Practices

### Development Guidelines

1. **Code Style**:
   - Follow Java naming conventions
   - Use meaningful variable and method names
   - Add JavaDoc for public methods

2. **Git Workflow**:
   - Create feature branches for new development
   - Write descriptive commit messages
   - Review code before merging

3. **Security**:
   - Never commit secrets or credentials
   - Use environment variables for configuration
   - Validate all user inputs

4. **Testing**:
   - Write unit tests for service methods
   - Test error scenarios
   - Mock external dependencies

### Troubleshooting Checklist

Before asking for help, check:

- [ ] All environment variables are set correctly
- [ ] Database is running and accessible
- [ ] Azure Storage credentials are valid
- [ ] No port conflicts exist
- [ ] Latest code changes are pulled
- [ ] Dependencies are up to date
- [ ] Application logs for error messages

## Resources

### Documentation
- [Spring Boot Reference](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [Azure Storage Java SDK](https://docs.microsoft.com/en-us/java/api/overview/azure/storage-blob-readme)

### Tools
- [Postman](https://www.postman.com/) - API testing
- [Docker Desktop](https://www.docker.com/products/docker-desktop) - Containerization
- [Azure Storage Explorer](https://azure.microsoft.com/en-us/features/storage-explorer/) - Storage management

---

*This guide provides comprehensive setup and development information. For additional help or questions, consult the development team or project documentation.*
