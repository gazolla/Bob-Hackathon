# ToDo Java PostgreSQL Application

A modern Spring Boot 3.3+ REST API application for managing tasks, built with Java 21 and PostgreSQL.

## 🚀 Features

- **RESTful API** - Complete CRUD operations for tasks
- **Spring Boot 3.3+** - Latest Spring Boot framework with Java 21
- **PostgreSQL** - Robust relational database
- **Flyway** - Database migration management
- **Bean Validation** - Request validation with Jakarta Validation
- **Global Exception Handling** - Centralized error handling
- **Docker Support** - Containerized application with Docker Compose
- **Clean Architecture** - Separation of concerns with DTOs, Services, and Controllers

## 📋 Prerequisites

- **Java 21** or higher
- **Maven 3.8+** (or use included Maven wrapper)
- **Docker & Docker Compose** (for containerized deployment)
- **PostgreSQL 16** (if running without Docker)

## 🏗️ Project Structure

```
todo-java-postgres/
├── src/
│   ├── main/
│   │   ├── java/com/example/todo/
│   │   │   ├── TodoApplication.java          # Main application class
│   │   │   ├── controller/
│   │   │   │   └── TaskController.java       # REST API endpoints
│   │   │   ├── service/
│   │   │   │   └── TaskService.java          # Business logic
│   │   │   ├── repository/
│   │   │   │   └── TaskRepository.java       # Data access layer
│   │   │   ├── model/
│   │   │   │   └── Task.java                 # Entity model
│   │   │   ├── dto/
│   │   │   │   ├── TaskRequest.java          # Request DTO
│   │   │   │   └── TaskResponse.java         # Response DTO
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       └── ResourceNotFoundException.java
│   │   └── resources/
│   │       ├── application.properties        # Application configuration
│   │       └── db/migration/
│   │           └── V1__Create_tasks_table.sql # Database schema
│   └── test/
├── Dockerfile                                 # Docker image definition
├── docker-compose.yml                         # Docker Compose configuration
└── pom.xml                                    # Maven dependencies
```

## 🔧 Configuration

### Database Configuration

The application uses PostgreSQL with the following default settings:

```properties
Database: tododb
Username: todouser
Password: todopass
Port: 5432
```

You can modify these in `src/main/resources/application.properties` or via environment variables.

## 🚀 Getting Started

### Option 1: Run with Docker Compose (Recommended)

1. **Start PostgreSQL database:**
   ```bash
   cd todo-java-postgres
   docker-compose up -d postgres
   ```

2. **Wait for PostgreSQL to be ready** (about 10 seconds)

3. **Run the application locally:**
   ```bash
   ./mvnw spring-boot:run
   ```

   Or on Windows:
   ```bash
   mvnw.cmd spring-boot:run
   ```

The application will start on `http://localhost:8080`

### Option 2: Run Everything with Docker Compose

```bash
cd todo-java-postgres
docker-compose up --build
```

This will:
- Build the application Docker image
- Start PostgreSQL container
- Start the application container
- Run Flyway migrations automatically

### Option 3: Run Locally (without Docker)

1. **Install and start PostgreSQL 16**

2. **Create database and user:**
   ```sql
   CREATE DATABASE tododb;
   CREATE USER todouser WITH PASSWORD 'todopass';
   GRANT ALL PRIVILEGES ON DATABASE tododb TO todouser;
   ```

3. **Run the application:**
   ```bash
   ./mvnw spring-boot:run
   ```

## 📡 API Endpoints

### Base URL
```
http://localhost:8080/api/tasks
```

### Endpoints

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| GET | `/api/tasks` | Get all tasks | - |
| GET | `/api/tasks?status=PENDING` | Get tasks by status | - |
| GET | `/api/tasks/{id}` | Get task by ID | - |
| POST | `/api/tasks` | Create new task | TaskRequest |
| PUT | `/api/tasks/{id}` | Update task | TaskRequest |
| DELETE | `/api/tasks/{id}` | Delete task | - |

### Task Status Values
- `PENDING`
- `IN_PROGRESS`
- `COMPLETED`

### Request/Response Examples

#### Create Task (POST /api/tasks)
```json
{
  "title": "Complete project documentation",
  "description": "Write comprehensive README and API docs",
  "status": "PENDING",
  "dueDate": "2026-05-10T15:00:00"
}
```

#### Response
```json
{
  "id": 1,
  "title": "Complete project documentation",
  "description": "Write comprehensive README and API docs",
  "status": "PENDING",
  "dueDate": "2026-05-10T15:00:00",
  "createdAt": "2026-05-02T12:00:00",
  "updatedAt": "2026-05-02T12:00:00"
}
```

#### Update Task (PUT /api/tasks/1)
```json
{
  "title": "Complete project documentation",
  "description": "Write comprehensive README and API docs",
  "status": "IN_PROGRESS",
  "dueDate": "2026-05-10T15:00:00"
}
```

## 🧪 Testing the API

### Using cURL

**Get all tasks:**
```bash
curl http://localhost:8080/api/tasks
```

**Create a task:**
```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Test Task",
    "description": "This is a test task",
    "status": "PENDING",
    "dueDate": "2026-05-10T15:00:00"
  }'
```

**Get task by ID:**
```bash
curl http://localhost:8080/api/tasks/1
```

**Update a task:**
```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated Task",
    "description": "Updated description",
    "status": "COMPLETED",
    "dueDate": "2026-05-10T15:00:00"
  }'
```

**Delete a task:**
```bash
curl -X DELETE http://localhost:8080/api/tasks/1
```

**Filter by status:**
```bash
curl http://localhost:8080/api/tasks?status=PENDING
```

## 🛠️ Development

### Build the project
```bash
./mvnw clean package
```

### Run tests
```bash
./mvnw test
```

### Build Docker image
```bash
docker build -t todo-java-postgres .
```

## 🗄️ Database Migrations

The application uses Flyway for database migrations. Migration scripts are located in:
```
src/main/resources/db/migration/
```

Migrations run automatically on application startup.

## 🐛 Troubleshooting

### Port 8080 already in use
```bash
# Find and kill the process using port 8080
lsof -ti:8080 | xargs kill -9
```

### PostgreSQL connection refused
- Ensure PostgreSQL is running: `docker-compose ps`
- Check database credentials in `application.properties`
- Verify PostgreSQL is healthy: `docker-compose logs postgres`

### Maven wrapper permission denied
```bash
chmod +x mvnw
```

## 📚 Technology Stack

- **Java 21** - Latest LTS version
- **Spring Boot 3.3.0** - Application framework
- **Spring Data JPA** - Data persistence
- **PostgreSQL 16** - Database
- **Flyway** - Database migrations
- **Jakarta Validation** - Bean validation
- **Maven** - Build tool
- **Docker** - Containerization

## 📝 License

This project is open source and available under the MIT License.

## 👨‍💻 Author

Created as a demonstration of modern Spring Boot development practices with Java 21.