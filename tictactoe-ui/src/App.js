import React, { useState } from 'react';
import './App.css';

const API_URL = '/api/engine';

function App() {
  const [gameId, setGameId] = useState(null);
  const [board, setBoard] = useState(Array(9).fill(null));
  const [status, setStatus] = useState('READY');
  const [winner, setWinner] = useState(null);
  const [currentTurn, setCurrentTurn] = useState('X');
  const [moveHistory, setMoveHistory] = useState([]);
  const [error, setError] = useState(null);
  const [isSimulating, setIsSimulating] = useState(false);

  const startSimulation = async () => {
    try {
      setError(null);
      const newGameId = `game-${Date.now()}`;
      setGameId(newGameId);

      // Create new game
      const response = await fetch(`${API_URL}/games/${newGameId}`, {
        method: 'POST',
      });

      if (!response.ok) {
        throw new Error('Failed to create game');
      }

      const gameData = await response.json();
      setBoard(flattenBoard(gameData.board));
      setStatus(gameData.status);
      setCurrentTurn(gameData.currentTurn);
      setMoveHistory([]);
      setIsSimulating(true);

      // Start automated gameplay
      simulateGame(newGameId);
    } catch (err) {
      setError(err.message);
    }
  };

  const simulateGame = async (gId) => {
    let gameStatus = 'IN_PROGRESS';
    let turn = 'X';

    while (gameStatus === 'IN_PROGRESS') {
      try {
        // Wait for a moment to make moves visible
        await new Promise(resolve => setTimeout(resolve, 1000));

        // Find available moves
        const gameState = await fetch(`${API_URL}/games/${gId}`).then(r => r.json());
        const availableMoves = [];

        for (let row = 0; row < 3; row++) {
          for (let col = 0; col < 3; col++) {
            if (gameState.board[row][col] === null) {
              availableMoves.push({ row, col });
            }
          }
        }

        if (availableMoves.length === 0) break;

        // Pick random move
        const move = availableMoves[Math.floor(Math.random() * availableMoves.length)];

        // Make move
        const response = await fetch(`${API_URL}/games/${gId}/move`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            player: turn,
            row: move.row,
            col: move.col,
          }),
        });

        if (!response.ok) {
          throw new Error('Move failed');
        }

        const moveData = await response.json();
        setBoard(flattenBoard(moveData.board));
        setStatus(moveData.status);
        setWinner(moveData.winner);
        setCurrentTurn(moveData.nextTurn);

        setMoveHistory(prev => [...prev, {
          player: turn,
          position: `(${move.row}, ${move.col})`,
          message: moveData.message
        }]);

        gameStatus = moveData.status;
        turn = moveData.nextTurn;

      } catch (err) {
        setError(err.message);
        break;
      }
    }

    setIsSimulating(false);
  };

  const flattenBoard = (board2D) => {
    const flat = [];
    for (let i = 0; i < 3; i++) {
      for (let j = 0; j < 3; j++) {
        flat.push(board2D[i][j]);
      }
    }
    return flat;
  };

  const resetGame = () => {
    setGameId(null);
    setBoard(Array(9).fill(null));
    setStatus('READY');
    setWinner(null);
    setCurrentTurn('X');
    setMoveHistory([]);
    setError(null);
    setIsSimulating(false);
  };

  const getStatusDisplay = () => {
    switch (status) {
      case 'X_WINS':
        return 'Player X Wins!';
      case 'O_WINS':
        return 'Player O Wins!';
      case 'DRAW':
        return 'Game is a Draw!';
      case 'IN_PROGRESS':
        return `Game in Progress - ${currentTurn}'s turn`;
      default:
        return 'Ready to Start';
    }
  };

  return (
    <div className="App">
      <header className="App-header">
        <h1>Tic Tac Toe - Auto Simulation</h1>
      </header>

      <div className="game-container">
        <div className="left-panel">
          <div className="status-card">
            <h2>{getStatusDisplay()}</h2>
            {gameId && <p className="game-id">Game ID: {gameId}</p>}
          </div>

          <div className="board">
            {board.map((cell, index) => (
              <div key={index} className={`cell ${cell ? 'filled' : ''}`}>
                {cell && <span className={`symbol symbol-${cell}`}>{cell}</span>}
              </div>
            ))}
          </div>

          <div className="controls">
            <button
              className="btn btn-primary"
              onClick={startSimulation}
              disabled={isSimulating}
            >
              {isSimulating ? 'Simulating...' : 'Start Simulation'}
            </button>

            <button
              className="btn btn-secondary"
              onClick={resetGame}
              disabled={isSimulating}
            >
              Reset
            </button>
          </div>

          {error && (
            <div className="error-message">
              ⚠️ Error: {error}
            </div>
          )}
        </div>

        <div className="right-panel">
          <h3>Move History</h3>
          <div className="move-history">
            {moveHistory.length === 0 ? (
              <p className="no-moves">No moves yet. Start simulation to begin!</p>
            ) : (
              moveHistory.map((move, index) => (
                <div key={index} className="move-item">
                  <span className="move-number">#{index + 1}</span>
                  <span className={`move-player player-${move.player}`}>
                    {move.player}
                  </span>
                  <span className="move-position">{move.position}</span>
                </div>
              ))
            )}
          </div>

          <div className="stats">
            <h4>Game Stats</h4>
            <div className="stat-item">
              <span>Total Moves:</span>
              <span>{moveHistory.length}</span>
            </div>
            <div className="stat-item">
              <span>Status:</span>
              <span>{status}</span>
            </div>
            {winner && (
              <div className="stat-item winner">
                <span>Winner:</span>
                <span>Player {winner}</span>
              </div>
            )}
          </div>
        </div>
      </div>

      <footer className="footer">
        <p>Powered by Spring Boot Microservices</p>
        <p>API: <code>{API_URL}</code></p>
      </footer>
    </div>
  );
}

export default App;