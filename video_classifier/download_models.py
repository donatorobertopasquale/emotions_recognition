import os
import logging
import torch
from huggingface_hub import hf_hub_download, snapshot_download

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

def download_models():
    """Download and cache the video emotion recognition model"""
    
    model_id = "neilchouGTX/ResEmoteNet_Four_datasets_BatchSize32"
    cache_dir = os.path.join(os.path.dirname(__file__), 'emotion_recognizer', 'models')
    
    # Create cache directory if it doesn't exist
    os.makedirs(cache_dir, exist_ok=True)
    
    logger.info(f"Downloading video emotion analysis model from {model_id}")
    logger.info(f"Models will be saved to {cache_dir}")
    
    try:
        # Download the entire repository content
        logger.info("Downloading model repository...")
        model_path = snapshot_download(
            repo_id=model_id,
            cache_dir=cache_dir,
            local_dir=os.path.join(cache_dir, "ResEmoteNet"),
        )
        logger.info(f"Model downloaded successfully to {model_path}")
        
        # Verify we can find important files
        model_files = os.listdir(model_path)
        logger.info(f"Downloaded files: {model_files}")
        
        # Check if any PyTorch model file exists
        pytorch_files = [f for f in model_files if f.endswith('.pt') or f.endswith('.pth')]
        if pytorch_files:
            logger.info(f"Found PyTorch model files: {pytorch_files}")
        else:
            logger.warning("No PyTorch model files found in the downloaded repository")
        
        logger.info("Download completed successfully")
        return True
    
    except Exception as e:
        logger.error(f"Error downloading model: {str(e)}")
        return False

if __name__ == "__main__":
    success = download_models()
    exit(0 if success else 1)