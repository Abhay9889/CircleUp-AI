FROM python:3.11-slim

WORKDIR /app

# System dependencies for OCR, audio, ML
RUN apt-get update && apt-get install -y --no-install-recommends \
    tesseract-ocr \
    tesseract-ocr-hin \
    tesseract-ocr-tam \
    ffmpeg \
    libsm6 \
    libxext6 \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

# Install Python dependencies first (cached layer)
COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Download spaCy model
RUN python -m spacy download en_core_web_sm

# Copy app code
COPY . .

# Create dirs for FAISS indices
RUN mkdir -p faiss_indices

EXPOSE 8000

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]