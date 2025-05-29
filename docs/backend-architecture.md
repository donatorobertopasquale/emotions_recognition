# Backend Architecture & Technical Documentation

## Table of Contents

1. [Overview](#overview)
2. [Architecture](#architecture)
3. [Technology Stack](#technology-stack)
4. [Project Structure](#project-structure)
5. [Core Components](#core-components)
6. [Security Implementation](#security-implementation)
7. [Database Design](#database-design)
8. [API Endpoints](#api-endpoints)
9. [Design Patterns](#design-patterns)
10. [Configuration](#configuration)
11. [Deployment](#deployment)
12. [Development Guidelines](#development-guidelines)

## Overview

The backend is a **Spring Boot 3.4.4** application designed to support an emotion recognition system. It provides RESTful APIs for user management, authentication, and emotion reaction data processing, with integrated Azure Blob Storage for image handling.

### Key Features

- **User Authentication**: JWT-based stateless authentication with access/refresh token mechanism
- **Emotion Reaction Processing**: Storage and management of user reactions to images
- **Azure Integration**: Seamless integration with Azure Blob Storage for image management
- **Database Operations**: PostgreSQL integration with JPA/Hibernate ORM
- **Containerized Deployment**: Multi-stage Docker build for optimized production deployment

## Architecture

### High-Level Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Backend       │    │   Database      │
│   Application   │◄──►│   Spring Boot   │◄──►│   PostgreSQL    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │   Azure Blob    │
                       │   Storage       │
                       └─────────────────┘
```

### Layered Architecture

The application follows a **layered architecture** pattern:

```
┌─────────────────────────────────────────┐
│             Controller Layer            │  ← REST API Endpoints
├─────────────────────────────────────────┤
│             Service Layer               │  ← Business Logic
├─────────────────────────────────────────┤
│           Repository Layer              │  ← Data Access
├─────────────────────────────────────────┤
│             Model Layer                 │  ← Entities & DTOs
└─────────────────────────────────────────┘
```

## Technology Stack

### Core Framework
- **Spring Boot 3.4.4**: Main application framework
- **Spring Web**: RESTful web services
- **Spring Data JPA**: Database abstraction layer
- **Spring Security**: Authentication and authorization

### Database & ORM
- **PostgreSQL**: Primary database
- **Hibernate**: JPA implementation
- **Connection Pooling**: Built-in HikariCP

### Security & Authentication
- **JWT (JSON Web Tokens)**: Stateless authentication
- **JJWT Library**: JWT token handling
- **Cookie-based Token Storage**: Secure token management

### Cloud Integration
- **Azure Storage Blob**: Image storage and retrieval
- **Azure SDK**: Native Azure integration

### Build & Deployment
- **Maven**: Dependency management and build tool
- **Docker**: Containerization
- **Multi-stage builds**: Optimized production images

### Development Tools
- **Lombok**: Boilerplate code reduction
- **Joda Time**: Date/time handling utilities

## Project Structure

```
backend/
├── src/main/java/com/eyxpoliba/emotion_recognition/
│   ├── EmotionRecognitionApplication.java      # Main Spring Boot application
│   ├── controller/
│   │   └── AppController.java                  # REST API endpoints
│   ├── dto/
│   │   └── UserProfilationDTO.java            # Data Transfer Objects
│   ├── model/
│   │   ├── UserEntity.java                    # User entity
│   │   ├── ReactionsEntity.java               # User reactions entity
│   │   └── BlacklistTokenEntity.java          # Token blacklist entity
│   ├── payload/
│   │   ├── ResultPayload.java                 # Request payloads
│   │   └── ImageDescriptionAndReactionPayload.java
│   ├── repository/
│   │   ├── UserRepository.java                # Data access interfaces
│   │   ├── ReactionsRepository.java
│   │   └── BlacklistTokenRepository.java
│   ├── responses/
│   │   └── LoginResponse.java                 # API response objects
│   ├── security/
│   │   ├── SecurityConfigurer.java            # Security configuration
│   │   ├── JwtProvider.java                   # JWT token management
│   │   └── JwtAuthFilter.java                 # Authentication filter
│   └── service/
│       ├── UserService.java                   # User business logic
│       ├── ReactionsService.java              # Reactions business logic
│       └── AzureStorageService.java           # Azure Blob Storage service
├── src/main/resources/
│   └── application.properties                 # Application configuration
├── Dockerfile                                 # Container configuration
└── pom.xml                                   # Maven dependencies
```

## Core Components

### 1. Controller Layer

#### AppController
The main REST controller that handles all HTTP requests:

```java
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AppController {
    // Endpoints for authentication, image download, and result registration
}
```

**Key Endpoints:**
- `POST /api/login` - User authentication and session initiation
- `POST /api/logout` - Session termination and token blacklisting
- `GET /api/download-image` - Azure Blob Storage image retrieval
- `POST /api/register-result` - Emotion reaction data storage

### 2. Service Layer

#### UserService
Handles user authentication, session management, and JWT token operations:

```java
@Service
@RequiredArgsConstructor
public class UserService {
    // User login/logout logic with JWT token management
    // Cookie-based token storage and validation
}
```

**Key Features:**
- JWT access/refresh token generation
- Cookie-based token storage
- Token blacklisting for logout functionality
- Random image selection from Azure Storage

#### ReactionsService
Manages emotion reaction data processing:

```java
@Service
@AllArgsConstructor
public class ReactionsService {
    // Emotion reaction data persistence
    // User authentication context handling
}
```

#### AzureStorageService
Provides Azure Blob Storage integration:

```java
@Service
public class AzureStorageService {
    // Blob container management
    // Random image selection
    // Image download functionality
}
```

**Key Features:**
- Container initialization with SAS token authentication
- Random blob selection for user experiments
- Efficient image streaming and download

### 3. Repository Layer

All repositories extend `JpaRepository` for standard CRUD operations:

```java
public interface UserRepository extends JpaRepository<UserEntity, Long> {}
public interface ReactionsRepository extends JpaRepository<ReactionsEntity, Long> {}
public interface BlacklistTokenRepository extends JpaRepository<BlacklistTokenEntity, String> {}
```

### 4. Model Layer

#### Entity Relationships

```
UserEntity (1) ──────── (*) ReactionsEntity
     │
     └── Primary Key: id (Long)
         Fields: nickname, email, age, gender, nationality

ReactionsEntity
├── Foreign Key: userId → UserEntity.id
├── Fields: image, imageDescription, imageReaction, aiComment
└── Table: user_reactions

BlacklistTokenEntity
├── Primary Key: jwt (String)
├── Field: expirationDate
└── Table: blacklist_tokens
```

## Security Implementation

### JWT Authentication Flow

```
1. User Login Request
   ↓
2. UserService validates and creates user
   ↓
3. JWT tokens generated (access + refresh)
   ↓
4. Tokens stored as HTTP-only cookies
   ↓
5. JwtAuthFilter validates subsequent requests
   ↓
6. SecurityContext populated with user info
```

### Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfigurer {
    // Stateless session management
    // CSRF disabled for API usage
    // JWT filter chain configuration
}
```

**Security Features:**
- **Stateless Authentication**: No server-side session storage
- **Token Expiration**: Access tokens (60 min), Refresh tokens (120 min)
- **Token Blacklisting**: Secure logout implementation
- **Cookie Security**: HTTP-only cookies for token storage
- **CORS Configuration**: Configurable cross-origin requests

### Authorization Rules

```java
// Public endpoints
requests.requestMatchers("/api/*/public/*").permitAll();
requests.requestMatchers("/api/login").permitAll();

// Admin endpoints
requests.requestMatchers("/api/dashboard/").hasRole("ADMIN");

// All other endpoints require authentication
requests.anyRequest().permitAll();
```

### JWT Token Structure

```json
{
  "sub": "username",
  "iss": "emotion-recognition-app",
  "iat": 1234567890,
  "exp": 1234571490,
  "roles": "USER",
  "userId": 123
}
```

## Database Design

### Entity-Relationship Diagram

```
┌─────────────────┐       ┌─────────────────────┐
│    UserEntity   │       │   ReactionsEntity   │
├─────────────────┤       ├─────────────────────┤
│ id (PK)        │◄─────┤│ id (PK)            │
│ nickname       │       │ userId (FK)        │
│ email          │       │ image              │
│ age            │       │ imageDescription   │
│ gender         │       │ imageReaction      │
│ nationality    │       │ aiComment          │
└─────────────────┘       └─────────────────────┘

┌─────────────────────┐
│ BlacklistTokenEntity│
├─────────────────────┤
│ jwt (PK)           │
│ expirationDate     │
└─────────────────────┘
```

### Database Configuration

```properties
# PostgreSQL Configuration
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

**Note**: `create-drop` is used for development. For production, use `validate` or `update`.

## API Endpoints

### Authentication Endpoints

#### POST /api/login
Authenticates user and initiates session.

**Request Body:**
```json
{
  "nickname": "string",
  "email": "string",
  "age": 25,
  "gender": "string",
  "nationality": "string"
}
```

**Response:**
```json
{
  "userId": 123,
  "imagesName": ["image1.jpg", "image2.jpg", ...]
}
```

**Side Effects:**
- Creates or updates user record
- Sets JWT access/refresh tokens as HTTP-only cookies
- Returns 10 random image names from Azure Storage

#### POST /api/logout
Terminates user session and blacklists JWT token.

**Response:**
```json
{
  "message": "logout successful"
}
```

### Data Management Endpoints

#### GET /api/download-image
Downloads image from Azure Blob Storage.

**Query Parameters:**
- `imageName`: Name of the image file

**Response:**
- Binary image data with appropriate content headers

#### POST /api/register-result
Stores user emotion reaction data.

**Request Body:**
```json
{
  "imagesDescriptionsAndReactions": [
    {
      "image": "image1.jpg",
      "description": "A happy family",
      "reaction": "joy",
      "aiComment": "AI-generated insight"
    }
  ]
}
```

**Response:**
```json
{
  "message": "Result registered successfully"
}
```

## Design Patterns

### 1. Dependency Injection Pattern
Utilized throughout the application via Spring's IoC container:

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    // Dependencies injected automatically
}
```

### 2. Repository Pattern
Data access abstraction through Spring Data JPA:

```java
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // CRUD operations provided automatically
    // Custom queries can be added as needed
}
```

### 3. Builder Pattern
Used in entity creation for complex objects:

```java
ReactionsEntity newResult = ReactionsEntity.builder()
    .userId(user)
    .image(imageData.image())
    .imageDescription(imageData.description())
    .build();
```

### 4. Filter Chain Pattern
Security filter implementation:

```java
@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    // Intercepts requests for authentication
    // Chains to next filter upon successful validation
}
```

### 5. DTO Pattern
Data transfer between layers:

```java
public record LoginResponse(Long userId, List<String> imagesName) {}
public record ResultPayload(List<ImageDescriptionAndReactionPayload> imagesDescriptionsAndReactions) {}
```

## Configuration

### Environment Variables

The application uses environment-based configuration for different deployment environments:

```properties
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/emotion_recognition
DB_USERNAME=your_postgres_user
DB_PASSWORD=your_postgres_password

# Azure Storage Configuration
AZURE_STORAGE_CONN_STRING=your_azure_storage_connection_string
AZURE_STORAGE_CONTAINER_NAME=your_container_name
AZURE_STORAGE_SAS_TOKEN=your_sas_token

# Security Configuration
SECURITY_ISSUER=emotion-recognition-app
SECURITY_SECRET=your_jwt_secret_key
```

### Application Properties

```properties
# Database Configuration
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# Azure Storage Configuration
azure.storage.connection-string=${AZURE_STORAGE_CONN_STRING}
azure.storage.container-name=${AZURE_STORAGE_CONTAINER_NAME}
azure.storage.sas-token=${AZURE_STORAGE_SAS_TOKEN}

# Security Configuration
security.issuer=${SECURITY_ISSUER}
security.secret=${SECURITY_SECRET}
```

## Deployment

### Docker Configuration

#### Multi-Stage Build Process

```dockerfile
# Stage 1: Build stage
FROM openjdk:18-oracle AS build
WORKDIR /app
RUN microdnf install -y maven
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Production stage
FROM openjdk:18-oracle AS prod
WORKDIR /app
RUN groupadd -r appuser && useradd -r -g appuser appuser
COPY --from=build /app/target/*.jar app.jar
RUN chown -R appuser:appuser /app
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Benefits:**
- **Optimized Image Size**: Build dependencies excluded from production image
- **Security**: Non-root user execution
- **Caching**: Separate dependency and source code layers for efficient rebuilds

### Docker Compose Integration

```yaml
backend:
  container_name: backend_container
  build:
    context: ./backend
    dockerfile: Dockerfile
  ports:
    - "8080:8080"
  environment:
    - DB_URL=jdbc:postgresql://db:5432/emotion_recognition
    - DB_USERNAME=${POSTGRESDB_USER}
    - DB_PASSWORD=${POSTGRESDB_ROOT_PASSWORD}
    # Additional environment variables...
  depends_on:
    - db
  restart: always
```

### Maven Configuration

#### Key Dependencies

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Azure Integration -->
    <dependency>
        <groupId>com.azure</groupId>
        <artifactId>azure-storage-blob</artifactId>
        <version>12.30.0</version>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt</artifactId>
        <version>0.9.1</version>
    </dependency>
    
    <!-- Utilities -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>joda-time</groupId>
        <artifactId>joda-time</artifactId>
        <version>2.12.5</version>
    </dependency>
</dependencies>
```

## Development Guidelines

### Code Style & Standards

1. **Package Organization**: Follow domain-driven design principles
2. **Naming Conventions**: Use descriptive names for classes, methods, and variables
3. **Documentation**: Document public APIs and complex business logic
4. **Error Handling**: Implement proper exception handling with meaningful messages

### Testing Strategy

```java
// Example test structure (to be implemented)
@SpringBootTest
class UserServiceTest {
    @Autowired
    private UserService userService;
    
    @MockBean
    private UserRepository userRepository;
    
    // Unit tests for service methods
}
```

### Security Best Practices

1. **Environment Variables**: Never hardcode secrets in source code
2. **Token Management**: Implement proper token rotation and blacklisting
3. **Input Validation**: Validate all user inputs
4. **SQL Injection Prevention**: Use JPA/Hibernate parameterized queries

### Performance Considerations

1. **Database Optimization**: 
   - Use appropriate indexing strategies
   - Implement connection pooling
   - Consider query optimization

2. **Caching Strategy**:
   - Implement Redis for session management (future enhancement)
   - Cache frequently accessed data

3. **Azure Storage**:
   - Use CDN for image delivery (future enhancement)
   - Implement efficient blob streaming

### Monitoring & Logging

```java
@Slf4j
@Service
public class UserService {
    public ResponseEntity<LoginResponse> login(UserEntity user, HttpServletResponse response) {
        log.info("User login attempt for: {}", user.getNickname());
        // Service logic...
        log.info("User login successful for: {}", user.getNickname());
    }
}
```

**Recommended Logging Levels:**
- **ERROR**: System errors, exceptions
- **WARN**: Deprecated usage, potential issues
- **INFO**: User actions, system state changes
- **DEBUG**: Detailed execution flow (development only)

### Future Enhancements

1. **API Documentation**: Integrate Swagger/OpenAPI
2. **Metrics & Monitoring**: Add Spring Actuator endpoints
3. **Caching Layer**: Implement Redis for improved performance
4. **Rate Limiting**: Add request throttling for API protection
5. **Database Migration**: Implement Flyway for database versioning
6. **Testing Suite**: Comprehensive unit and integration tests
7. **Error Handling**: Global exception handler with standardized error responses

---

*This documentation provides a comprehensive overview of the backend architecture, implementation details, and operational guidelines. For specific implementation questions or contributions, please refer to the development team or project maintainers.*
