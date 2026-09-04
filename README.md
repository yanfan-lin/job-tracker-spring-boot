# Job Tracker Spring Boot API

[![My Skills](https://skillicons.dev/icons?i=java,spring,maven,hibernate,postgres,docker,git,github,githubactions)](https://skillicons.dev)

![Java CI](https://github.com/yanfan-lin/job-tracker-spring-boot/actions/workflows/ci.yml/badge.svg)

## Project Overview

Job Tracker is a Spring Boot REST API for managing job applications. Users authenticate with JWTs and can access
only their own records.

This project rebuilds an earlier [FastAPI version](https://github.com/yanfan-lin/job-tracker-API) using Java and
Spring Boot.

## Features

- Registration and login with normalized emails, BCrypt hashing, and signed JWTs
- Create, view, update, and delete user-owned job applications
- Filtering, searching, sorting, and pagination
- Request validation and structured error responses

## Local Setup

### Requirements

- Java 21
- Docker with Compose

The Maven wrapper is included, so Maven does not need to be installed separately.

### Clone the repository

```bash
git clone https://github.com/yanfan-lin/job-tracker-spring-boot.git
cd job-tracker-spring-boot
```

### Set the JWT secret

`JWT_SECRET` must be Base64-encoded and decode to at least 32 bytes (256 bits). Generate a separate secret for
each environment. These commands create a temporary secret for local development.

#### Windows PowerShell

```powershell
$secretBytes = New-Object byte[] 32
$random = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$random.GetBytes($secretBytes)
$random.Dispose()
$env:JWT_SECRET = [Convert]::ToBase64String($secretBytes)
```

#### macOS/Linux shell

```bash
export JWT_SECRET="$(openssl rand -base64 32)"
```

### Run with Docker Compose

```bash
docker compose up --build
```

Docker Compose starts the API at http://localhost:8080 and PostgreSQL at `localhost:5433`. Swagger is public by
default; set `SWAGGER_PUBLIC=false` before starting to protect it.

Stop the containers with:

```bash
docker compose down
```

Add `-v` to remove the database volume:

```bash
docker compose down -v
```

### Run the API locally

Start PostgreSQL with Docker:

```bash
docker compose up -d db
```

#### Windows PowerShell

```powershell
.\mvnw.cmd spring-boot:run
```

#### macOS/Linux shell

```bash
./mvnw spring-boot:run
```

### Environment variables

| Variable            | Local default                                  | Purpose                                                 |
|---------------------|------------------------------------------------|---------------------------------------------------------|
| `DATABASE_URL`      | `jdbc:postgresql://localhost:5433/job_tracker` | PostgreSQL connection URL                               |
| `DATABASE_USERNAME` | `postgres`                                     | PostgreSQL username                                     |
| `DATABASE_PASSWORD` | `postgres`                                     | PostgreSQL password                                     |
| `JWT_SECRET`        | Required                                       | Base64 signing secret that decodes to at least 32 bytes |
| `SWAGGER_PUBLIC`    | `false` (`true` with Compose)                  | Allow public Swagger and OpenAPI access                 |

These defaults are for local development, not production.

## Authentication and Security

Registration and login are public. Emails are normalized before storage, passwords are hashed with BCrypt, and login
returns a signed JWT that expires after one hour.

1. Register with `POST /auth/register`.
2. Log in with `POST /auth/login`.
3. Copy the returned `accessToken`.
4. Send it to protected endpoints with the header `Authorization: Bearer <accessToken>`.

Authentication is stateless. Tokens use HS256 and must have a valid expiration and a positive numeric `userId` claim.
Missing, invalid, expired, or incomplete tokens are rejected.

The API uses the authenticated user ID to limit every job application operation to that user's records. Requests for
missing applications and applications owned by another user both return `404 Not Found`.

## Swagger / OpenAPI

- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Swagger is protected by default. Set `SWAGGER_PUBLIC=true` to allow public documentation access; Docker Compose does
this by default. Application endpoints always require authentication.

To call protected endpoints in Swagger UI, log in, click **Authorize**, and paste the `accessToken`. Swagger adds the
`Bearer` prefix automatically.

## API Endpoints

| Method | Endpoint                 | Authentication | Description                                      |
|--------|--------------------------|----------------|--------------------------------------------------|
| POST   | `/auth/register`         | Public         | Register a user                                  |
| POST   | `/auth/login`            | Public         | Log in and receive a JWT access token            |
| GET    | `/applications`          | Bearer token   | List the authenticated user's applications       |
| GET    | `/applications/{id}`     | Bearer token   | Get an owned application                         |
| POST   | `/applications`          | Bearer token   | Create an application for the authenticated user |
| PATCH  | `/applications/{id}`     | Bearer token   | Partially update an owned application            |
| DELETE | `/applications/{id}`     | Bearer token   | Delete an owned application                      |
| GET    | `/swagger-ui/index.html` | Conditional    | View Swagger UI                                  |
| GET    | `/v3/api-docs`           | Conditional    | Get the OpenAPI document                         |

## Query Parameters

`GET /applications` supports:

| Parameter | Default        | Example      | Description                        |
|-----------|----------------|--------------|------------------------------------|
| `status`  | none           | `interview`  | Filter by application status       |
| `search`  | none           | `java`       | Search company and title           |
| `sort_by` | `date_applied` | `created_at` | Select the sort field              |
| `order`   | `desc`         | `asc`        | Select `asc` or `desc`             |
| `limit`   | `10`           | `5`          | Set the number of records per page |
| `page`    | `0`            | `1`          | Select a zero-based page           |

Supported status values are `applied`, `interview`, `offer`, and `rejected`. Supported sort fields are `id`,
`company`, `title`, `status`, `date_applied`, `created_at`, and `updated_at`.

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

### List applications

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

Creating an application requires `company`, `title`, `status`, and `dateApplied`; `notes` is optional. PATCH requests
accept any subset of these fields. Company and title must not be blank or exceed 255 characters.

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

The API returns errors as structured JSON. Validation errors also include field-specific messages:

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

Spring Security handles authentication failures, so their response format may differ.

## Run Tests

The test suite covers validation, error handling, JWTs, access rules, and ownership-scoped queries. The application
uses PostgreSQL; repository tests use H2.

Windows PowerShell:

```powershell
.\mvnw.cmd clean test
```

macOS/Linux:

```bash
./mvnw clean test
```

## CI

[GitHub Actions](.github/workflows/ci.yml) runs Maven tests with Java 21 on pushes to and pull requests targeting
`main` or `dev`.
