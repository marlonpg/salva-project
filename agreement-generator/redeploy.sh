#!/bin/bash

set -e

echo "🔄 Rebuilding and redeploying agreement-generator..."

cd "$(dirname "$0")"

echo "📦 Stopping and removing current container..."
docker-compose down

echo "🔨 Building Docker image..."
docker-compose build

echo "🚀 Starting service..."
docker-compose up -d

echo "⏳ Waiting for service to be ready..."
sleep 3

echo "✅ Checking API health..."
if curl -s http://localhost:8081/api/pdf/templates/agreement-template-v2.html/fields > /dev/null; then
  echo "✅ Service is running and healthy!"
else
  echo "❌ Service failed to start"
  exit 1
fi
