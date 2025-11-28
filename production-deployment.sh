#!/bin/bash
# Production Deployment Script

# Set environment variables
export SPRING_PROFILES_ACTIVE=prod
export DB_HOST=your-mssql-server-ip
export DB_PORT=1433
export DB_NAME=templatedb
export DB_USERNAME=sa
export DB_PASSWORD=your-database-password

# Create storage directory
mkdir -p storage

# Start Template Service (Port 8081) - Start this first
echo "Starting Template Service..."
nohup java -jar template-service-1.0.0.jar > template-service.log 2>&1 &
TEMPLATE_PID=$!
echo "Template Service started with PID: $TEMPLATE_PID"

# Wait for Template Service to start
sleep 30

# Start Gateway Service (Port 8080)
echo "Starting Gateway Service..."
nohup java -jar gateway-service-0.0.1-SNAPSHOT.jar > gateway-service.log 2>&1 &
GATEWAY_PID=$!
echo "Gateway Service started with PID: $GATEWAY_PID"

echo "Both services started successfully!"
echo "Template Service PID: $TEMPLATE_PID"
echo "Gateway Service PID: $GATEWAY_PID"
echo "Check logs: tail -f template-service.log gateway-service.log"