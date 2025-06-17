from fastapi import Request, HTTPException
from fastapi.responses import JSONResponse
from starlette.middleware.base import BaseHTTPMiddleware
from jwt_auth import JWTAuthFilter
import logging
import time

logger = logging.getLogger(__name__)


class JWTAuthMiddleware(BaseHTTPMiddleware):
    """Middleware to handle JWT authentication for protected endpoints"""
    
    def __init__(self, app, excluded_paths=None):
        super().__init__(app)
        self.jwt_auth = JWTAuthFilter()
        self.excluded_paths = excluded_paths or [
            "/api/health",
            "/api/connections/count",
            "/docs", 
            "/redoc", 
            "/openapi.json",
            "/favicon.ico"
        ]
        logger.info(f"JWT Auth Middleware initialized with excluded paths: {self.excluded_paths}")
    
    async def dispatch(self, request: Request, call_next):
        start_time = time.time()
        
        # Skip authentication for excluded paths
        if any(request.url.path.startswith(path) for path in self.excluded_paths):
            logger.debug(f"Skipping authentication for excluded path: {request.url.path}")
            return await call_next(request)
        
        # Skip authentication for public endpoints
        if "/public" in request.url.path:
            logger.debug(f"Skipping authentication for public endpoint: {request.url.path}")
            return await call_next(request)
        
        try:
            # Validate JWT token and add user info to request state
            user_info = self.jwt_auth.validate_request(request)
            request.state.user_info = user_info
            
            # Log successful authentication
            username = user_info.get("username", "unknown")
            user_id = user_info.get("user_id", "unknown")
            token_type = user_info.get("token_type", "unknown")
            logger.info(f"Authentication successful for user: {username} (ID: {user_id}, Type: {token_type})")
            
            response = await call_next(request)
            
            # Log request duration
            duration = time.time() - start_time
            logger.debug(f"Request {request.method} {request.url.path} completed in {duration:.3f}s")
            
            return response
            
        except HTTPException as e:
            logger.warning(f"JWT Authentication failed for {request.url.path}: {e.detail}")
            return JSONResponse(
                status_code=e.status_code,
                content={"message": e.detail}
            )
        except Exception as e:
            logger.error(f"Unexpected error during JWT authentication for {request.url.path}: {str(e)}")
            return JSONResponse(
                status_code=401,
                content={"message": "Authentication failed"}
            )
