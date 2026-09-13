from functools import lru_cache
from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    # --- Application Settings ---
    APP_NAME: str = "Football Scouting AI Microservice"
    APP_DESCRIPTION: str = "Microservice providing Gemini AI capabilities for assistance in football club management."
    ENVIRONMENT: str = "development"
    DEBUG: bool = True
    API_V1_STR: str = "/api/v1"

    # --- Google Gemini Settings ---
    GEMINI_API_KEY: str
    DEFAULT_GEMINI_MODEL: str = "gemini-3.5-flash-lite"
    GEMINI_TEMPERATURE: float = 0.3

    # --- RabbitMQ Settings ---
    RABBITMQ_URL: str = "amqp://guest:guest@rabbitmq:5672/"

    # --- Pydantic Settings Configuration ---
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore"
    )

@lru_cache()
def get_settings() -> Settings:
    """
    Creates a cached instance of Settings.
    """
    return Settings() # pyright: ignore[reportCallIssue]

settings = get_settings()