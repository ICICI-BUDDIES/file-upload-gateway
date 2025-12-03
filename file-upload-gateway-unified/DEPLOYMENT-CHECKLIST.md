# Deployment Checklist - File Upload Gateway

## ✅ Issues Fixed

### 1. **Frontend URLs - FIXED**
- ✅ All JavaScript files now use `window.location.origin` (dynamic)
- ✅ No hardcoded localhost URLs
- ✅ Works on any domain/port

### 2. **Database Configuration - FIXED**
- ✅ Supports environment variables: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- ✅ Has fallback defaults for local testing
- ✅ No need to rebuild JAR for different environments

### 3. **Internal Service Communication - SIMPLIFIED**
- ✅ Removed unnecessary HTTP calls to itself
- ✅ Services now use direct database access
- ✅ No need for template.service.url configuration

## 🚀 Deployment Steps

### Option 1: Hardcode Company DB (Simple)
1. Edit `application-prod.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:sqlserver://your-company-db:1433;databaseName=proddb;encrypt=false;trustServerCertificate=true
       username: prod_user
       password: prod_password
   ```

2. Build JAR:
   ```bash
   mvn clean package
   ```

3. Deploy JAR to company domain

4. Run:
   ```bash
   java -jar file-upload-gateway-unified-1.0.0.jar
   ```

### Option 2: Use Environment Variables (Recommended)
1. Keep current config (with env vars)

2. Build JAR:
   ```bash
   mvn clean package
   ```

3. Deploy with environment variables:
   ```bash
   java -jar file-upload-gateway-unified-1.0.0.jar \
     -DDB_URL=jdbc:sqlserver://company-db:1433;databaseName=proddb \
     -DDB_USERNAME=prod_user \
     -DDB_PASSWORD=prod_password \
     -DTEMPLATE_SERVICE_URL=http://your-domain:8081
   ```

## ⚠️ Potential Issues & Solutions

### 1. **Port Configuration**
- **Current**: Server runs on port `8081`
- **Solution**: Change in `application.properties` if needed:
  ```properties
  server.port=${SERVER_PORT:8080}
  ```

### 2. **Database Tables**
- **Current**: `ddl-auto: update` (auto-creates tables)
- **Production**: Consider changing to `validate` after first run
- **Tables created**: `templates`

### 3. **File Upload Size**
- **Current**: 10MB max
- **Change if needed** in `application.properties`:
  ```properties
  spring.servlet.multipart.max-file-size=50MB
  spring.servlet.multipart.max-request-size=50MB
  ```

### 4. **CORS Configuration**
- **Current**: Allows all origins (`*`)
- **Production**: Consider restricting in `CorsConfig.java`:
  ```java
  .allowedOrigins("https://your-company-domain.com")
  ```

### 5. **SQL Server Driver**
- ✅ Already included in `pom.xml`
- ✅ Compatible with SQL Server 2012+

### 6. **Template Service Internal Communication**
- ✅ **FIXED**: Removed HTTP calls to itself
- ✅ Services now use direct Spring dependency injection
- ✅ Faster and simpler - no network overhead

### 7. **H2 Console Link**
- **Current**: Admin page has H2 console link
- **Production**: H2 is disabled in prod profile (SQL Server used instead)
- **Action**: Remove H2 link from admin.html or hide it in prod

### 8. **Static Files**
- ✅ All HTML/JS/CSS served from JAR
- ✅ No external file dependencies
- ✅ Works on any domain

## 🔒 Security Recommendations

1. **Database Credentials**: Use environment variables, not hardcoded
2. **CORS**: Restrict to your company domain
3. **HTTPS**: Enable SSL in production
4. **SQL Injection**: Already protected (using JPA/Hibernate)
5. **File Upload**: Already validated (size, type, structure)

## 📋 Pre-Deployment Checklist

- [ ] Update `application-prod.yml` with company DB credentials (if hardcoding)
- [ ] Test JAR locally with prod profile
- [ ] Verify database connectivity
- [ ] Check firewall rules for SQL Server port
- [ ] Ensure company domain DNS is configured
- [ ] Test file upload with sample templates
- [ ] Verify CORS works from company domain
- [ ] Check logs for any errors

## 🧪 Testing After Deployment

1. Access: `https://your-company-domain.com/register.html`
2. Register a test template
3. Access: `https://your-company-domain.com/index.html?app=<hash>`
4. Upload a test file
5. Verify data in SQL Server database

## 📝 Configuration Summary

| Property | Default | Environment Variable | Required |
|----------|---------|---------------------|----------|
| Server Port | 8081 | SERVER_PORT | No |
| DB URL | localhost:1433 | DB_URL | Yes (prod) |
| DB Username | myuser | DB_USERNAME | Yes (prod) |
| DB Password | mypassword | DB_PASSWORD | Yes (prod) |
| Template Service URL | http://localhost:8081 | TEMPLATE_SERVICE_URL | No |
| Max File Size | 10MB | - | No |

## ✅ Ready for Deployment!

All hardcoded URLs and configurations have been made dynamic. The application will work on any domain/port without code changes.
