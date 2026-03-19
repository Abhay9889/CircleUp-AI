

from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    # Database
    database_url: str = "postgresql://educircle:educircle_secret@localhost:5432/educircle"
    redis_url: str = "redis://localhost:6379/0"

    # MinIO
    minio_url: str = "http://localhost:9000"
    minio_access_key: str = "minioadmin"
    minio_secret_key: str = "minio_secret_123"

    # Ollama
    ollama_base_url: str = "http://localhost:11434"
    ollama_model: str = "llama3.2"
    ollama_embed_model: str = "nomic-embed-text"

    # APIs
    brave_api_key: str = ""
    libre_translate_url: str = "http://localhost:5001"

    # JWT
    jwt_secret: str = "dev_jwt_secret"

    # App
    environment: str = "development"

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"


@lru_cache()
def get_settings() -> Settings:
    return Settings()