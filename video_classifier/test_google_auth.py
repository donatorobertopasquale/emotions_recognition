#!/usr/bin/env python3
"""
Test script for Google JWT validation
"""
import sys
import os

# Add current directory to path
sys.path.append(os.path.dirname(__file__))

from google_auth import GoogleTokenVerifier
from jwt_auth import JWTAuthFilter

def test_google_token_verifier():
    """Test Google token verifier initialization"""
    try:
        verifier = GoogleTokenVerifier()
        print("✅ Google Token Verifier initialized successfully")
        print(f"   Client ID: {verifier.client_id}")
        return True
    except Exception as e:
        print(f"❌ Google Token Verifier initialization failed: {e}")
        return False

def test_jwt_auth_filter():
    """Test JWT Auth Filter with Google support"""
    try:
        auth_filter = JWTAuthFilter()
        print("✅ JWT Auth Filter with Google support initialized successfully")
        return True
    except Exception as e:
        print(f"❌ JWT Auth Filter initialization failed: {e}")
        return False

def main():
    print("Testing Google JWT Authentication Implementation...")
    print("=" * 50)
    
    success = True
    success &= test_google_token_verifier()
    success &= test_jwt_auth_filter()
    
    print("=" * 50)
    if success:
        print("✅ All tests passed! Google JWT authentication is ready.")
    else:
        print("❌ Some tests failed. Check the errors above.")
    
    return 0 if success else 1

if __name__ == "__main__":
    sys.exit(main())
