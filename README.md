# User Management Microservice

This microservice handles user management functionality for the food ordering application, including authentication, authorization, and user profile management.

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- PostgreSQL 12 or higher
- Docker (optional)

## Setup Instructions

1. Clone the repository
2. Create a PostgreSQL database named `foodorder`
3. Update the database credentials in `src/main/resources/application.yml` if needed
4. Set the JWT secret key as an environment variable or update it in the application.yml

## Running the Application

### Using Maven

```bash
mvn spring-boot:run
```

### Using Docker

```bash
docker build -t user-management-service .
docker run -p 8080:8080 user-management-service
```

## API Documentation

The service exposes REST APIs under the base path `/api/v1`. Key endpoints include:

- POST `/auth/register` - Register a new user
- POST `/auth/login` - Login and get JWT token
- GET `/users/profile` - Get user profile
- PUT `/users/profile` - Update user profile

## Security

The service uses JWT-based authentication. Include the JWT token in the Authorization header for protected endpoints:

```
Authorization: Bearer <your-jwt-token>
```

## Testing

Run the test suite using:

```bash
mvn test
```

## Monitoring

The application exposes actuator endpoints for monitoring:

- Health check: `/actuator/health`
- Metrics: `/actuator/metrics`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request 