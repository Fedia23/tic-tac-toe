# Game Session Service

A Spring Boot microservice that manages tic-tac-toe game sessions and automates gameplay by generating random moves for both players.

## Features

- Create new game sessions
- Simulate automated games with random move generation
- Track game history and move sequences
- Communicate with Game Engine Service for game state management
- Store session data in H2 in-memory database

## Tech Stack

- Java 17
- Spring Boot 4.1.0
- Spring Data JPA
- H2 Database (in-memory)
- Spring Cloud OpenFeign
- Lombok
- Maven

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Game Engine Service running on http://localhost:8081

## Running the Service

### Build and run using Maven:

```bash
./mvnw clean install
./mvnw spring-boot:run
```

The service will start on port 8082 by default.

## API Endpoints

### 1. Create a New Game Session

**POST** `/sessions`

Creates a new game session and initializes the game state in the Game Engine Service.

**Response:**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "gameId": "game-123",
  "status": "CREATED",
  "message": "Game session created successfully"
}
```

### 2. Simulate Automated Game

**POST** `/sessions/{sessionId}/simulate`

Automatically generates and executes random moves for both players until the game concludes.

**Note:** `sessionId` must be a valid UUID format.

**Response:**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "COMPLETED",
  "result": "X_WINS",
  "totalMoves": 7,
  "message": "Game simulation completed"
}
```

### 3. Get Session Details

**GET** `/sessions/{sessionId}`

Retrieves complete session information including game state and move history.

**Note:** `sessionId` must be a valid UUID format.

**Response:**
```json
{
  "sessionId": "550e8400-e29b-41d4-a716-446655440000",
  "gameId": "game-123",
  "status": "COMPLETED",
  "result": "X_WINS",
  "currentBoard": "X,O,X,O,X,O,X, , ",
  "currentPlayer": null,
  "createdAt": "2026-06-26T15:30:00",
  "completedAt": "2026-06-26T15:30:15",
  "moves": [
    {
      "moveNumber": 1,
      "player": "X",
      "position": 4,
      "timestamp": "2026-06-26T15:30:01"
    }
  ]
}
```

### 4. Get All Sessions (Paginated) 🆕

**GET** `/sessions`

Retrieves a paginated list of all game sessions.

**Query Parameters:**
- `page` - Page number (default: 0)
- `size` - Page size (default: 20)
- `sort` - Sort field and direction (e.g., `createdAt,desc`)
- `status` - Filter by status (CREATED, IN_PROGRESS, COMPLETED)

**Examples:**
```bash
# Get first page (20 items)
GET /sessions?page=0&size=20

# Get completed games only
GET /sessions?status=COMPLETED

# Sort by creation date descending
GET /sessions?sort=createdAt,desc
```

**Response:**
```json
{
  "content": [
    {
      "sessionId": "550e8400-e29b-41d4-a716-446655440000",
      "gameId": "game-123",
      "status": "COMPLETED",
      "result": "X_WINS",
      "createdAt": "2026-06-26T15:30:00",
      "completedAt": "2026-06-26T15:30:15",
      "totalMoves": 7
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20
  },
  "totalElements": 1,
  "totalPages": 1
}
```

## Configuration

Application configuration can be found in `src/main/resources/application.properties`:

```properties
# Server Configuration
server.port=8082

# Game Engine Service URL
game.engine.url=http://localhost:8081

# H2 Database
spring.datasource.url=jdbc:h2:mem:gamesessiondb
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

## Database

The service uses an H2 in-memory database. You can access the H2 console at:

**URL:** http://localhost:8082/h2-console

**JDBC URL:** jdbc:h2:mem:gamesessiondb
**Username:** sa
**Password:** (empty)

## Data Model

### GameSession
- `id` - Unique session identifier (UUID)
- `gameId` - Reference to game in Game Engine Service
- `status` - CREATED, IN_PROGRESS, COMPLETED
- `result` - IN_PROGRESS, X_WINS, O_WINS, DRAW
- `currentBoard` - String representation of board state
- `currentPlayer` - X or O
- `createdAt` - Session creation timestamp
- `completedAt` - Session completion timestamp

### Move
- `id` - Unique move identifier
- `sessionId` - Reference to game session
- `moveNumber` - Sequential move number
- `player` - X or O
- `position` - Board position (0-8)
- `timestamp` - Move execution timestamp

## Example Usage

```bash
# Create a new session
curl -X POST http://localhost:8082/sessions

# Response:
# {"sessionId":"550e8400-e29b-41d4-a716-446655440000","gameId":"game-456","status":"CREATED","message":"Game session created successfully"}

# Simulate the game
curl -X POST http://localhost:8082/sessions/550e8400-e29b-41d4-a716-446655440000/simulate

# Response:
# {"sessionId":"550e8400-e29b-41d4-a716-446655440000","status":"COMPLETED","result":"O_WINS","totalMoves":9,"message":"Game simulation completed"}

# Get session details
curl http://localhost:8082/sessions/550e8400-e29b-41d4-a716-446655440000

# Get all sessions (first page, 10 items)
curl "http://localhost:8082/sessions?page=0&size=10"

# Get completed sessions only
curl "http://localhost:8082/sessions?status=COMPLETED"

# Get sessions sorted by creation date
curl "http://localhost:8082/sessions?sort=createdAt,desc"
```

## Error Handling

The service includes comprehensive error handling with detailed error responses:

### Error Response Format
```json
{
  "timestamp": "2026-06-26T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: Invalid UUID format"
}
```

### Validation Error Format 🆕
```json
{
  "timestamp": "2026-06-26T15:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "fieldErrors": {
    "position": "Position must be between 0 and 8",
    "player": "Player must be either 'X' or 'O'"
  }
}
```

### Status Codes
- `200 OK` - Successful operation
- `201 Created` - Session created successfully
- `400 Bad Request` - Invalid request (validation failed, invalid UUID, game already completed)
- `404 Not Found` - Session not found
- `500 Internal Server Error` - Game Engine Service communication errors
- `503 Service Unavailable` - Game Engine Service unavailable

## Architecture

The service follows a layered architecture:

```
Controller Layer (REST API)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
H2 Database

External Communication:
Service Layer → Feign Client → Game Engine Service
```

## Move Generation

The service uses a random move generation strategy:
- Identifies all empty positions on the board
- Randomly selects one valid position
- Submits the move to the Game Engine Service
- Continues until game completion

## API Documentation 🆕

Interactive API documentation is available via Swagger UI:

**Swagger UI:** http://localhost:8082/swagger-ui.html
**OpenAPI JSON:** http://localhost:8082/api-docs

The Swagger UI provides:
- Complete API documentation
- Interactive testing interface
- Request/response examples
- Schema definitions

## Monitoring

Spring Boot Actuator is enabled for monitoring:

**Health Check:** http://localhost:8082/actuator/health

## Development

To run tests:

```bash
./mvnw test
```

To build:

```bash
./mvnw clean package
```