import asyncio
import json
import logging
import aio_pika
from app.config import settings
from app.schemas.rabbitmq_dto import SeasonalSummaryRequest, SeasonalSummaryResponse
from app.services.gemini_service import GeminiService
from app.services.prompt_service import PromptService

logger = logging.getLogger(__name__)

async def process_message(message: aio_pika.IncomingMessage, gemini_service: GeminiService, channel: aio_pika.abc.AbstractChannel):
    async with message.process():
        try:
            body = json.loads(message.body.decode())
            request_data = SeasonalSummaryRequest(**body)
            logger.info(f"Processing summary for Report ID: {request_data.seasonalReportId}")

            # Prepare prompts and call LLM
            sys_instruction = PromptService.get_system_instruction(role="seasonal_scout")
            prompt = PromptService.build_seasonal_summary_prompt(request_data.model_dump())
            final_prompt = f"{sys_instruction}\n\n{prompt}"

            # We run the synchronous Gemini call in a thread to not block the asyncio loop
            response_text = await asyncio.to_thread(
                gemini_service.generate_text,
                prompt=final_prompt
            )

            # Construct Response
            response_data = SeasonalSummaryResponse(
                seasonalReportId=request_data.seasonalReportId,
                summary=response_text
            )

            # Send back to Spring Boot
            exchange = await channel.get_exchange("scouting-exchange")
            await exchange.publish(
                aio_pika.Message(
                    body=response_data.model_dump_json().encode(),
                    content_type="application/json"
                ),
                routing_key="seasonal.response"
            )
            logger.info(f"Successfully returned summary for Report ID: {request_data.seasonalReportId}")

        except Exception as e:
            logger.error(f"Failed to process rabbitmq message: {e}")

async def consume_rabbitmq(gemini_service: GeminiService):
    retries = 5
    while retries > 0:
        try:
            connection = await aio_pika.connect_robust(settings.RABBITMQ_URL)
            channel = await connection.channel()

            # Ensure exchange and queues exist in case FastAPI starts before Spring Boot
            exchange = await channel.declare_exchange("scouting-exchange", aio_pika.ExchangeType.DIRECT)
            request_queue = await channel.declare_queue("seasonal-report-request-queue", durable=True)
            response_queue = await channel.declare_queue("seasonal-report-response-queue", durable=True)

            await request_queue.bind(exchange, routing_key="seasonal.request")
            await response_queue.bind(exchange, routing_key="seasonal.response")

            logger.info("Connected to RabbitMQ, starting consumer...")

            async for message in request_queue:
                # Process messages concurrently
                asyncio.create_task(process_message(message, gemini_service, channel))

            break # Exit retry loop if successful
        except Exception as e:
            retries -= 1
            logger.warning(f"RabbitMQ connection failed, retrying in 5 seconds... ({retries} left). Error: {e}")
            await asyncio.sleep(5)