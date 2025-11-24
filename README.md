# File Upload Gateway

A comprehensive file upload and validation system with template management capabilities, designed for enterprise applications requiring structured data processing.

## 🏗️ Architecture Overview

The File Upload Gateway consists of three main components:

### 1. **Frontend** (Static Web Interface)
- **Technology**: HTML5, CSS3, Vanilla JavaScript
- **Features**: 
  - ICICI Bank branded UI
  - Template selection and preview
  - File upload with real-time validation
  - Responsive design for mobile and desktop
  - App registration interface

### 2. **Template Service** (Port 8081)
- **Technology**: Spring Boot 2.7.18, Java 8
- **Database**: H2 (Development) / SQL Server (Production)
- **Responsibilities**:
  - Template storage and management
  - App registration and hash generation
  - Template metadata and structure rules
  - File format support (CSV, Excel, TXT, Pipe-delimited)

### 3. **Gateway Service** (Port 8082)
- **Technology**: Spring Boot 2.7.15, Java 8
- **Responsibilities**:
  - File upload processing
  - Template-based validation
  - Data extraction and transformation
  - External application notification

## 📁 Project Structure

```
file-upload-gateway/
├── frontend/                    # Web interface
│   ├── index.html              # Main upload interface
│   ├── register.html           # App registration
│   ├── admin.html              # Admin interface
│   ├── script.js               # Main application logic
│   ├── register.js             # Registration logic
│   ├── style.css               # Styling and branding
│   └── assets/                 # Images and icons
├── backend/
│   ├── template-service/       # Template management service
│   │   ├── src/main/java/      # Java source code
│   │   ├── storage/templates/  # Template file storage
│   │   └── pom.xml            # Maven dependencies
│   └── gateway-service/        # File processing service
│       ├── src/main/java/      # Java source code
│       └── pom.xml            # Maven dependencies
├── storage/templates/          # Shared template storage
└── docs/                      # Documentation
```

## 🚀 Quick Start

### Prerequisites
- **Java 8** or higher
- **Maven 3.6+**
- **Modern web browser**
- **SQL Server** (for production) or H2 (for development)

### 1. Start Template Service
```bash
cd backend/template-service
mvn spring-boot:run
```
*Service will start on http://localhost:8081*

### 2. Start Gateway Service
```bash
cd backend/gateway-service
mvn spring-boot:run
```
*Service will start on http://localhost:8082*

### 3. Open Frontend
Open `frontend/index.html` in your web browser or serve via a web server.

## 🔧 Configuration

### Environment Profiles
Both services support `dev` and `prod` profiles:

**Development (default)**:
- H2 in-memory database
- File-based template storage
- Debug logging enabled

**Production**:
- SQL Server database
- Persistent storage
- Optimized logging

### Application Properties

**Template Service** (`application.properties`):
```properties
server.port=8081
spring.profiles.active=dev
spring.servlet.multipart.max-file-size=10MB
```

**Gateway Service** (`application.properties`):
```properties
server.port=8082
spring.profiles.active=dev
gateway.allowed.applications=*
gateway.max-file-mb=10
template.service.url=http://localhost:8081
```

## 📋 Features

### Template Management
- **Multi-format Support**: CSV, Excel (.xlsx), TXT, Pipe-delimited
- **Structure Validation**: Column headers, row counts, data types
- **Template Preview**: Real-time table preview with sample data
- **Version Control**: Template versioning and history

### File Processing
- **Upload Validation**: File size, format, and structure validation
- **Data Extraction**: Automatic parsing based on template rules
- **Error Handling**: Detailed validation messages and error reporting
- **Progress Tracking**: Real-time upload and processing status

### Application Integration
- **App Registration**: Secure hash-based app identification
- **API Endpoints**: RESTful APIs for external integration
- **Webhook Support**: Automatic data forwarding to registered endpoints
- **Multi-tenant**: Support for multiple applications

### User Interface
- **Responsive Design**: Mobile-first approach with desktop optimization
- **Accessibility**: WCAG compliant interface elements
- **Brand Consistency**: ICICI Bank visual identity
- **Intuitive Workflow**: Step-by-step guided process

## 🔌 API Documentation

### Template Service Endpoints

#### Get App Categories
```http
GET /templates/app/{appHash}/categories
```
Returns available template categories for a specific application.

#### Download Template
```http
GET /templates/app/{appHash}/{category}/download
```
Downloads the template file for the specified category.

#### Get Template Metadata
```http
GET /templates/app/{appHash}/{category}/metadata
```
Returns template structure rules and validation criteria.

#### Register Application
```http
POST /api/register
Content-Type: application/json

{
  "appName": "MyApplication",
  "description": "Application description",
  "contactEmail": "admin@example.com",
  "endpointUrl": "https://api.myapp.com/webhook"
}
```

### Gateway Service Endpoints

#### Upload File
```http
POST /api/gateway/upload
Content-Type: multipart/form-data

file: [uploaded file]
application: [app identifier]
category: [template category]
appNameHash: [app hash]
delimiter: [optional, for custom delimiters]
```

## 🛠️ Development

### Building the Project
```bash
# Build Template Service
cd backend/template-service
mvn clean package

# Build Gateway Service
cd backend/gateway-service
mvn clean package
```

### Running Tests
```bash
mvn test
```

### Code Structure

**Template Service**:
- `controller/`: REST API endpoints
- `service/`: Business logic layer
- `repository/`: Data access layer
- `model/`: Entity definitions
- `parser/`: File format parsers
- `storage/`: File storage management

**Gateway Service**:
- `controller/`: Upload and validation endpoints
- `service/`: File processing and validation
- `dto/`: Data transfer objects
- `util/`: Utility classes

## 🔒 Security Features

- **Input Validation**: Comprehensive file and data validation
- **File Size Limits**: Configurable upload size restrictions
- **CORS Configuration**: Cross-origin request handling
- **Hash-based Authentication**: Secure app identification
- **Error Sanitization**: Safe error message handling

## 📊 Supported File Formats

| Format | Extension | Features |
|--------|-----------|----------|
| CSV | `.csv` | Comma-separated, custom delimiters |
| Excel | `.xlsx` | Multiple sheets, rich formatting |
| Text | `.txt` | Plain text, configurable separators |
| Pipe | `.txt` | Pipe-delimited format |

## 🚨 Error Handling

The system provides comprehensive error handling:

- **Validation Errors**: Clear messages for structure mismatches
- **File Format Errors**: Detailed parsing error information
- **Network Errors**: Graceful handling of service communication
- **User-Friendly Messages**: Non-technical error descriptions

## 🔄 Workflow

1. **App Registration**: Register your application to get a unique hash
2. **Template Selection**: Choose from available template categories
3. **Template Download**: Download the template file for your data
4. **File Preparation**: Prepare your data according to template structure
5. **File Upload**: Upload and validate your file
6. **Data Processing**: System extracts and validates data
7. **Integration**: Validated data is sent to your application endpoint

## 📈 Performance

- **File Size**: Up to 10MB per upload (configurable)
- **Concurrent Users**: Supports multiple simultaneous uploads
- **Processing Speed**: Optimized parsers for large datasets
- **Memory Management**: Efficient streaming for large files

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is proprietary software developed for ICICI Bank applications.

## 📞 Support

For technical support or questions:
- **Email**: support@example.com
- **Documentation**: See `/docs` folder for detailed technical documentation
- **Issues**: Use the GitHub issue tracker for bug reports

## 🔄 Version History

- **v1.0.0**: Initial release with core functionality
- **v1.1.0**: Added Excel support and improved validation
- **v1.2.0**: Enhanced UI and mobile responsiveness
- **v1.3.0**: Multi-tenant support and webhook integration

---

**Built with ❤️ for enterprise file processing needs**