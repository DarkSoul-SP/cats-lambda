# Cats Lambda Function

A plain Java-based AWS Lambda function that fetches random cat images from the free CATAAS API and sends them to a Telegram chat. 
Works with AWS EventBridge by cron. Perfect for daily cat photo updates via a scheduled trigger!

## Overview

**CatsHandler** is a serverless Lambda function that automatically:
1. Fetches random cat image metadata from the CATAAS API (https://cataas.com)
2. Resolves image URLs (handling both relative and absolute paths)
3. Sends the cat photo to a specified Telegram chat via the Telegram Bot API
4. Runs on a schedule (default: 5:00 AM UTC daily via EventBridge cron trigger)

## Architecture

### Components

- **CatsHandler** (`CatsHandler.java`) - AWS Lambda entry point implementing `RequestHandler`
- **TelegramCatBot** (`TelegramCatBot.java`) - Core business logic

### Technology Stack

- **Java 25** - Modern Java with virtual thread support
- **Maven** - Build and dependency management
- **AWS Lambda** - Serverless compute
- **AWS Secrets Manager** - Secure credential storage

## Setup

### Configuration

#### 1. Create AWS Secret

Store your Telegram credentials in AWS Secrets Manager:

```json
{
  "BOT_TOKEN": "your_telegram_bot_token",
  "CHAT_ID": "your_chat_id",
  "IMG_CAPTION": "Here's a random cat for you! 🐱"
}
```

**Secret Name:** `cats-labmda/secret` (as configured in `CatsHandler.java` line 23)
**Note:** Update the `SECRET_NAME` constant if you use a different secret name.

#### 2. Deploy to AWS Lambda
#### 3. Set Up EventBridge Trigger (Optional)

### Environment Variables

No environment variables are required. All configuration is stored in AWS Secrets Manager.


## API Integration

### CATAAS API

**Endpoint:** `https://cataas.com/cat?json=true`

**Response:**
```json
{
  "id": "abc123",
  "created_at": "2024-01-01T12:00:00.000Z",
  "url": "/cat/abc123",
  "mimetype": "image/jpeg",
  "tags": ["cute", "fluffy"]
}
```

### Telegram Bot API

**Endpoint:** `https://api.telegram.org/{BOT_TOKEN}/sendPhoto`

**Request Body:**
```json
{
  "chat_id": "your_chat_id",
  "photo": "https://cataas.com/cat/abc123.jpg",
  "caption": "Here's a random cat for you! 🐱"
}
```

## References

- [CATAAS API](https://cataas.com)
- [Telegram Bot API](https://core.telegram.org/bots/api)
- [AWS Lambda Java Runtime](https://docs.aws.amazon.com/lambda/latest/dg/lambda-java.html)
- [AWS Secrets Manager](https://docs.aws.amazon.com/secretsmanager/)
