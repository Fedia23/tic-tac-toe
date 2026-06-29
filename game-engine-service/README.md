# Game Engine Service - Tic Tac Toe

A microservice that manages the core game logic for a distributed Tic Tac Toe application.

## Features

- RESTful API for game management
- In-memory game state storage
- Move validation
- Win/Draw detection
- Comprehensive error handling
- **OpenAPI/Swagger documentation**
- **CORS support for frontend integration**
- **Rate limiting protection (100 requests/minute per IP)**

## Prerequisites

- Java 17 or higher
- Maven 3.6+

## Running the Service

```bash
./mvnw spring-boot:run
```

The service will start on port **8081**.

## API Documentation

Once the service is running, you can access:

- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8081/v3/api-docs

The Swagger UI provides an interactive interface to:
- View all available endpoints
- Test API calls directly from the browser
- See request/response schemas
- Review error responses

## Security & Performance Features

### CORS Configuration
The service is configured to allow cross-origin requests from:
- `http://localhost:3000` (React default)
- `http://localhost:4200` (Angular default)
- `http://localhost:8080` (Vue default)

Configure additional origins in `application.properties`:
```properties
cors.allowed-origins=http://localhost:3000
```

### Rate Limiting
The service implements IP-based rate limiting:
- **Limit**: 100 requests per minute per IP
- **Response Headers**:
  - `X-RateLimit-Limit`: Maximum requests allowed
  - `X-RateLimit-Remaining`: Requests remaining in current window
  - `X-RateLimit-Reset`: Seconds until rate limit resets
- **429 Response**: When limit exceeded

Configure rate limits in `application.properties`:
```properties
rate-limit.capacity=100
rate-limit.refill-tokens=100
rate-limit.refill-duration-minutes=1
```

## API Endpoints

### 1. Create a New Game

**POST** `/games/{gameId}`

Creates a new game with the specified ID.

**Response:**
```json
{
  "gameId": "game1",
  "board": [[null, null, null], [null, null, null], [null, null, null]],
  "status": "IN_PROGRESS",
  "currentTurn": "X",
  "winner": null
}
```

### 2. Make a Move

**POST** `/games/{gameId}/move`

Makes a move in the game.

**Request Body:**
```json
{
  "player": "X",
  "row": 0,
  "col": 0
}
```

**Response:**
```json
{
  "gameId": "game1",
  "board": [["X", null, null], [null, null, null], [null, null, null]],
  "status": "IN_PROGRESS",
  "winner": null,
  "nextTurn": "O",
  "message": "Move successful. Player O's turn next."
}
```

### 3. Get Game State

**GET** `/games/{gameId}`

Retrieves the current state of a game.

**Response:**
```json
{
  "gameId": "game1",
  "board": [["X", "O", null], [null, null, null], [null, null, null]],
  "status": "IN_PROGRESS",
  "currentTurn": "X",
  "winner": null
}
```

### 4. Delete a Game

**DELETE** `/games/{gameId}`

Deletes a game from memory.

## Game Status Values

- `IN_PROGRESS` - Game is still being played
- `X_WINS` - Player X has won
- `O_WINS` - Player O has won
- `DRAW` - Game ended in a draw

## Error Handling

The service handles the following error scenarios:

- **404 Not Found**: Game does not exist
- **400 Bad Request**: Invalid move (cell occupied, wrong turn, invalid coordinates)
- **409 Conflict**: Attempting to move in a finished game
- **400 Validation Error**: Invalid request format

## Example Usage with cURL

```bash
# Create a new game
curl -X POST http://localhost:8081/games/game1

# Make a move for Player X
curl -X POST http://localhost:8081/games/game1/move \
  -H "Content-Type: application/json" \
  -d '{"player": "X", "row": 0, "col": 0}'

# Make a move for Player O
curl -X POST http://localhost:8081/games/game1/move \
  -H "Content-Type: application/json" \
  -d '{"player": "O", "row": 1, "col": 1}'

# Get game state
curl http://localhost:8081/games/game1

# Delete a game
curl -X DELETE http://localhost:8081/games/game1
```

## Testing

The project includes comprehensive unit tests for the game service covering:
- Game creation and retrieval
- Move validation (occupied cells, wrong turns, finished games)
- Win condition detection (horizontal, vertical, diagonal)
- Draw detection
- Turn switching
- Error handling

Run the tests with:

```bash
./mvnw test
```

**Test Coverage:**
- `GameServiceTest`: 18 unit tests covering all game logic scenarios
- All win conditions (3 horizontal, 3 vertical, 2 diagonal)
- Draw scenarios
- Input validation
- Exception handling

## Health Check

Check the service health:

```bash
curl http://localhost:8081/actuator/health
```