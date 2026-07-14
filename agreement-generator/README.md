# Agreement Generator Service

A Spring Boot microservice for generating filled PDF agreements. This service takes a JSON request with optional fields and returns a PDF with those fields filled in.

## Features

- **Simple REST API** for PDF generation
- **Optional parameters** - only fields provided in the request are filled
- **Docker containerized** for easy deployment
- **Field discovery** - endpoint to list available fields in the PDF
- **Robust error handling** and logging

## Prerequisites

- Docker (for running the container)
- Maven 3.9+ (for local development)
- Java 17+

## API Endpoints

### Generate Transport Agreement

**POST** `/api/agreements/transport`

Generates a filled transport agreement PDF.

**Request Body:**
```json
{
  "fieldName1": "value1",
  "fieldName2": "value2"
}
```

All fields are optional. Only provided fields will be filled in the PDF.

**Response:**
- Returns the filled PDF file as binary data
- Content-Type: `application/pdf`
- Filename: `transport-agreement.pdf`

**Example using curl:**
```bash
curl -X POST http://localhost:8080/api/agreements/transport \
  -H "Content-Type: application/json" \
  -d '{"companyName": "Acme Corp", "date": "2026-07-13"}' \
  -o agreement.pdf
```

### List Available Fields

**GET** `/api/agreements/transport/fields`

Returns an array of all available field names in the transport agreement PDF.

**Response:**
```json
["field1", "field2", "field3", ...]
```

**Example using curl:**
```bash
curl http://localhost:8080/api/agreements/transport/fields
```

## Running with Docker

### Using Docker Compose (Recommended)

```bash
docker-compose up --build
```

The service will be available at `http://localhost:8080`

### Manual Docker Build

```bash
docker build -t salva-pdf-generator:latest .
docker run -p 8080:8080 salva-pdf-generator:latest
```

## Local Development

### Build

```bash
mvn clean package
```

### Run

```bash
mvn spring-boot:run
```

The service will start on `http://localhost:8080`

## Project Structure

```
agreement-generator/
├── src/
│   ├── main/
│   │   ├── java/com/salva/pdf/
│   │   │   ├── AgreementGeneratorApplication.java
│   │   │   ├── controller/
│   │   │   │   └── AgreementController.java
│   │   │   ├── dto/
│   │   │   │   └── AgreementRequest.java
│   │   │   └── service/
│   │   │       └── PdfFillerService.java
│   │   └── resources/
│   │       ├── templates/
│   │       │   └── transport-agreement.pdf
│   │       └── application.properties
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## Dependencies

- **Spring Boot 3.2.0** - Web framework
- **Apache PDFBox 3.0.0** - PDF manipulation
- **Lombok** - Reduce boilerplate code
- **Java 17** - Language version

## Logging

The service logs all operations. View logs in Docker:

```bash
docker-compose logs -f pdf-generator
```

## Integration with Main Backend

To integrate this service with your main backend:

1. Call this service from your backend when you need to generate agreements:

```bash
curl -X POST http://pdf-generator:8080/api/agreements/transport \
  -H "Content-Type: application/json" \
  -d '{...}'
```

2. Use service name `pdf-generator` for container-to-container communication in Docker networks

## Troubleshooting

- **No fillable fields found**: The PDF may not have form fields defined. Check the PDF is correct.
- **Field not found warning**: The requested field doesn't exist in the PDF. Check the available fields endpoint.
- **Port 8080 already in use**: Change the port in `docker-compose.yml` or application.properties

## Future Enhancements

- Support for multiple PDF templates
- Form field validation
- Batch processing
- Template versioning
