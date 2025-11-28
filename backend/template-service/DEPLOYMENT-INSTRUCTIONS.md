# Production Deployment Instructions

## Prerequisites
1. **MSSQL Server** installed and running
2. **Java 8+** installed on production server
3. **Maven** (for building)

## Database Setup
1. Run `database-setup.sql` on your MSSQL Server
2. Note down the database credentials

## Application Deployment

### Step 1: Build Both Microservices
```bash
# Option 1: Use build script
build-all.bat

# Option 2: Build manually
cd backend/gateway-service
mvn clean package -DskipTests
cd ../template-service  
mvn clean package -DskipTests
```
This creates:
- `backend/gateway-service/target/gateway-service-0.0.1-SNAPSHOT.jar`
- `backend/template-service/target/template-service-1.0.0.jar`

### Step 2: Set Environment Variables
Set these environment variables on your production server:
```
SPRING_PROFILES_ACTIVE=prod
DB_HOST=your-mssql-server-ip
DB_PORT=1433
DB_NAME=templatedb
DB_USERNAME=sa
DB_PASSWORD=your-database-password
SERVER_PORT=8081
```

### Step 3: Run Both Applications
```bash
# Start Template Service first (Port 8081)
java -jar template-service-1.0.0.jar

# Start Gateway Service (Port 8080)
java -jar gateway-service-0.0.1-SNAPSHOT.jar
```

## Verification
- Template Service runs on port 8081
- Gateway Service runs on port 8080  
- Database tables are created automatically
- Test Gateway: `GET http://server:8080/api/test`
- Test Template: `GET http://server:8081/templates/test`

## Notes for Deployment Team
- Application uses port 8081
- Needs access to MSSQL Server on port 1433
- Creates tables automatically (ddl-auto: update)
- File uploads stored in `storage/` directory
- No external dependencies beyond Java and MSSQL