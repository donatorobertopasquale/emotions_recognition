from fastapi import FastAPI, UploadFile, File, Request, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
import sys
import os
import logging
import asyncio
import json
import time
from typing import Set

# Set TF_KERAS environment variable to resolve Keras compatibility issues
os.environ['TF_KERAS'] = '1'

sys.path.append(os.path.join(os.path.dirname(__file__), "emotion_recognizer"))
from emotion_recognizer import EmotionRecognizer, EmotionResponse
from jwt_middleware import JWTAuthMiddleware
from jwt_auth import JWTAuthFilter
from config import Config
from transformers import pipeline
classifier = pipeline("sentiment-analysis", model=os.path.join(os.path.dirname(__file__), "emotion_recognizer", "models", "emotion_text_classifier") )
# Configure logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Track active WebSocket connections
active_websockets: Set[WebSocket] = set()

async def broadcast_to_all(message: dict) -> None:
    """Broadcast a message to all active WebSocket connections"""
    global active_websockets
    
    if not active_websockets:
        return
    
    disconnected: Set[WebSocket] = set()
    for websocket in list(active_websockets):
        try:
            await websocket.send_json(message)
        except Exception:
            # Connection is broken, mark for removal
            disconnected.add(websocket)
    
    # Remove disconnected WebSockets
    active_websockets -= disconnected
    logger.debug(f"Broadcasted message to {len(active_websockets)} connections")

# Load environment variables
Config.load_env_file(os.path.join(os.path.dirname(__file__), "..", ".env"))
logger.info("Environment variables loaded")

app = FastAPI(
    title="Emotion Recognition API",
    description="""
    API for video-based emotion recognition with JWT authentication
    
    ## WebSocket Endpoints
    
    ### `/api/ws` - Real-time Emotion Classification
    - **Protocol**: WebSocket
    - **Authentication**: JWT token required in cookies (`access_token` or `token`)
    - **Input**: Text messages for emotion classification
    - **Output**: JSON responses with emotion labels and confidence scores
    - **Format**: `{"label": "emotion_name", "score": confidence_value}`
    
    #### Connection Example:
    ```javascript
    // Set JWT token as cookie before connecting
    document.cookie = `access_token=${jwtToken}; path=/`;
    const ws = new WebSocket('ws://localhost:8000/api/ws');
    
    // Send text for classification
    ws.send("I'm feeling great today!");
    
    // Receive emotion classification
    ws.onmessage = (event) => {
        const result = JSON.parse(event.data);
        console.log(result); // {"label": "joy", "score": 0.9887}
    };
    ```
    """,
    version="1.0.0"
)
emotion_recognizer = EmotionRecognizer()
logger.info("Emotion recognizer initialized")

# Add JWT authentication middleware
app.add_middleware(JWTAuthMiddleware)
logger.info("JWT authentication middleware added")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Allows all origins
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/api/health")
async def health_check():
    """Health check endpoint - no authentication required"""
    logger.info("Health check requested")
    return {"status": "healthy", "service": "emotion-classifier"}

@app.get("/api/connections/count")
async def get_active_connections():
    """Get the current number of active WebSocket connections"""
    global active_websockets
    return {
        "active_connections": len(active_websockets),
        "timestamp": time.time()
    }

@app.post("/api/predict/", response_model=EmotionResponse)
async def predict_emotion(request: Request, file: UploadFile = File(...)):
    """Endpoint to predict emotion from uploaded image/video"""
    # User info is available in request.state.user_info (set by middleware)
    user_info = getattr(request.state, 'user_info', {})
    username = user_info.get('username', 'unknown')
    user_id = user_info.get('user_id', 'unknown')
    
    logger.info(f"Emotion prediction requested by user {username} (ID: {user_id})")
    logger.debug(f"File: {file.filename}, Content-Type: {file.content_type}")
    
    try:
        contents = await file.read()
        result = emotion_recognizer.process_video(contents)
        
        logger.info(f"Emotion prediction successful: {result.emotion} (confidence: {result.confidence:.4f})")
        return result
    except Exception as e:
        logger.error(f"Emotion prediction failed for user {username}: {str(e)}")
        raise e
    
@app.websocket("/api/ws")
async def websocket_endpoint(websocket: WebSocket):
    """WebSocket endpoint for real-time emotion classification with JWT authentication"""
    global active_websockets
    jwt_auth = JWTAuthFilter()
    
    try:
        # Extract token from cookies
        cookies = websocket.cookies
        token = cookies.get("access_token") or cookies.get("token") or cookies.get("accessToken")
        
        if not token:
            logger.warning("WebSocket connection rejected: Missing authentication token in cookies")
            await websocket.close(code=4001, reason="Missing authentication token")
            return
        
        # Validate token
        if not jwt_auth.jwt_provider.validate_token(token):
            logger.warning("WebSocket connection rejected: Invalid JWT token")
            await websocket.close(code=4001, reason="Invalid authentication token")
            return
        
        # Get user info from token
        username = jwt_auth.jwt_provider.get_username_from_token(token)
        user_id = jwt_auth.jwt_provider.get_user_id_from_token(token)
        
        await websocket.accept()
        
        # Add to active connections and notify all clients about the new connection count
        active_websockets.add(websocket)
        connection_count = len(active_websockets)
        
        # Send connection count update to all connected clients
        connection_update = {
            "type": "connection_update",
            "active_connections": connection_count,
            "timestamp": time.time()
        }
        await broadcast_to_all(connection_update)
        
        logger.info(f"WebSocket connected for user: {username} (ID: {user_id}). Total connections: {connection_count}")
        
        try:
            while True:
                try:
                    data = await websocket.receive_text()
                    response = classifier(data)
                    
                    # Handle the response properly
                    if response:
                        response_list = list(response) if hasattr(response, '__iter__') else [response]
                        if len(response_list) > 0:
                            # Send emotion classification result
                            first_response = response_list[0]
                            if isinstance(first_response, dict):
                                emotion_result = {
                                    "type": "emotion_classification",
                                    **first_response
                                }
                            else:
                                emotion_result = {
                                    "type": "emotion_classification",
                                    "result": first_response
                                }
                            await websocket.send_json(emotion_result)
                            logger.debug(f"Emotion classification sent for user {username}: {response_list[0]}")
                        else:
                            await websocket.send_json({"type": "error", "message": "No classification result"})
                    else:
                        await websocket.send_json({"type": "error", "message": "No classification result"})
                        
                except WebSocketDisconnect:
                    logger.info(f"WebSocket disconnected for user: {username}")
                    break
                except Exception as e:
                    logger.error(f"WebSocket error for user {username}: {str(e)}")
                    await websocket.send_json({"type": "error", "message": "Classification failed"})
        finally:
            # Remove from active connections and notify all remaining clients
            active_websockets.discard(websocket)
            connection_count = len(active_websockets)
            
            # Send updated connection count to all remaining clients
            connection_update = {
                "type": "connection_update", 
                "active_connections": connection_count,
                "timestamp": time.time()
            }
            await broadcast_to_all(connection_update)
            
            logger.info(f"WebSocket removed for user: {username}. Total connections: {connection_count}")
                
    except Exception as e:
        logger.error(f"WebSocket authentication error: {str(e)}")
        await websocket.close(code=4001, reason="Authentication failed")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)