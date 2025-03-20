import os
import logging
from transformers import AutoImageProcessor, AutoModelForImageClassification

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

def download_models():
    """Download and cache the emotion recognition model and processor"""
    
    model_name = "dima806/facial_emotions_image_detection"
    cache_dir = os.path.join(os.getcwd(), 'huggingface')
    
    # Create cache directory if it doesn't exist
    os.makedirs(cache_dir, exist_ok=True)
    
    logger.info(f"Downloading models from {model_name}")
    logger.info(f"Models will be saved to {cache_dir}")
    
    try:
        # Download and cache the image processor
        logger.info("Downloading image processor...")
        processor = AutoImageProcessor.from_pretrained(
            model_name, 
            cache_dir=cache_dir
        )
        logger.info("Image processor downloaded successfully")
        
        # Download and cache the model
        logger.info("Downloading model...")
        model = AutoModelForImageClassification.from_pretrained(
            model_name, 
            cache_dir=cache_dir
        )
        logger.info("Model downloaded successfully")
        
        logger.info("All downloads completed successfully")
        return True
    
    except Exception as e:
        logger.error(f"Error downloading models: {str(e)}")
        return False

if __name__ == "__main__":
    success = download_models()
    exit(0 if success else 1)