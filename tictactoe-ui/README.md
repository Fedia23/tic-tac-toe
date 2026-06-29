# 🎯 Tic Tac Toe UI - Auto Simulation

React-based user interface for visualizing automated Tic Tac Toe games played by microservices.

## Features

-  **Auto-Simulation**: Watch microservices play against each other
-  **Real-time Updates**: Board updates as moves are made
-  **Move History**: Track every move with timestamps
-  **Game Statistics**: View game progress and results
-  **Beautiful UI**: Modern gradient design with animations
-  **Error Handling**: Graceful error messages and recovery
-  **Responsive**: Works on desktop and mobile devices

## Prerequisites

- Node.js 14+ and npm
- Game Engine Service running on `http://localhost:8081`

## Installation

```bash
cd /Users/fcholak/test/tictactoe-ui
npm install
```

## Running the Application

### 1. Start the Backend Service

First, make sure the Game Engine Service is running:

```bash
cd /Users/fcholak/test/game-engine-service
./mvnw spring-boot:run
```

The backend will be available at `http://localhost:8081`

### 2. Start the React UI

In a new terminal:

```bash
cd /Users/fcholak/test/tictactoe-ui
npm start
```

The UI will open automatically at `http://localhost:3000`

## How to Use

1. **Start Simulation**: Click the "▶️ Start Simulation" button
2. **Watch the Game**: The microservices will automatically play against each other
3. **View Progress**:
   - See moves in real-time on the board
   - Check move history on the right panel
   - Monitor game statistics
4. **Reset**: Click "🔄 Reset" to start a new game

## Architecture

```
┌─────────────────┐         HTTP/REST        ┌──────────────────┐
│   React UI      │ ────────────────────────> │  Game Engine     │
│  (Port 3000)    │ <──────────────────────── │  Service (8081)  │
└─────────────────┘                           └──────────────────┘
```

## API Endpoints Used

- `POST /games/{gameId}` - Create a new game
- `GET /games/{gameId}` - Get current game state
- `POST /games/{gameId}/move` - Make a move

## Technology Stack

- **React 19** - UI Framework
- **Fetch API** - HTTP requests
- **CSS3** - Styling with gradients and animations
- **React Hooks** - State management (useState)

## Customization

### Change Backend URL

Edit `src/App.js`:

```javascript
const API_URL = 'http://localhost:8081'; // Change this
```

### Adjust Animation Speed

Edit the delay in `src/App.js`:

```javascript
await new Promise(resolve => setTimeout(resolve, 1000)); // Change 1000ms
```

## Troubleshooting

### CORS Errors

The backend has CORS enabled for `localhost:3000`. If you get CORS errors:

1. Check that the Game Engine Service is running
2. Verify CORS settings in backend's `application.properties`:
   ```properties
   cors.allowed-origins=http://localhost:3000
   ```

### Connection Errors

If you see "Failed to create game":

1. Verify backend is running: `curl http://localhost:8081/actuator/health`
2. Check browser console for detailed errors
3. Ensure no firewall is blocking the connection

## Production Build

```bash
npm run build
```

This creates an optimized build in the `build/` folder.

## Contributing

Feel free to enhance the UI with:
- WebSocket support for real-time updates
- Sound effects for moves
- Dark mode toggle
- Game replay functionality
- Multiple simultaneous games