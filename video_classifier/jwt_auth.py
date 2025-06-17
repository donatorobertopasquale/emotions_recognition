import jwt
import os
import logging
import base64
from datetime import datetime
from typing import Optional, Dict, Any
from fastapi import HTTPException, Request
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from fastapi.security.utils import get_authorization_scheme_param
from config import Config
from google_auth import GoogleTokenVerifier

# Set up logging
logger = logging.getLogger(__name__)


class JWTProvider:
    def __init__(self):
        base64_secret = Config.get_security_secret()
        try:
            # Try to decode as base64 first
            self.secret_key = base64.b64decode(base64_secret)
        except Exception:
            # If not valid base64, use as-is (encode to bytes)
            self.secret_key = base64_secret.encode('utf-8')
        
        self.issuer = Config.get_security_issuer()
        
        # Log configuration (without exposing the secret)
        logger.info(f"JWT Provider initialized with issuer: {self.issuer}")
    
    def get_username_from_token(self, token: str) -> str:
        """Extract username from JWT token"""
        try:
            payload = jwt.decode(token, self.secret_key, algorithms=["HS512"])
            username = payload.get("sub")
            if not username:
                raise HTTPException(status_code=401, detail="Username not found in token")
            return username
        except jwt.ExpiredSignatureError:
            raise HTTPException(status_code=401, detail="Token has expired")
        except jwt.InvalidTokenError as e:
            logger.warning(f"Invalid JWT token: {e}")
            raise HTTPException(status_code=401, detail="Invalid JWT token")
    
    def get_user_id_from_token(self, token: str) -> int:
        """Extract user ID from JWT token"""
        try:
            payload = jwt.decode(token, self.secret_key, algorithms=["HS512"])
            user_id = payload.get("userId")
            if user_id is None:
                raise HTTPException(status_code=401, detail="User ID not found in token")
            return int(user_id)
        except jwt.ExpiredSignatureError:
            raise HTTPException(status_code=401, detail="Token has expired")
        except jwt.InvalidTokenError as e:
            logger.warning(f"Invalid JWT token: {e}")
            raise HTTPException(status_code=401, detail="Invalid JWT token")
        except (ValueError, TypeError) as e:
            logger.warning(f"Invalid user ID in token: {e}")
            raise HTTPException(status_code=401, detail="Invalid user ID in token")
    
    def get_claim_from_token(self, token: str, claim_name: str) -> Any:
        """Extract a specific claim from JWT token"""
        try:
            payload = jwt.decode(token, self.secret_key, algorithms=["HS512"])
            return payload.get(claim_name)
        except jwt.ExpiredSignatureError:
            raise HTTPException(status_code=401, detail="Token has expired")
        except jwt.InvalidTokenError as e:
            logger.warning(f"Invalid JWT token: {e}")
            raise HTTPException(status_code=401, detail="Invalid JWT token")
    
    def validate_token(self, token: str) -> bool:
        """Validate JWT token"""
        try:
            payload = jwt.decode(token, self.secret_key, algorithms=["HS512"])
            
            # Check issuer
            iss = payload.get("iss")
            if iss != self.issuer:
                logger.warning(f"Invalid issuer in token: {iss}, expected: {self.issuer}")
                return False
            
            # Check if token has required claims
            required_claims = ["sub", "userId", "iss", "exp"]
            for claim in required_claims:
                if claim not in payload:
                    logger.warning(f"Missing required claim in token: {claim}")
                    return False
                
            return True
        except jwt.ExpiredSignatureError:
            logger.info("Token validation failed: Token has expired")
            return False
        except jwt.InvalidTokenError as e:
            logger.warning(f"Token validation failed: {e}")
            return False


class JWTAuthFilter:
    def __init__(self):
        self.jwt_provider = JWTProvider()
        self.google_verifier = GoogleTokenVerifier()
        self.bearer_scheme = HTTPBearer(auto_error=False)
    
    def get_token_from_request(self, request: Request) -> Optional[str]:
        """Extract JWT token from Authorization header or cookies"""
        # First try Authorization header
        authorization = request.headers.get("Authorization")
        if authorization:
            scheme, token = get_authorization_scheme_param(authorization)
            if scheme.lower() == "bearer" and token:
                return token
        
        # Then try cookies
        token = request.cookies.get("access_token") or request.cookies.get("token")
        if token:
            return token
            
        return None
    
    def validate_request(self, request: Request) -> Dict[str, Any]:
        """Validate JWT token from request and return user info"""
        # Skip validation for public endpoints
        if "/public" in request.url.path or request.url.path == "/api/login":
            return {}
        
        token = self.get_token_from_request(request)
        
        if not token:
            raise HTTPException(
                status_code=401, 
                detail="Malformed authorization header or missing token"
            )
        
        # Try to validate as internal JWT first
        try:
            if self.jwt_provider.validate_token(token):
                username = self.jwt_provider.get_username_from_token(token)
                user_id = self.jwt_provider.get_user_id_from_token(token)
                
                return {
                    "username": username,
                    "user_id": user_id,
                    "token": token,
                    "token_type": "internal"
                }
        except Exception as e:
            logger.debug(f"Token validation as internal JWT failed: {e}")
        
        # If internal JWT validation fails, try Google token validation
        try:
            google_payload = self.google_verifier.verify_token(token)
            return {
                "username": google_payload.get("email"),
                "user_id": google_payload.get("sub"),  # Google user ID
                "email": google_payload.get("email"),
                "google_id": google_payload.get("sub"),
                "token": token,
                "token_type": "google"
            }
        except Exception as e:
            logger.debug(f"Token validation as Google JWT failed: {e}")
        
        # If both validations fail, raise error
        raise HTTPException(status_code=401, detail="Invalid JWT token")
    
    def verify_google_token(self, token: str) -> Dict[str, Any]:
        """Verify Google ID token and return payload"""
        try:
            payload = self.google_verifier.verify_token(token)
            return {
                "google_id": payload.get("sub"),
                "email": payload.get("email"),
                "name": payload.get("name"),
                "picture": payload.get("picture"),
                "email_verified": payload.get("email_verified", False)
            }
        except Exception as e:
            logger.error(f"Google token verification failed: {e}")
            raise HTTPException(status_code=401, detail=f"Invalid Google token: {e}")
