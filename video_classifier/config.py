import os
from typing import Optional


class Config:
    """Configuration class for environment variables"""
    
    @staticmethod
    def get_security_secret() -> str:
        """Get JWT security secret from environment"""
        return os.getenv("SECURITY_SECRET", "secret")
    
    @staticmethod 
    def get_security_issuer() -> str:
        """Get JWT issuer from environment"""
        return os.getenv("SECURITY_ISSUER", "emotion_recognition")
    
    @staticmethod
    def get_google_client_id() -> str:
        """Get Google OAuth client ID from environment"""
        return os.getenv("GOOGLE_CLIENT_ID", "117151816412-k3bheigoqrcrm4h9aul8203unrr1ldit.apps.googleusercontent.com")
    
    @staticmethod
    def get_google_client_secret() -> str:
        """Get Google OAuth client secret from environment"""
        return os.getenv("GOOGLE_CLIENT_SECRET", "nope")
    
    @staticmethod
    def load_env_file(env_file_path: str = ".env") -> None:
        """Load environment variables from .env file"""
        if os.path.exists(env_file_path):
            with open(env_file_path, 'r') as f:
                for line in f:
                    line = line.strip()
                    if line and not line.startswith('#') and '=' in line:
                        key, value = line.split('=', 1)
                        # Only set if not already in environment
                        if key not in os.environ:
                            os.environ[key] = value
