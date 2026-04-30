# Multi-Tenant Monitoring System

A microservices-based monitoring system that allows tenants to register websites or APIs for health tracking. The system periodically checks registered endpoints and maintains historical health data.

## Services Overview

| Service | Port | Description |
|---------|------|-------------|
| API Gateway | 8080 | Central entry point, routing, JWT validation |
| Auth Service | 8081 | User authentication, JWT token generation |
| Monitoring Service | 8082 | Background health checks, tenant-isolated monitoring |

---

# Auth Service

Authentication service for the multi-tenant monitoring system, built with Spring Boot 3.5.13 and Java 21.

## Features

- User registration with validation
- User login with JWT token generation
- Role-based access control (USER, ADMIN)
- JWT-based stateless authentication
- Redis-backed token blacklisting for logout
- BCrypt password encoding
- Global exception handling

## Tech Stack

- **Framework:** Spring Boot 3.5.13
- **Language:** Java 21
- **Database:** PostgreSQL
- **Cache:** Redis (for token blacklisting)
- **Security:** Spring Security + JWT (io.jsonwebtoken 0.11.5)
- **ORM:** Spring Data JPA / Hibernate
- **Build Tool:** Maven
- **Utilities:** Lombok, Spring Boot DevTools

## Prerequisites

- Java 21 or higher
- PostgreSQL 12+
- Redis 6+
- Maven 3.6+ (or use the included Maven wrapper)

## Getting Started

### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE authdb;
```

Start Redis server:

```bash
# Windows (if using WSL or native Redis)
redis-server

# Or use Docker
docker run -d -p 6379:6379 redis:latest
```

### 2. Configuration

Update `src/main/resources/application.properties` with your database and Redis credentials:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/authdb
spring.datasource.username=postgres
spring.datasource.password=yourpassword

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

**Important:** Change the JWT secret in production:

```properties
jwt.secret=your-secure-random-secret-key-at-least-32-characters
jwt.expiration=86400000
```

### 3. Build the Application

```bash
./mvnw clean install
```

### 4. Run the Application

```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8081`

## API Documentation

### Register a New User

```bash
curl -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "id": "uuid-string",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER"
  }
}
```

### Login

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "john_doe",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "username": "john_doe",
    "email": "john@example.com",
    "role": "USER"
  }
}
```

### Access Protected Endpoints

Include the JWT token in the Authorization header:

```bash
curl -X GET http://localhost:8081/api/auth/me \
  -H "Authorization: Bearer <your-jwt-token>"
```

### Logout

Blacklist the current token in Redis:

```bash
curl -X POST http://localhost:8081/api/auth/logout \
  -H "Authorization: Bearer <your-jwt-token>"
```

**Response:**
```json
{
  "success": true,
  "message": "Logged out successfully",
  "data": null
}
```

After logout, the token is stored in Redis and will be rejected until it naturally expires.

## Project Structure

```
src/main/java/com/raj/monitoring/authservice/
├── AuthServiceApplication.java      # Main entry point
├── config/
│   └── RedisConfig.java             # Redis template configuration
├── controller/
│   └── AuthController.java          # REST endpoints
├── dto/
│   ├── ApiResponse.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── RegisterRequest.java
│   └── RegisterResponse.java
├── entity/
│   ├── User.java                    # User entity
│   └── Role.java                    # Role enum
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── ResourceAlreadyExistsException.java
├── repository/
│   └── UserRepository.java          # Data access layer
├── security/
│   ├── JwtService.java              # JWT token operations
│   ├── JwtAuthenticationFilter.java # JWT filter (checks blacklist)
│   └── SecurityConfig.java          # Security configuration
└── service/
    ├── AuthService.java             # Business logic
    ├── CustomUserDetailsService.java # User details service
    └── TokenBlacklistService.java   # Redis token blacklisting
```

## Validation Rules

| Field | Constraints |
|-------|-------------|
| username | Required, 3-50 characters |
| email | Required, valid email format |
| password | Required, minimum 8 characters |

## Error Handling

The application uses a global exception handler that returns consistent error responses:

| HTTP Status | Error Type |
|-------------|------------|
| 400 | Validation error |
| 401 | Authentication failed / Token revoked |
| 404 | Resource not found |
| 409 | Resource already exists |
| 500 | Internal server error |

## Redis Token Blacklist

When a user logs out:
1. The JWT token is extracted from the Authorization header
2. Remaining token TTL is calculated from expiration time
3. Token is stored in Redis with key pattern: `blacklist:token:<token>`
4. Redis TTL is set to match the token's remaining validity
5. `JwtAuthenticationFilter` checks all incoming requests against blacklist
6. Blacklisted tokens receive 401 Unauthorized with message "Token has been revoked"

**Redis Key Format:** `blacklist:token:<jwt-token-string>`

**Automatic Cleanup:** Tokens automatically expire from Redis after their original JWT expiration time, preventing unbounded Redis growth.

## Testing

Run tests with:

```bash
./mvnw test
```

## Build for Production

```bash
./mvnw clean package -DskipTests
```

The JAR file will be created at `target/auth-service-0.0.1-SNAPSHOT.jar`

Run the production JAR:

```bash
java -jar target/auth-service-0.0.1-SNAPSHOT.jar
```

## Security Considerations

1. **JWT Secret:** Use a strong, randomly generated secret (at least 32 characters)
2. **Password Policy:** Implement stronger password requirements in production
3. **HTTPS:** Always use HTTPS in production environments
4. **Rate Limiting:** Consider adding rate limiting for auth endpoints
5. **Account Lockout:** Implement account lockout after failed login attempts
6. **Token Expiration:** Adjust JWT expiration based on security requirements
7. **Redis Security:** Secure Redis with authentication in production
8. **Token Blacklist:** Ensure Redis persistence for token blacklist reliability

---

# Monitoring Service

Background-driven microservice that allows users (segregated by tenant) to register websites or APIs for tracking. It stores these monitors in a database and uses a scheduled job to periodically send HTTP requests to each active endpoint, measuring response time and availability (UP/DOWN).

## Features

- Multi-tenant monitoring with tenant isolation via `X-Tenant-ID` header
- Configurable check intervals (minimum 10 seconds)
- Response time tracking and UP/DOWN status monitoring
- Historical health check logs
- Background scheduler running every 5 seconds
- WebFlux-based non-blocking HTTP health checks

## Tech Stack

- **Framework:** Spring Boot 3.5.13
- **Language:** Java 21
- **Database:** PostgreSQL (monitor configs + health logs)
- **Cache:** Redis (shared with other services)
- **Security:** Spring Security + JWT (io.jsonwebtoken 0.11.5)
- **ORM:** Spring Data JPA / Hibernate
- **HTTP Client:** Spring WebFlux WebClient
- **Build Tool:** Maven
- **Utilities:** Lombok, Spring Boot DevTools

## Prerequisites

- Java 21 or higher
- PostgreSQL 12+
- Redis 6+
- Maven 3.6+ (or use the included Maven wrapper)

## Getting Started

### 1. Database Setup

Create a PostgreSQL database:

```sql
CREATE DATABASE monitoringdb;
```

### 2. Configuration

Update `src/main/resources/application.properties`:

```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/monitoringdb
spring.datasource.username=postgres
spring.datasource.password=yourpassword

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# JWT (must match auth-service and api-gateway)
jwt.secret=c2VjdXJlLXNlY3JldC1rZXktZm9yLWp3dC10b2tlbi1nZW5lcmF0aW9uLW1pbi0zMi1jaGFycw==
```

### 3. Build and Run

```bash
./mvnw clean install
./mvnw spring-boot:run
```

The application will start on `http://localhost:8082`

## API Documentation

All endpoints require:
- `Authorization: Bearer <jwt-token>` header
- `X-Tenant-ID: <tenant-id>` header for tenant isolation

### Create a Monitor

```bash
curl -X POST http://localhost:8082/api/monitors \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "X-Tenant-ID: tenant-123" \
  -d '{
    "name": "Google Homepage",
    "url": "https://www.google.com",
    "checkIntervalSeconds": 30,
    "timeoutSeconds": 5
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Monitor created successfully",
  "data": {
    "id": "uuid-string",
    "tenantId": "tenant-123",
    "name": "Google Homepage",
    "url": "https://www.google.com",
    "checkIntervalSeconds": 30,
    "timeoutSeconds": 5,
    "status": "UNKNOWN",
    "createdAt": "2026-04-30T12:00:00Z"
  }
}
```

### List All Monitors

```bash
curl -X GET http://localhost:8082/api/monitors \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "X-Tenant-ID: tenant-123"
```

### Get Monitor Details

```bash
curl -X GET http://localhost:8082/api/monitors/{id} \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "X-Tenant-ID: tenant-123"
```

### Update Monitor

```bash
curl -X PUT http://localhost:8082/api/monitors/{id} \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "X-Tenant-ID: tenant-123" \
  -d '{
    "name": "Updated Name",
    "checkIntervalSeconds": 60
  }'
```

### Delete Monitor

```bash
curl -X DELETE http://localhost:8082/api/monitors/{id} \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "X-Tenant-ID: tenant-123"
```

### Get Health Check Logs

```bash
curl -X GET http://localhost:8082/api/monitors/{id}/logs \
  -H "Authorization: Bearer <your-jwt-token>" \
  -H "X-Tenant-ID: tenant-123"
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": "log-uuid",
      "monitorId": "monitor-uuid",
      "status": "UP",
      "responseTimeMs": 150,
      "statusCode": 200,
      "errorMessage": null,
      "createdAt": "2026-04-30T12:05:00Z"
    }
  ]
}
```

## Project Structure

```
src/main/java/com/raj/monitoring/monitoringservice/
├── MonitoringServiceApplication.java    # Main entry point
├── client/
│   ├── HttpClient.java                  # WebFlux health check client
│   └── HealthCheckResult.java           # Health check result DTO
├── config/
│   ├── RedisConfig.java                 # Redis configuration
│   ├── SecurityConfig.java              # Security configuration
│   ├── WebClientConfig.java             # WebClient bean
│   └── SchedulerConfig.java             # Thread pool scheduler
├── controller/
│   └── MonitorController.java           # REST endpoints
├── dto/
│   ├── ApiResponse.java
│   ├── CreateMonitorRequest.java
│   ├── UpdateMonitorRequest.java
│   ├── MonitorResponse.java
│   └── MonitorLogResponse.java
├── entity/
│   ├── Monitor.java                     # Monitor entity
│   ├── MonitorLog.java                  # Health check log entity
│   └── MonitorStatus.java               # UP/DOWN/UNKNOWN enum
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   ├── ResourceAlreadyExistsException.java
│   └── TenantNotFoundException.java
├── repository/
│   ├── MonitorRepository.java           # Monitor data access
│   └── MonitorLogRepository.java        # Log data access
├── scheduler/
│   └── HealthCheckScheduler.java        # Scheduled health checks
├── security/
│   ├── JwtService.java                  # JWT validation
│   └── JwtAuthenticationFilter.java     # JWT authentication filter
├── service/
│   └── MonitorService.java              # Business logic
└── util/
    ├── TenantContext.java               # ThreadLocal tenant holder
    └── TenantHeaderFilter.java          # Extracts X-Tenant-ID header
```

## Validation Rules

| Field | Constraints |
|-------|-------------|
| name | Required, 3-100 characters |
| url | Required, valid URL format |
| checkIntervalSeconds | Required, minimum 10 seconds |
| timeoutSeconds | Required, minimum 1 second |

## Error Handling

| HTTP Status | Error Type |
|-------------|------------|
| 400 | Validation error / Missing tenant header |
| 401 | Authentication failed / Invalid JWT |
| 403 | Access denied |
| 404 | Monitor not found |
| 409 | URL already exists for tenant |
| 500 | Internal server error |

## Scheduler Behavior

The `HealthCheckScheduler` runs every 5 seconds:

1. Queries monitors where `nextCheckAt <= now`
2. Executes HTTP health check for each due monitor
3. Records response time and status code
4. Updates monitor status (UP/DOWN) and `nextCheckAt`
5. Persists health check result to `monitor_logs` table

**Redis Key Format:** `blacklist:token:<jwt-token-string>`

## Multi-Tenancy

Tenant isolation is implemented at the application layer:

1. **X-Tenant-ID Header** - Client sends tenant identifier
2. **TenantHeaderFilter** - Extracts header value
3. **TenantContext** - Stores in ThreadLocal for current request
4. **Service Layer** - All queries filtered by tenantId
5. **Repository Layer** - Enforces tenant scoping in queries

Tenants cannot access or modify other tenants' data.

## Testing

```bash
./mvnw test
```

## Build for Production

```bash
./mvnw clean package -DskipTests
```

Run the production JAR:

```bash
java -jar target/monitoring-service-0.0.1-SNAPSHOT.jar
```

## Security Considerations

1. **JWT Secret:** Must match auth-service and api-gateway
2. **Tenant Validation:** Validate tenant ID against known tenants in production
3. **HTTPS:** Always use HTTPS in production
4. **Rate Limiting:** Consider rate limiting for health check endpoints
5. **Redis Security:** Secure Redis with authentication
6. **Database Isolation:** Consider separate schemas per tenant for strict isolation

---

# API Gateway

The API Gateway serves as the central entry point for all client requests, routing to appropriate backend services.

## Configuration

Routes are configured in `application.properties`:

```properties
# Auth Service Route
spring.cloud.gateway.routes[0].id=auth-service
spring.cloud.gateway.routes[0].uri=http://localhost:8081
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/auth/**

# Monitoring Service Route
spring.cloud.gateway.routes[1].id=monitoring-service
spring.cloud.gateway.routes[1].uri=http://localhost:8082
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/monitors/**
```

## Running Through Gateway

All endpoints can be accessed via the gateway on port 8080:

- Auth: `http://localhost:8080/api/auth/**`
- Monitoring: `http://localhost:8080/api/monitors/**`

---

# Full System Usage Example

### 1. Register and Login

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "john", "email": "john@test.com", "password": "password123"}'

# Login - save the token from response
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### 2. Create Monitor (via Gateway)

```bash
curl -X POST http://localhost:8080/api/monitors \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: my-tenant" \
  -d '{"name": "My API", "url": "https://api.example.com/health", "checkIntervalSeconds": 30, "timeoutSeconds": 5}'
```

### 3. View Monitors and Logs

```bash
# List monitors
curl -X GET http://localhost:8080/api/monitors \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: my-tenant"

# View health logs
curl -X GET http://localhost:8080/api/monitors/{monitor-id}/logs \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: my-tenant"
```

---

# License

This project is part of the multi-tenant monitoring system.
