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

The application will start on `http://localhost:8080`

## API Documentation

### Register a New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
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
curl -X POST http://localhost:8080/api/auth/login \
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
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <your-jwt-token>"
```

### Logout

Blacklist the current token in Redis:

```bash
curl -X POST http://localhost:8080/api/auth/logout \
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

## License

This project is part of the multi-tenant monitoring system.
