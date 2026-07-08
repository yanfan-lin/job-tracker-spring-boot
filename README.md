# Job Tracker Spring Boot API

[![My Skills](https://skillicons.dev/icons?i=java,spring,maven,postgres,docker,git,github)](https://skillicons.dev)

![Java CI](https://github.com/yanfan-lin/job-tracker-spring-boot/actions/workflows/ci.yml/badge.svg)

## Project Overview

Job Tracker is a Spring Boot REST API for tracking job applications. The project supports creating, reading, updating,
deleting, filtering, searching, sorting, and paginating job application records.

## Features

- Create job applications
- View all job applications
- View a job application by ID
- Partially update job applications
- Delete job applications
- Filter applications by status
- Search applications by company or title
- Sort applications by selected fields
- Paginate results with limit and offset
- Validate request bodies with DTO validation
- Return structured JSON error responses
- Use HTTP Basic authentication for write operations
- Generate Swagger/OpenAPI documentation
- Run the API and PostgreSQL with Docker Compose
- Run automated tests in GitHub Actions

## Tech Stack

- Java 21
- Spring Boot 4.1
- Spring Web MVC
- Spring Data JPA / Hibernate
- PostgreSQL
- Spring Security
- Spring Boot Actuator
- Springdoc OpenAPI / Swagger UI
- Docker / Docker Compose
- JUnit 5
- Mockito
- AssertJ
- MockMvc
- GitHub Actions CI

## Project Structure

```text
src/main/java/com/yanfan/jobtracker
├── config
│   └── SecurityConfig.java
├── controller
│   └── JobApplicationController.java
├── dto
│   ├── JobApplicationPatchRequest.java
│   ├── JobApplicationRequest.java
│   └── JobApplicationResponse.java
├── exception
│   ├── GlobalExceptionHandler.java
│   └── ResourceNotFoundException.java
├── model
│   └── JobApplication.java
├── repository
│   └── JobApplicationRepository.java
├── service
│   └── JobApplicationService.java
└── JobTrackerApplication.java
```

## Architecture

### The project uses a layered backend structure:

- `Controller layer`: receives HTTP requests and returns HTTP responses.
- `Service layer`: contains business logic such as validation flow, update logic, filtering, sorting, and DTO mapping.
- `Repository layer`: handles database access through Spring Data JPA.
- `DTO layer`: separates API request/response models from the database entity.
- `Exception layer`: converts application errors into consistent JSON error responses.

## Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/yanfan-lin/job-tracker-spring-boot.git
```

```bash
cd job-tracker-spring-boot
```

### 2. Requirements

#### Make sure the following tools are installed:

- Java 21
- Docker Desktop
- PostgreSQL (only needed if running without Docker)
- Git

The project includes a Maven wrapper, so Maven does not need to be installed separately.

### 3. Run with Docker Compose

The recommended local setup is Docker Compose because it starts both the Spring Boot API and PostgreSQL:

```bash
docker compose up --build
```

#### The API will be available at:

- http://localhost:8080

The PostgreSQL container is exposed on host port 5433 to avoid conflicts with a local PostgreSQL installation.

#### PostgreSQL connection from the host machine:

- Host: `localhost`
- Port: `5433`

#### To stop the containers:

```bash
docker compose down
```

#### To stop the containers and remove the database volume:

```bash
docker compose down -v
```

### 4. Run locally without Docker

If you want to run the Spring Boot app directly from IntelliJ or the terminal, create a local PostgreSQL database
first:

```sql
CREATE DATABASE job_tracker;
```

The application reads database and security settings from environment variables.
Recommended local environment variables:

```TEXT
DATABASE_URL=jdbc:postgresql://localhost:5432/job_tracker
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_local_postgres_password
APP_USERNAME=admin
APP_PASSWORD=changeme
```

On Windows PowerShell, you can set them temporarily with:

```powershell
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/job_tracker"
$env:DATABASE_USERNAME="postgres"
$env:DATABASE_PASSWORD="your_local_postgres_password"
$env:APP_USERNAME="admin"
$env:APP_PASSWORD="changeme"
```

Then run the app with:

```bash
.\mvnw.cmd spring-boot:run
```

On macOS/Linux:

```bash
./mvnw spring-boot:run
```

## Environment and Database Configuration

The application reads its database connection from these environment variables:

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`

For Docker Compose local development, the app connects to the PostgreSQL service using the values defined in
docker-compose.yml:

- `DATABASE_URL=jdbc:postgresql://db:5432/job_tracker`
- `DATABASE_USERNAME=postgres`
- `DATABASE_PASSWORD=postgres`

For local non-Docker development, the app can connect to a PostgreSQL database running on localhost:

```text
DATABASE_URL=jdbc:postgresql://localhost:5432/job_tracker
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=your_local_postgres_password
```

The application also reads demo HTTP Basic Auth credentials from:

- `APP_USERNAME`
- `APP_PASSWORD`

Default demo credentials used by Docker Compose:

- Username: `admin`
- Password: `changeme`

## Local API Docs

### After the server starts, open:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

## Health Check

You can verify that the API is running with:

```bash
curl -i http://localhost:8080/actuator/health
```

Expected result:

```bash
HTTP/1.1 200 OK
```

The response body should include:

```json
{
  "status": "UP"
}
```

## Run Tests

### The test suite includes:

- Service unit tests
- Controller tests
- Security tests

### Run the automated test suite with:

```bash
.\mvnw.cmd test
```

On macOS/Linux:

```bash
./mvnw test
```

## CI

### GitHub Actions runs the Maven test suite automatically on:

- push to `main`
- pull requests to `main`

### Workflow file:

- `.github/workflows/ci.yml`

## Security

### Read endpoints are public:

- `GET /applications`
- `GET /applications/{id}`
- `GET /actuator/health`
- `GET /swagger-ui/index.html`
- `GET /v3/api-docs`

### Write endpoints require HTTP Basic authentication:

- `POST /applications`
- `PATCH /applications/{id}`
- `DELETE /applications/{id}`

### Default Docker/local demo credentials:

- Username: `admin`
- Password: `changeme`

For real deployment, credentials should be provided through environment variables or a secret manager.

## API Endpoints

| Method | Endpoint                 | Auth Required | Description                        |
|--------|--------------------------|---------------|------------------------------------|
| GET    | `/applications`          | No            | List job applications              |
| GET    | `/applications/{id}`     | No            | Get one job application            |
| POST   | `/applications`          | Yes           | Create a job application           |
| PATCH  | `/applications/{id}`     | Yes           | Partially update a job application |
| DELETE | `/applications/{id}`     | Yes           | Delete a job application           |
| GET    | `/actuator/health`       | No            | Health check                       |
| GET    | `/swagger-ui/index.html` | No            | Swagger UI                         |
| GET    | `/v3/api-docs`           | No            | OpenAPI JSON                       |

## Query Parameters

`GET /applications` supports:

| Parameter | Example        | Description                     |
|-----------|----------------|---------------------------------|
| `status`  | `interview`    | Filters by status               |
| `search`  | `java`         | Searches company and title      |
| `sort_by` | `date_applied` | Sort field                      |
| `order`   | `asc`          | Sort direction: `asc` or `desc` |
| `limit`   | `10`           | Number of records per page      |
| `offset`  | `0`            | Starting offset                 |

Supported `status` values:

- `applied`
- `interview`
- `offer`
- `rejected`

Supported `sort_by` values:

- `id`
- `company`
- `title`
- `status`
- `date_applied`
- `created_at`
- `updated_at`

## Example Requests

### List all applications

```bash
curl -i "http://localhost:8080/applications"
```

### Search applications

```bash
curl -i "http://localhost:8080/applications?search=java"
```

### Filter by status

```bash
curl -i "http://localhost:8080/applications?status=interview"
```

### Sort and paginate

```bash
curl -i "http://localhost:8080/applications?sort_by=date_applied&order=asc&limit=5&offset=0"
```

### Create an application

```bash
curl -i -X POST "http://localhost:8080/applications" \
  -u "admin:changeme" \
  -H "Content-Type: application/json" \
  -d '{
    "company": "Amazon",
    "title": "Backend Developer",
    "status": "applied",
    "dateApplied": "2026-07-06",
    "notes": "Applied through LinkedIn"
  }'
```

### Update an application

```bash
curl -i -X PATCH "http://localhost:8080/applications/1" \
  -u "admin:changeme" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "interview",
    "notes": "Recruiter screen scheduled"
  }'
```

### Delete an application

```bash
curl -i -X DELETE "http://localhost:8080/applications/1" \
  -u "admin:changeme"
```

## Example Response

```json
{
  "id": 1,
  "company": "Amazon",
  "title": "Backend Developer",
  "status": "applied",
  "dateApplied": "2026-07-06",
  "notes": "Applied through LinkedIn",
  "createdAt": "2026-07-06T10:00:00",
  "updatedAt": "2026-07-06T10:00:00"
}
```

## Error Response Format

The API returns structured JSON error responses:

### Example 404 response:

```json
{
  "timestamp": "2026-07-07T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Job application not found with id: 999"
}
```

### Example validation response:

```json
{
  "timestamp": "2026-07-07T10:00:00",
  "status": 400,
  "error": "Validation Error",
  "message": "Request body validation failed",
  "fieldErrors": {
    "company": "Company is required",
    "title": "Title is required",
    "status": "Status must be one of: applied, interview, offer, rejected",
    "dateApplied": "Date applied is required"
  }
}
```

## Project Highlights

### This project demonstrates:

- Building REST APIs with Spring Boot
- Designing layered backend architecture
- Using DTOs for request and response separation
- Validating API input with Jakarta Bean Validation
- Handling errors with a global exception handler
- Using PostgreSQL with Spring Data JPA and Hibernate
- Implementing filtering, searching, sorting, and pagination
- Adding HTTP Basic security
- Writing service, controller, and security tests
- Running the project with Docker Compose
- Generating Swagger/OpenAPI documentation
- Running automated tests with GitHub Actions CI
