# Job Tracker Spring Boot API

[![My Skills](https://skillicons.dev/icons?i=java,spring,maven,hibernate,postgres,docker,git,github,githubactions)](https://skillicons.dev)

![Java CI](https://github.com/yanfan-lin/job-tracker-spring-boot/actions/workflows/ci.yml/badge.svg)

## Project Overview

Job Tracker is a Spring Boot REST API with registration, login, signed JWT, and job applications
scoped to the authenticated owner. It demonstrates production-style authentication, authorization, persistence,
validation, testing, and containerized local development.

This is the Java/Spring Boot re-architecture of an earlier [FastAPI version](https://github.com/yanfan-lin/job-tracker-API),
rebuilt to apply enterprise-grade security, layered design, and deeper test coverage in the Java ecosystem.

## Features

- User registration and login with normalized email addresses and BCrypt password hashing
- Signed HS256 JWT access tokens and stateless bearer authentication
- Ownership-scoped create, read, update, and delete operations
- Filtering, searching, sorting, and pagination
- DTO validation and structured application-level error responses
- PostgreSQL persistence through Spring Data JPA
- Configurable local Swagger/OpenAPI access with bearer-token authorization
- Docker Compose for the API and database
- 63 automated tests across API, service, security, and persistence layers, with GitHub Actions CI

## Tech Stack

- **Language & Framework:** Java 21, Spring Boot 4.1.0, Spring Web MVC
- **Security & Validation:** Spring Security, OAuth2 Resource Server, JWT, BCrypt, Jakarta Validation
- **Persistence:** Spring Data JPA, Hibernate, PostgreSQL
- **Testing:** JUnit 5, Mockito, AssertJ, MockMvc, H2
- **API Documentation & Health:** Springdoc OpenAPI, Spring Boot Actuator
- **Build & DevOps:** Maven, Docker, Docker Compose, GitHub Actions

## Project Structure

```text
src/main/java/com/yanfan/jobtracker
|-- config
|   |-- JwtConfig.java
|   |-- OpenApiConfig.java
|   `-- SecurityConfig.java
|-- controller
|   |-- AuthController.java
|   `-- JobApplicationController.java
|-- dto
|   |-- AppUserResponse.java
|   |-- RegisterRequest.java
|   |-- LoginRequest.java
|   |-- LoginResponse.java
|   |-- JobApplicationRequest.java
|   |-- JobApplicationPatchRequest.java
|   `-- JobApplicationResponse.java
|-- exception
|   |-- GlobalExceptionHandler.java
|   |-- DuplicateEmailException.java
|   |-- InvalidCredentialsException.java
|   `-- ResourceNotFoundException.java
|-- model
|   |-- AppUser.java
|   `-- JobApplication.java
|-- repository
|   |-- AppUserRepository.java
|   `-- JobApplicationRepository.java
|-- service
|   |-- AuthService.java
|   |-- JwtService.java
|   `-- JobApplicationService.java
`-- JobTrackerApplication.java
```

## Architecture

The project uses a layered backend structure:

- `Controller layer`: receives HTTP requests, extracts the authenticated identity, and returns HTTP responses.
- `Service layer`: contains registration, login, ownership, update, filtering, sorting, and DTO-mapping logic.
- `Repository layer`: handles PostgreSQL access through Spring Data JPA.
- `Model layer`: maps users, job applications, timestamps, and their ownership relationship.
- `DTO layer`: separates API request and response models from JPA entities and keeps password hashes out of responses.
- `Configuration layer`: configures BCrypt, stateless Spring Security, and JWT signing and validation.
- `Exception layer`: converts application errors into consistent JSON error responses.

## Local Setup

### 1. Clone the repository

```bash
git clone https://github.com/yanfan-lin/job-tracker-spring-boot.git
cd job-tracker-spring-boot
```

### 2. Requirements

- Java 21
- Docker Desktop
- PostgreSQL, only when running without Docker
- Git

The project includes a Maven wrapper, so Maven does not need to be installed separately.

### 3. Run with Docker Compose

Docker Compose starts both the Spring Boot API and PostgreSQL. Set `JWT_SECRET` in the same shell before starting it;
Compose stops with an environment-variable error if the value is missing.

`JWT_SECRET` must be Base64 encoded and must decode to at least 32 bytes (256 bits). Generate a separate secret for
each environment. The examples below create a temporary random secret for local development; they are not production
secrets and should not be committed.

#### Windows PowerShell

```powershell
$secretBytes = New-Object byte[] 32
$random = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$random.GetBytes($secretBytes)
$random.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($secretBytes)

docker compose up --build
```

#### macOS/Linux shell

```bash
export JWT_SECRET="$(openssl rand -base64 32)"
docker compose up --build
```

The services are exposed at:

- API: http://localhost:8080
- PostgreSQL from the host: `localhost:5433`
- PostgreSQL inside the Compose network: `db:5432`

The PostgreSQL container uses host port 5433 to avoid conflicts with a PostgreSQL installation already using port 5432.

For local development, Docker Compose enables anonymous Swagger/OpenAPI access by default. A host-provided
`SWAGGER_PUBLIC` value overrides that default. For example, this protects the documentation routes while leaving the
rest of the application behavior unchanged:

```powershell
$env:SWAGGER_PUBLIC = "false"
docker compose up --build
```

To stop the containers:

```bash
docker compose down
```

To stop the containers and remove the local database volume:

```bash
docker compose down -v
```

### 4. Run locally without Docker

Create a local PostgreSQL database:

```sql
CREATE DATABASE job_tracker;
```

Set `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and `JWT_SECRET`. The database properties have local
fallback values, but `JWT_SECRET` is required and must meet the Base64 and 32-byte decoded-length requirements above.
`SWAGGER_PUBLIC` is optional; set it to `true` for anonymous local documentation access. Omitting it leaves Swagger
protected and does not prevent the API from running.

#### Windows PowerShell

```powershell
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/job_tracker"
$env:DATABASE_USERNAME = "postgres"
$env:DATABASE_PASSWORD = "your_local_postgres_password"

$secretBytes = New-Object byte[] 32
$random = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$random.GetBytes($secretBytes)
$random.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($secretBytes)
$env:SWAGGER_PUBLIC = "true"

.\mvnw.cmd spring-boot:run
```

#### macOS/Linux shell

```bash
export DATABASE_URL="jdbc:postgresql://localhost:5432/job_tracker"
export DATABASE_USERNAME="postgres"
export DATABASE_PASSWORD="your_local_postgres_password"
export JWT_SECRET="$(openssl rand -base64 32)"
export SWAGGER_PUBLIC=true

./mvnw spring-boot:run
```

## Environment and Database Configuration

| Variable | Docker Compose | Local non-Docker | Purpose |
|----------|----------------|------------------|---------|
| `DATABASE_URL` | `jdbc:postgresql://db:5432/job_tracker` | `jdbc:postgresql://localhost:5432/job_tracker` | PostgreSQL connection URL |
| `DATABASE_USERNAME` | `postgres` | Your local PostgreSQL username | PostgreSQL username |
| `DATABASE_PASSWORD` | `postgres` | Your local PostgreSQL password | PostgreSQL password |
| `JWT_SECRET` | Passed through from the host | Set in the shell that starts the application | Base64-encoded JWT signing secret |
| `SWAGGER_PUBLIC` | `true` for local development unless overridden | `false` unless explicitly enabled | Control anonymous access to Swagger UI and OpenAPI documentation |

`JWT_SECRET` must be Base64 encoded and decode to at least 32 bytes. For local development, Hibernate currently uses
`spring.jpa.hibernate.ddl-auto=update`, and `spring.jpa.show-sql=true` prints generated SQL. These settings support
development and are not presented as production recommendations. Open Session in View is disabled.

### Database schema

- `app_users` stores registered users. Email addresses are unique, and `password_hash` stores the BCrypt hash rather
  than the raw password.
- `job_applications` has a mandatory `user_id` foreign key, so every application belongs to one registered user.
- Both entities maintain `created_at` and `updated_at` timestamps through JPA lifecycle callbacks.

PostgreSQL is the running application's database. H2 is used only for JPA repository integration tests.

## Authentication

### Public authentication endpoints

#### Register

`POST /auth/register`

Request:

```json
{
  "email": "person@example.com",
  "password": "password123"
}
```

The email must be valid and no longer than 254 characters. Registration passwords must contain at least 8 characters.
Emails are trimmed and normalized to lowercase before storage.

Successful response:

```json
{
  "id": 1,
  "email": "person@example.com",
  "createdAt": "2026-07-27T10:00:00"
}
```

The stored BCrypt password hash is not included in the response.

#### Log in

`POST /auth/login`

Request:

```json
{
  "email": "person@example.com",
  "password": "password123"
}
```

Successful response:

```json
{
  "accessToken": "signed-jwt-value",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

`expiresIn` is expressed in seconds.

### Authentication flow

1. Register with `POST /auth/register`.
2. Log in with `POST /auth/login`.
3. Copy the returned `accessToken`.
4. Send it to protected endpoints with the header `Authorization: Bearer <accessToken>`.

## Security

These authentication routes remain public, and only for POST requests:

- `POST /auth/register`
- `POST /auth/login`

`/applications` and `/applications/**` always require a valid JWT bearer token. Swagger UI and OpenAPI routes are
public only when `app.swagger.public=true`; otherwise they require authentication. Every other unmatched route also
requires authentication, including actuator health.

Tokens are signed and verified with HS256. The decoder validates the signature and standard timestamps, including
expiration, and requires a positive numeric `userId` claim. A missing, invalid, expired, or incomplete bearer token is
rejected before an application controller runs.

The server creates the `userId` claim from the authenticated database user during login and uses it to assign new
applications. Collection queries filter by that user ID; individual read, update, and delete queries use both the
application ID and user ID. One user therefore cannot access another user's application through these endpoints.
Making Swagger public does not bypass these authentication or ownership checks.

Requests for a nonexistent application and requests for another user's application both receive a not-found response,
which avoids exposing whether another user's record exists.

## Swagger / OpenAPI

Springdoc generates:

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Swagger is protected by default when `SWAGGER_PUBLIC` is absent or set to `false`. Set `SWAGGER_PUBLIC=true` to allow
anonymous local access to Swagger UI, the OpenAPI document, and their supporting routes. Docker Compose enables this
local documentation access by default unless the host overrides `SWAGGER_PUBLIC`.

This setting opens only the documentation routes. Registration and login remain public, while `/applications` stays
JWT-protected. To call application endpoints from Swagger UI:

1. Register and log in through the public authentication endpoints.
2. Copy the `accessToken` value from the login response.
3. Click **Authorize** and paste only the token value.
4. Swagger adds the `Bearer` prefix automatically when it sends requests.

The application remains stateless. Public documentation access does not create a browser login session, expose job
application data, or bypass ownership enforcement.

## Health Check

Only `GET /actuator/health` is exposed, and it currently requires bearer authentication:

```bash
curl -i "http://localhost:8080/actuator/health" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

A healthy response includes:

```json
{
  "status": "UP"
}
```

## API Endpoints

| Method | Endpoint                 | Authentication | Description                                      |
|--------|--------------------------|----------------|--------------------------------------------------|
| POST   | `/auth/register`         | Public         | Register a user                                  |
| POST   | `/auth/login`            | Public         | Log in and receive a JWT access token            |
| GET    | `/applications`          | Bearer token   | List the authenticated user's applications       |
| GET    | `/applications/{id}`     | Bearer token   | Get an owned application                         |
| POST   | `/applications`          | Bearer token   | Create an application for the authenticated user |
| PATCH  | `/applications/{id}`     | Bearer token   | Partially update an owned application             |
| DELETE | `/applications/{id}`     | Bearer token   | Delete an owned application                      |
| GET    | `/actuator/health`       | Bearer token   | Check application health                         |
| GET    | `/swagger-ui/index.html` | Conditional    | View Swagger UI                                  |
| GET    | `/v3/api-docs`           | Conditional    | Get the OpenAPI document                         |

Swagger/OpenAPI routes are public only when `SWAGGER_PUBLIC=true`; otherwise they require authentication.

## Query Parameters

`GET /applications` supports:

| Parameter | Default        | Example        | Description                             |
|-----------|----------------|----------------|-----------------------------------------|
| `status`  | none           | `interview`    | Filter by status                        |
| `search`  | none           | `java`         | Search company and title                |
| `sort_by` | `date_applied` | `created_at`   | Select the sort field                   |
| `order`   | `desc`         | `asc`          | Select `asc` or `desc`                  |
| `limit`   | `10`           | `5`            | Set the number of records per page      |
| `page`    | `0`            | `1`            | Select a zero-based page                |

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

The following examples use a shell such as Bash, zsh, or Git Bash.

### Register a user

```bash
curl -i -X POST "http://localhost:8080/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "person@example.com",
    "password": "password123"
  }'
```

### Log in

```bash
curl -i -X POST "http://localhost:8080/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "person@example.com",
    "password": "password123"
  }'
```

Copy the `accessToken` value from the login response and save it:

```bash
export ACCESS_TOKEN="<paste accessToken here>"
```

### List the authenticated user's applications

```bash
curl -i "http://localhost:8080/applications" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

Filtering, searching, sorting, and pagination can be combined:

```bash
curl -i "http://localhost:8080/applications?status=interview&search=java&sort_by=date_applied&order=asc&limit=5&page=0" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### Create an application

```bash
curl -i -X POST "http://localhost:8080/applications" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "company": "Amazon",
    "title": "Backend Developer",
    "status": "applied",
    "dateApplied": "2026-07-06",
    "notes": "Applied through LinkedIn"
  }'
```

`POST /applications` requires `company`, `title`, `status`, and `dateApplied`; `notes` is optional. Company and title
must not be blank and must not exceed 255 characters. Status must be one of `applied`, `interview`, `offer`, or
`rejected`.

### Update an owned application

```bash
curl -i -X PATCH "http://localhost:8080/applications/1" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "interview",
    "notes": "Recruiter screen scheduled"
  }'
```

All PATCH fields may be omitted. When supplied, company and title must not be blank and must not exceed 255
characters, while status must be one of `applied`, `interview`, `offer`, or `rejected`. `dateApplied` and `notes`
remain optional.

### Delete an owned application

```bash
curl -i -X DELETE "http://localhost:8080/applications/1" \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### Short Windows PowerShell token example

```powershell
$loginBody = @{
    email = "person@example.com"
    password = "password123"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/auth/login" `
    -ContentType "application/json" `
    -Body $loginBody

$accessToken = $loginResponse.accessToken

Invoke-RestMethod `
    -Method Get `
    -Uri "http://localhost:8080/applications" `
    -Headers @{ Authorization = "Bearer $accessToken" }
```

## Example Job Application Response

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

The global exception handler returns structured JSON for application-level errors such as validation failures,
duplicate registration, invalid login credentials, missing resources, and invalid query parameters.

### Example 404 response

```json
{
  "timestamp": "2026-07-07T10:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Job application not found with id: 999"
}
```

### Example validation response

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

Authentication failures are handled by Spring Security before the controller and may use Spring Security's response
format rather than the global exception-handler format.

## Run Tests

The project currently has 63 passing tests. The automated test suite includes:

- JWT configuration and required-claim validation tests
- Real JWT signing and decoding tests
- Authentication controller, service, and endpoint-security tests
- Job application controller, service, and endpoint-security tests
- Configurable Swagger route-security tests covering anonymous documentation access when enabled, protected behavior
  when disabled, and `/applications` remaining authenticated when Swagger is public
- H2-backed JPA repository integration tests for real ownership-scoped queries
- Request-validation and error-response tests, including regression coverage for blank and overlong application fields

PostgreSQL remains the application database; H2 is used only by the repository integration tests.

Run a fresh build and test suite on Windows:

```powershell
.\mvnw.cmd clean test
```

Run a fresh build and test suite on macOS/Linux:

```bash
./mvnw clean test
```

## CI

GitHub Actions runs the Maven test suite automatically for:

- pushes to `main` and `dev`
- pull requests targeting `main` or `dev`

Workflow file:

- `.github/workflows/ci.yml`

The workflow uses Java 21, the Maven wrapper, Maven dependency caching, and a fixed test-only JWT secret on an Ubuntu runner.

## Project Highlights

This project demonstrates:

- Designing a readable layered REST API with Spring Boot
- Persisting and querying relational data with PostgreSQL, Spring Data JPA, and Hibernate
- Registering users, normalizing email addresses, and hashing passwords with BCrypt
- Issuing and validating HS256 JWTs with stateless Spring Security authentication
- Validating required claims and enforcing per-user ownership
- Separating API and persistence models with DTOs
- Applying Jakarta Bean Validation and consistent application-level error handling
- Testing authentication, JWTs, controllers, services, security, and ownership behavior
- Using Docker Compose, Swagger/OpenAPI, and GitHub Actions CI
