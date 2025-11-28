# Build Instructions for Both Microservices

## Project Structure
```
file-upload-gateway/
├── backend/
│   ├── gateway-service/     (Port 8080)
│   └── template-service/    (Port 8081)
└── frontend/
```

## Building Both Services

### Option 1: Build Each Service Separately

#### Gateway Service:
```bash
cd backend/gateway-service
mvn clean package -DskipTests
```
**Output:** `target/gateway-service-0.0.1-SNAPSHOT.jar`

#### Template Service:
```bash
cd backend/template-service
mvn clean package -DskipTests
```
**Output:** `target/template-service-1.0.0.jar`

### Option 2: Build Both at Once (from root)
```bash
# From file-upload-gateway root directory
cd backend/gateway-service && mvn clean package -DskipTests && cd ../template-service && mvn clean package -DskipTests
```

## Deployment JARs Location
- **Gateway Service:** `backend/gateway-service/target/gateway-service-0.0.1-SNAPSHOT.jar`
- **Template Service:** `backend/template-service/target/template-service-1.0.0.jar`

## Running in Production
```bash
# Start Template Service (Port 8081)
java -jar template-service-1.0.0.jar

# Start Gateway Service (Port 8080) 
java -jar gateway-service-0.0.1-SNAPSHOT.jar
```

## Notes for Deployment Team
- Both services are independent Spring Boot applications
- Each creates its own executable JAR file
- Gateway Service depends on Template Service being available
- Both need MSSQL database access in production