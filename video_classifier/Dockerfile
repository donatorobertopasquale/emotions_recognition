# Stage 1: Build stage
FROM python:3.11-slim AS build

# Set the working directory
WORKDIR /app

# Install system dependencies required to build dlib
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    cmake \
    libsm6 \
    libxext6 \
    libxrender-dev \
    && rm -rf /var/lib/apt/lists/*

# Copy requirements and download script first (for better caching)
COPY requirements.txt download_models.py ./

# Create and activate virtual environment
RUN python -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# Install required packages
RUN pip install --no-cache-dir -r requirements.txt

# Copy the emotion recognizer module (needed for downloading models)
COPY emotion_recognizer ./emotion_recognizer/

# Download the model
RUN python download_models.py

# Stage 2: Production stage
FROM python:3.11-slim AS prod

# Set the working directory
WORKDIR /app

# Install runtime dependencies for OpenCV and other libraries
# Added libgl1-mesa-glx for OpenCV OpenGL dependency
RUN apt-get update && apt-get install -y --no-install-recommends \
    libsm6 \
    libxext6 \
    libxrender-dev \
    libgl1-mesa-glx \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

# Create a non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy virtual environment from build stage
COPY --from=build /opt/venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# Copy the downloaded model from build stage
COPY --from=build /app/emotion_recognizer/models /app/emotion_recognizer/models

# Copy application files
COPY api.py ./
COPY emotion_recognizer ./emotion_recognizer/

# Fix permissions
RUN chown -R appuser:appuser /app /opt/venv && \
    chmod -R 755 /app

# Expose the port for the API server
EXPOSE 8000

# Switch to non-root user
USER appuser

# Run the API server using uvicorn
CMD ["uvicorn", "api:app", "--host", "0.0.0.0", "--port", "8000", "--limit-max-requests", "52428800"]