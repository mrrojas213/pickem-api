# Pick Em League API

A serverless REST API built on AWS that powers a sports pick em league. Users predict game winners, earn points for correct picks, and compete on a leaderboard.

## Architecture

- **AWS Lambda** — Java 21 serverless functions
- **API Gateway** — REST API endpoints
- **DynamoDB** — NoSQL database for users, games, and picks
- **GitHub Actions** — CI/CD pipeline for automated builds and deployments

## Live API

Try the live leaderboard endpoint directly in your browser:

`https://4jmed72tpd.execute-api.us-east-1.amazonaws.com/prod/leaderboard`

All other endpoints accept POST requests and can be tested using PowerShell or Postman.

## Endpoints

| Method | Endpoint | Full URL |
|---|---|---|
| POST | /users | `https://4jmed72tpd.execute-api.us-east-1.amazonaws.com/prod/users` |
| POST | /games | `https://4jmed72tpd.execute-api.us-east-1.amazonaws.com/prod/games` |
| POST | /picks | `https://4jmed72tpd.execute-api.us-east-1.amazonaws.com/prod/picks` |
| GET | /leaderboard | `https://4jmed72tpd.execute-api.us-east-1.amazonaws.com/prod/leaderboard` |

## Example Requests (PowerShell)

**Create a user:**
```powershell
Invoke-WebRequest -Uri "https://4jmed72tpd.execute-api.us-east-1.amazonaws.com/prod/users" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"username": "player1"}' `
  -UseBasicParsing
```

**Schedule a game:**
```powershell
Invoke-WebRequest -Uri "https://4jmed72tpd.execute-api.us-east-1.amazonaws.com/prod/games" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"homeTeam": "Chargers", "awayTeam": "49ers", "gameDate": "2026-09-10", "sport": "NFL"}' `
  -UseBasicParsing
```

**Submit a pick:**
```powershell
Invoke-WebRequest -Uri "https://4jmed72tpd.execute-api.us-east-1.amazonaws.com/prod/picks" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"userId": "your-user-id", "gameId": "your-game-id", "pickedTeam": "49ers"}' `
  -UseBasicParsing
```

**Submit game result:**
```powershell
Invoke-WebRequest -Uri "https://4jmed72tpd.execute-api.us-east-1.amazonaws.com/prod/score" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"gameId": "your-game-id", "winner": "49ers"}' `
  -UseBasicParsing
```

**Get leaderboard:**
```powershell
Invoke-WebRequest -Uri "https://4jmed72tpd.execute-api.us-east-1.amazonaws.com/prod/leaderboard" `
  -Method GET `
  -UseBasicParsing
```

## CI/CD Pipeline

Every push to `main` triggers a GitHub Actions workflow that:
1. Builds the project with Maven
2. Runs tests
3. Deploys updated JAR to all Lambda functions automatically

## Tech Stack

Java 21, AWS Lambda, AWS API Gateway, AWS DynamoDB, AWS IAM, GitHub Actions, Maven