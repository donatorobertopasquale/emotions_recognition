#!/usr/bin/env python3
"""
Test the FastAPI server with JWT authentication
"""
import requests
import jwt
from datetime import datetime, timedelta
import os
import sys

def create_test_token():
    """Create a test JWT token matching the backend format"""
    secret = "secret"  # From .env
    issuer = "emotion_recognition"  # From .env
    
    payload = {
        "sub": "testuser",  # username
        "userId": 12345,
        "roles": "USER", 
        "iss": issuer,
        "iat": datetime.utcnow(),
        "exp": datetime.utcnow() + timedelta(minutes=60)
    }
    
    return jwt.encode(payload, secret, algorithm="HS512")

def test_api_endpoints():
    """Test API endpoints with and without authentication"""
    base_url = "http://localhost:8000"
    
    print("Testing API endpoints...")
    
    # Test health endpoint (should work without auth)
    try:
        response = requests.get(f"{base_url}/api/health", timeout=5)
        print(f"Health check: {response.status_code} - {response.json()}")
    except requests.exceptions.RequestException as e:
        print(f"Health check failed: {e}")
        return False
    
    # Test predict endpoint without auth (should fail)
    try:
        # Create a dummy file for testing
        files = {"file": ("test.txt", b"test content", "text/plain")}
        response = requests.post(f"{base_url}/api/predict/", files=files, timeout=5)
        print(f"Predict without auth: {response.status_code} - {response.json()}")
    except requests.exceptions.RequestException as e:
        print(f"Predict without auth test failed: {e}")
    
    # Test predict endpoint with Bearer token
    try:
        token = create_test_token()
        headers = {"Authorization": f"Bearer {token}"}
        files = {"file": ("test.txt", b"test content", "text/plain")}
        response = requests.post(f"{base_url}/api/predict/", headers=headers, files=files, timeout=5)
        print(f"Predict with Bearer token: {response.status_code} - {response.text}")
    except requests.exceptions.RequestException as e:
        print(f"Predict with Bearer token test failed: {e}")
    
    # Test predict endpoint with cookie
    try:
        token = create_test_token()
        cookies = {"access_token": token}
        files = {"file": ("test.txt", b"test content", "text/plain")}
        response = requests.post(f"{base_url}/api/predict/", cookies=cookies, files=files, timeout=5)
        print(f"Predict with cookie: {response.status_code} - {response.text}")
    except requests.exceptions.RequestException as e:
        print(f"Predict with cookie test failed: {e}")
    
    return True

if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == "token":
        # Just create and print a token for manual testing
        token = create_test_token()
        print(f"Test JWT token: {token}")
    else:
        test_api_endpoints()
