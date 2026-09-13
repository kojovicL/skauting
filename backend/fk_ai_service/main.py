import asyncio
from contextlib import asynccontextmanager
from fastapi import FastAPI
from app.api.router import api_router
from app.api.v1 import tactical
from app.config import settings
from app.services.gemini_service import GeminiService
from app.services.rabbitmq_consumer import consume_rabbitmq

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Initialize the LLM service instance
    gemini_service = GeminiService()

    # Start the RabbitMQ consumer as a background task
    rabbitmq_task = asyncio.create_task(consume_rabbitmq(gemini_service))

    yield # App runs here

    # Clean up gracefully on shutdown
    rabbitmq_task.cancel()

app = FastAPI(
    title=settings.APP_NAME,
    description=settings.APP_DESCRIPTION,
    lifespan=lifespan
)

app.include_router(api_router)
api_router.include_router(tactical.router)


@app.get("/health", tags=["System"])
def health_check():
    return {"status": "UP"}