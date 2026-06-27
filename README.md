# Tic Tac Toe - Full Stack Application

Functional Tic Tac Toe application with automated game simulation.
## Project Structure

```
test/
├── game-engine-service/     # Backend - game logic (Spring Boot)
├── game-session-service/    # Backend - session management (Spring Boot)
├── tictactoe-ui/           # Frontend - UI (React)
└── docker-compose.yml      # Docker orchestration
```

## Architecture

### Services:

1. **Game Engine Service** (Port 8081)
   - Tic Tac Toe game logic
   - Move validation
   - Winner determination
   - REST API + Swagger documentation

2. **Game Session Service** (Port 8082)
   - Game session management
   - Automated game simulation
   - Move history persistence (H2)
   - REST API + Swagger documentation

3. **Tic Tac Toe UI** (Port 80/3000)
   - React-based user interface
   - Interactive gameplay
   - Game history browsing

### Technologies:

- **Backend**: Spring Boot 3.2.0, Java 17, H2 Database
- **Frontend**: React 19, Nginx
- **API Documentation**: SpringDoc OpenAPI 3 (Swagger)
- **Containerization**: Docker, Docker Compose

## Quick Start

```bash
# Build and start
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

## Application Access

After starting the application with Docker:

| Service | URL | Description |
|--------|-----|------|
| **Frontend** | http://localhost:3000 | Main UI application |
| **Game Engine API** | http://localhost:8081 | Game logic REST API |
| **Session API** | http://localhost:8082 | Session management REST API |
| **Engine Swagger** | http://localhost:8081/swagger-ui.html | API documentation|
| **Session Swagger** | http://localhost:8082/swagger-ui.html | API documentation |

## Documentation

- **[game-engine-service/README.md](./game-engine-service/README.md)** - Game Engine documentation
- **[game-session-service/README.md](./game-session-service/README.md)** - Game Session documentation
- **[tictactoe-ui/README.md](./tictactoe-ui/README.md)** - UI documentation

## Testing

### Backend Tests:

```bash
# Game Engine Service
cd game-engine-service
./mvnw test

# Game Session Service
cd game-session-service
./mvnw test
```

### Frontend Tests:

```bash
cd tictactoe-ui
npm test
```

## API Endpoints

### Game Engine Service (http://localhost:8081)

```bash
# Create a new game
POST /games

# Make a move
POST /games/{gameId}/moves
{
  "position": 4,
  "player": "X"
}

# Get game state
GET /games/{gameId}
```

### Game Session Service (http://localhost:8082)

```bash
# Create a session
POST /sessions

# Simulate a game
POST /sessions/{sessionId}/simulate

# Get session details
GET /sessions/{sessionId}

# Get all sessions (with pagination)
GET /sessions?page=0&size=20
```

## Development

### Local Development Requirements

- Java 17+
- Maven 3.8+
- Node.js 18+
- Docker & Docker Compose (optional)

### Docker Issues:

```bash
# Restart from a clean slate
docker-compose down -v
docker system prune -a
docker-compose up -d --build
```

### Detailed Logs:

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f game-session-service
```