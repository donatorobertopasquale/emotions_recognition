# Backend API Reference

## Overview

This document provides detailed API reference for the Emotion Recognition Backend service. The API follows RESTful principles and uses JSON for data exchange.

## Base URL

```
http://localhost:8080/api
```

## Authentication

The API uses JWT (JSON Web Token) based authentication with HTTP-only cookies for token storage.

### Authentication Flow

1. **Login**: POST to `/api/login` with user credentials
2. **Token Storage**: JWT tokens are automatically stored as HTTP-only cookies
3. **Subsequent Requests**: Tokens are automatically included in requests
4. **Logout**: POST to `/api/logout` to invalidate tokens

### Token Types

- **Access Token**: Short-lived (60 minutes) for API access
- **Refresh Token**: Longer-lived (120 minutes) for token renewal

## Endpoints

### Authentication

#### POST /api/login

Authenticates a user and establishes a session.

**Request:**
```http
POST /api/login
Content-Type: application/json

{
  "nickname": "johndoe",
  "email": "john@example.com",
  "age": 25,
  "gender": "Male",
  "nationality": "Italian"
}
```

**Request Body Parameters:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| nickname | string | Yes | User's display name |
| email | string | Yes | User's email address |
| age | integer | Yes | User's age |
| gender | string | Yes | User's gender |
| nationality | string | Yes | User's nationality |

**Response:**
```http
HTTP/1.1 201 Created
Content-Type: application/json
Set-Cookie: accessToken=eyJhbGciOiJIUzUxMiJ9...; Path=/; Max-Age=3600
Set-Cookie: refreshToken=eyJhbGciOiJIUzUxMiJ9...; Path=/; Max-Age=7200

{
  "userId": 123,
  "imagesName": [
    "image1.jpg",
    "image2.jpg",
    "image3.jpg",
    "image4.jpg",
    "image5.jpg",
    "image6.jpg",
    "image7.jpg",
    "image8.jpg",
    "image9.jpg",
    "image10.jpg"
  ]
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| userId | integer | Unique identifier for the user |
| imagesName | array[string] | List of 10 random image filenames from Azure Storage |

**Status Codes:**
- `201 Created` - User successfully authenticated
- `400 Bad Request` - Invalid request body
- `500 Internal Server Error` - Server error

**Side Effects:**
- Creates or updates user record in database
- Sets JWT access and refresh tokens as HTTP-only cookies
- Retrieves 10 random images from Azure Blob Storage

---

#### POST /api/logout

Terminates the user session and blacklists the JWT token.

**Request:**
```http
POST /api/logout
Cookie: accessToken=eyJhbGciOiJIUzUxMiJ9...
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json
Set-Cookie: jwt=; Path=/; Max-Age=0

"logout successful"
```

**Status Codes:**
- `200 OK` - Successfully logged out
- `400 Bad Request` - JWT token is required or already logged out
- `401 Unauthorized` - Invalid or expired token

**Side Effects:**
- Adds JWT token to blacklist
- Clears JWT cookies
- Invalidates user session

---

### Image Management

#### GET /api/download-image

Downloads an image from Azure Blob Storage.

**Request:**
```http
GET /api/download-image?imageName=example.jpg
Cookie: accessToken=eyJhbGciOiJIUzUxMiJ9...
```

**Query Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| imageName | string | Yes | Name of the image file to download |

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: image/jpeg
Content-Length: 245760

[Binary image data]
```

**Status Codes:**
- `200 OK` - Image successfully retrieved
- `400 Bad Request` - Missing or invalid imageName parameter
- `401 Unauthorized` - Authentication required
- `404 Not Found` - Image not found in storage
- `500 Internal Server Error` - Azure Storage error

**Authentication:** Required

---

### Data Collection

#### POST /api/register-result

Stores user emotion reaction data for analysis.

**Request:**
```http
POST /api/register-result
Content-Type: application/json
Cookie: accessToken=eyJhbGciOiJIUzUxMiJ9...

{
  "imagesDescriptionsAndReactions": [
    {
      "image": "family_photo.jpg",
      "description": "A happy family at the beach",
      "reaction": "joy",
      "aiComment": "The user shows positive emotional response to family imagery"
    },
    {
      "image": "sunset.jpg",
      "description": "Beautiful sunset over mountains",
      "reaction": "peaceful",
      "aiComment": "Natural landscapes evoke calm emotional states"
    }
  ]
}
```

**Request Body Parameters:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| imagesDescriptionsAndReactions | array | Yes | Array of image reaction objects |

**Image Reaction Object:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| image | string | Yes | Image filename |
| description | string | Yes | Description of the image content |
| reaction | string | Yes | User's emotional reaction |
| aiComment | string | Yes | AI-generated comment about the reaction |

**Response:**
```http
HTTP/1.1 201 Created
Content-Type: application/json
Location: /api/register-result

{
  "message": "Result registered successfully"
}
```

**Status Codes:**
- `201 Created` - Results successfully stored
- `400 Bad Request` - Invalid request body or missing fields
- `401 Unauthorized` - Authentication required
- `404 Not Found` - User not found
- `500 Internal Server Error` - Database error

**Authentication:** Required

**Side Effects:**
- Creates records in the `user_reactions` table for each image reaction
- Associates reactions with the authenticated user

---

## Error Responses

All endpoints may return standardized error responses:

### 401 Unauthorized

```http
HTTP/1.1 401 Unauthorized
Content-Type: application/json

{
  "message": "JWT token not found in cookies"
}
```

### 400 Bad Request

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "message": "Invalid request body"
}
```

### 500 Internal Server Error

```http
HTTP/1.1 500 Internal Server Error
Content-Type: application/json

{
  "message": "Internal server error"
}
```

## Data Models

### User Entity

```json
{
  "id": 123,
  "nickname": "johndoe",
  "email": "john@example.com",
  "age": 25,
  "gender": "Male",
  "nationality": "Italian"
}
```

### Reaction Entity

```json
{
  "id": 456,
  "userId": 123,
  "image": "family_photo.jpg",
  "imageDescription": "A happy family at the beach",
  "imageReaction": "joy",
  "aiComment": "The user shows positive emotional response to family imagery"
}
```

## Security Considerations

### CORS Policy

The API currently allows requests from any origin. In production, configure appropriate CORS policies:

```java
// Example CORS configuration
@CrossOrigin(origins = "https://your-frontend-domain.com")
```

### Rate Limiting

Consider implementing rate limiting for production deployments:

- **Login endpoint**: 5 requests per minute per IP
- **Image download**: 100 requests per minute per user
- **Result registration**: 10 requests per minute per user

### Input Validation

All user inputs are validated:

- **Email format**: Must be valid email address
- **Age**: Must be positive integer
- **Image names**: Validated against Azure Storage
- **Text fields**: Sanitized to prevent XSS

## SDK Examples

### JavaScript/Fetch

```javascript
// Login
const loginResponse = await fetch('/api/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  credentials: 'include', // Include cookies
  body: JSON.stringify({
    nickname: 'johndoe',
    email: 'john@example.com',
    age: 25,
    gender: 'Male',
    nationality: 'Italian'
  })
});

const loginData = await loginResponse.json();
console.log('User ID:', loginData.userId);
console.log('Images:', loginData.imagesName);

// Download image
const imageResponse = await fetch('/api/download-image?imageName=example.jpg', {
  credentials: 'include'
});
const imageBlob = await imageResponse.blob();
const imageUrl = URL.createObjectURL(imageBlob);

// Register results
const resultResponse = await fetch('/api/register-result', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  credentials: 'include',
  body: JSON.stringify({
    imagesDescriptionsAndReactions: [
      {
        image: 'example.jpg',
        description: 'A beautiful landscape',
        reaction: 'calm',
        aiComment: 'User responds positively to nature scenes'
      }
    ]
  })
});
```

### cURL Examples

```bash
# Login
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -c cookies.txt \
  -d '{
    "nickname": "johndoe",
    "email": "john@example.com",
    "age": 25,
    "gender": "Male",
    "nationality": "Italian"
  }'

# Download image
curl -X GET "http://localhost:8080/api/download-image?imageName=example.jpg" \
  -b cookies.txt \
  -o downloaded_image.jpg

# Register results
curl -X POST http://localhost:8080/api/register-result \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "imagesDescriptionsAndReactions": [
      {
        "image": "example.jpg",
        "description": "A beautiful landscape",
        "reaction": "calm",
        "aiComment": "User responds positively to nature scenes"
      }
    ]
  }'

# Logout
curl -X POST http://localhost:8080/api/logout \
  -b cookies.txt
```

## Development and Testing

### Local Development Setup

1. **Database**: Ensure PostgreSQL is running on port 5432
2. **Azure Storage**: Configure Azure Blob Storage credentials
3. **Environment Variables**: Set all required environment variables
4. **Build**: Run `./mvnw spring-boot:run` from the backend directory

### Testing Endpoints

Use the provided cURL examples or tools like Postman to test the API endpoints. Ensure you:

1. Start with a login request to establish authentication
2. Save cookies for subsequent requests
3. Test error scenarios (invalid tokens, missing parameters)
4. Verify data persistence in the database

### Database Inspection

```sql
-- View users
SELECT * FROM users;

-- View user reactions
SELECT ur.*, u.nickname 
FROM user_reactions ur 
JOIN users u ON ur.user_id = u.id;

-- View blacklisted tokens
SELECT * FROM blacklist_tokens;
```

---

*This API reference provides comprehensive information for integrating with the Emotion Recognition Backend. For additional support or questions, please refer to the development team.*
