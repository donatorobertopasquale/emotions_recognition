#!/usr/bin/env python3
"""
Comprehensive test suite for JWT authentication in the FastAPI emotion classifier service
"""
import requests
import jwt
from datetime import datetime, timedelta
import json
import sys
import os

def create_test_token(username="testuser", user_id=12345, expires_in_minutes=60):
    """Create a test JWT token matching the backend format"""
    secret = "secret"  # From .env SECURITY_SECRET
    issuer = "emotion_recognition"  # From .env SECURITY_ISSUER
    
    payload = {
        "sub": username,  # username
        "userId": user_id,
        "roles": "USER", 
        "iss": issuer,
        "iat": datetime.utcnow(),
        "exp": datetime.utcnow() + timedelta(minutes=expires_in_minutes)
    }
    
    return jwt.encode(payload, secret, algorithm="HS512")

def create_expired_token():
    """Create an expired JWT token for testing"""
    return create_test_token(expires_in_minutes=-10)  # Expired 10 minutes ago

def test_health_endpoint(base_url):
    """Test health endpoint (should work without auth)"""
    print("🔍 Testing health endpoint...")
    try:
        response = requests.get(f"{base_url}/api/health", timeout=5)
        if response.status_code == 200:
            print(f"✅ Health check: {response.status_code} - {response.json()}")
            return True
        else:
            print(f"❌ Health check failed: {response.status_code}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"❌ Health check error: {e}")
        return False

def test_predict_without_auth(base_url, video_file):
    """Test predict endpoint without authentication (should fail)"""
    print("🔍 Testing predict endpoint without authentication...")
    try:
        with open(video_file, 'rb') as f:
            files = {"file": ("test.mp4", f, "video/mp4")}
            response = requests.post(f"{base_url}/api/predict/", files=files, timeout=10)
        
        if response.status_code == 401:
            print(f"✅ Predict without auth correctly rejected: {response.status_code} - {response.json()}")
            return True
        else:
            print(f"❌ Predict without auth should fail: {response.status_code}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"❌ Predict without auth test error: {e}")
        return False

def test_predict_with_bearer_token(base_url, video_file):
    """Test predict endpoint with Bearer token authentication"""
    print("🔍 Testing predict endpoint with Bearer token...")
    try:
        token = create_test_token()
        headers = {"Authorization": f"Bearer {token}"}
        
        with open(video_file, 'rb') as f:
            files = {"file": ("test.mp4", f, "video/mp4")}
            response = requests.post(f"{base_url}/api/predict/", headers=headers, files=files, timeout=15)
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ Predict with Bearer token: {response.status_code}")
            print(f"   Emotion: {result.get('emotion')}, Confidence: {result.get('confidence'):.4f}")
            return True
        else:
            print(f"❌ Predict with Bearer token failed: {response.status_code} - {response.text}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"❌ Predict with Bearer token error: {e}")
        return False

def test_predict_with_cookie(base_url, video_file):
    """Test predict endpoint with cookie authentication"""
    print("🔍 Testing predict endpoint with cookie...")
    try:
        token = create_test_token()
        cookies = {"access_token": token}
        
        with open(video_file, 'rb') as f:
            files = {"file": ("test.mp4", f, "video/mp4")}
            response = requests.post(f"{base_url}/api/predict/", cookies=cookies, files=files, timeout=15)
        
        if response.status_code == 200:
            result = response.json()
            print(f"✅ Predict with cookie: {response.status_code}")
            print(f"   Emotion: {result.get('emotion')}, Confidence: {result.get('confidence'):.4f}")
            return True
        else:
            print(f"❌ Predict with cookie failed: {response.status_code} - {response.text}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"❌ Predict with cookie error: {e}")
        return False

def test_predict_with_invalid_token(base_url, video_file):
    """Test predict endpoint with invalid token"""
    print("🔍 Testing predict endpoint with invalid token...")
    try:
        headers = {"Authorization": "Bearer invalid_token_here"}
        
        with open(video_file, 'rb') as f:
            files = {"file": ("test.mp4", f, "video/mp4")}
            response = requests.post(f"{base_url}/api/predict/", headers=headers, files=files, timeout=10)
        
        if response.status_code == 401:
            print(f"✅ Invalid token correctly rejected: {response.status_code} - {response.json()}")
            return True
        else:
            print(f"❌ Invalid token should be rejected: {response.status_code}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"❌ Invalid token test error: {e}")
        return False

def test_predict_with_expired_token(base_url, video_file):
    """Test predict endpoint with expired token"""
    print("🔍 Testing predict endpoint with expired token...")
    try:
        token = create_expired_token()
        headers = {"Authorization": f"Bearer {token}"}
        
        with open(video_file, 'rb') as f:
            files = {"file": ("test.mp4", f, "video/mp4")}
            response = requests.post(f"{base_url}/api/predict/", headers=headers, files=files, timeout=10)
        
        if response.status_code == 401:
            print(f"✅ Expired token correctly rejected: {response.status_code} - {response.json()}")
            return True
        else:
            print(f"❌ Expired token should be rejected: {response.status_code}")
            return False
    except requests.exceptions.RequestException as e:
        print(f"❌ Expired token test error: {e}")
        return False

def main():
    """Run all authentication tests"""
    base_url = "http://localhost:8000"
    video_file = "emotion_recognizer/test2.mp4"
    
    if not os.path.exists(video_file):
        print(f"❌ Test video file not found: {video_file}")
        sys.exit(1)
    
    print("🚀 Running JWT Authentication Test Suite for Emotion Classifier API\n")
    
    tests = [
        ("Health Endpoint", test_health_endpoint, [base_url]),
        ("Predict Without Auth", test_predict_without_auth, [base_url, video_file]),
        ("Predict with Bearer Token", test_predict_with_bearer_token, [base_url, video_file]),
        ("Predict with Cookie", test_predict_with_cookie, [base_url, video_file]),
        ("Predict with Invalid Token", test_predict_with_invalid_token, [base_url, video_file]),
        ("Predict with Expired Token", test_predict_with_expired_token, [base_url, video_file])
    ]
    
    passed = 0
    total = len(tests)
    
    for test_name, test_func, args in tests:
        print(f"\n{'='*60}")
        print(f"Test: {test_name}")
        print('='*60)
        
        if test_func(*args):
            passed += 1
        else:
            print(f"❌ {test_name} failed")
    
    print(f"\n{'='*60}")
    print(f"🏁 Test Results: {passed}/{total} tests passed")
    print('='*60)
    
    if passed == total:
        print("🎉 All tests passed! JWT authentication is working correctly.")
        sys.exit(0)
    else:
        print("⚠️ Some tests failed. Check the implementation.")
        sys.exit(1)

if __name__ == "__main__":
    main()
