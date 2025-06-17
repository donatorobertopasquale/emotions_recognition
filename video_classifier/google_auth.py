from google.oauth2 import id_token
from google.auth.transport import requests
from typing import Dict, Any
import logging
from config import Config

logger = logging.getLogger(__name__)


class GoogleTokenVerifier:
    """Service for verifying Google ID tokens"""
    
    def __init__(self):
        self.client_id = Config.get_google_client_id()
        self.request = requests.Request()
        logger.info(f"Google Token Verifier initialized with client ID: {self.client_id}")
    
    def verify_token(self, token: str) -> Dict[str, Any]:
        """
        Verify Google ID token and return payload
        
        Args:
            token: Google ID token string
            
        Returns:
            Dictionary containing token payload
            
        Raises:
            ValueError: If token is invalid
        """
        try:
            # Verify the token
            idinfo = id_token.verify_oauth2_token(
                token, 
                self.request, 
                self.client_id
            )
            
            # Verify issuer
            if idinfo['iss'] not in ['accounts.google.com', 'https://accounts.google.com']:
                logger.error(f"Invalid token issuer: {idinfo.get('iss')}")
                raise ValueError('Invalid token issuer')
            
            # Verify audience (client ID)
            if idinfo['aud'] != self.client_id:
                logger.error(f"Token audience mismatch. Expected: {self.client_id}, Got: {idinfo.get('aud')}")
                raise ValueError('Invalid token audience')
            
            # Verify email is verified
            email_verified = idinfo.get('email_verified', False)
            if not email_verified:
                logger.error("Email not verified in Google token")
                raise ValueError('Email not verified')
            
            logger.info(f"Successfully verified Google token for user: {idinfo.get('email')} ({idinfo.get('sub')})")
            return idinfo
            
        except ValueError as e:
            logger.error(f"Google token verification failed: {e}")
            raise ValueError(f"Invalid Google token: {e}")
        except Exception as e:
            logger.error(f"Unexpected error during Google token verification: {e}")
            raise ValueError(f"Token verification error: {e}")
