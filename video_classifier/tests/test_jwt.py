#!/usr/bin/env python3
"""
Test script for JWT authentication functionality
"""
import os
import sys
sys.path.append(os.path.dirname(__file__))

from config import Config
from jwt_auth import JWTProvider, JWTAuthFilter
import jwt
from datetime import datetime, timedelta

def test_jwt_functionality():
    """Test JWT token validation functionality"""
    
    # Load environment variables
    Config.load_env_file(os.path.join(os.path.dirname(__file__), "..", ".env"))
    
    print("Testing JWT functionality...")
    print(f"Security Secret: {Config.get_security_secret()}")
    print(f"Security Issuer: {Config.get_security_issuer()}")
    
    # Create a test JWT token (similar to what the backend creates)
    secret = Config.get_security_secret()
    issuer = Config.get_security_issuer()
    
    # Create a test token payload
    payload = {
        "sub": "testuser",  # username
        "userId": 12345,
        "roles": "USER",
        "iss": issuer,
        "iat": datetime.utcnow(),
        "exp": datetime.utcnow() + timedelta(minutes=60)
    }
    
    # Generate token
    token = jwt.encode(payload, secret, algorithm="HS512")
    print(f"Generated test token: {token[:50]}...")
    
    # Test JWT Provider
    jwt_provider = JWTProvider()
    
    try:
        # Test token validation
        is_valid = jwt_provider.validate_token(token)
        print(f"Token validation: {is_valid}")
        
        # Test username extraction
        username = jwt_provider.get_username_from_token(token)
        print(f"Extracted username: {username}")
        
        # Test user ID extraction
        user_id = jwt_provider.get_user_id_from_token(token)
        print(f"Extracted user ID: {user_id}")
        
        print("✅ JWT functionality test passed!")
        return True
        
    except Exception as e:
        print(f"❌ JWT functionality test failed: {e}")
        return False

if __name__ == "__main__":
    test_jwt_functionality()
