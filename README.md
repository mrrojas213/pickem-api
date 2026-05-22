# Pick Em League API

A serverless REST API built on AWS that powers a sports pick em league. Users predict game winners, earn points for correct picks, and compete on a leaderboard.

## Architecture

- **AWS Lambda** — Java 21 serverless functions
- **API Gateway** — REST API endpoints
- **DynamoDB** — NoSQL database for users, games, and picks
- **GitHub Actions** — CI/CD pipeline for automated builds and deployments

## Live API

Base URL: `https://4jmed72tpd.execute-api.us-east-1.amazonaws.com/prod`

## Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | /users | Register a new player |
| POST | /games | Schedule a game |
| POST | /picks | Submit a pick |
| PUT | /games/{id}/score | Submit game result |
| GET | /leaderboard | Get ranked standings |

## Example Requests

**Create a user:**
```json
POST /users
{
  "username": "player1"
}
```

**Schedule a game:**
```json
POST /games
{
  "homeTeam": "Chargers",
  "awayTeam": "49ers",
  "gameDate": "2026-09-10",
  "sport": "NFL"
}
```

**Submit a pick:**
```json
POST /picks
{
  "userId": "your-user-id",
  "gameId": "your-game-id",
  "pickedTeam": "Chiefs"
}
```

**Get leaderboard:**
```json
GET /leaderboard
```

## CI/CD Pipeline

Every push to `main` triggers a GitHub Actions workflow that:
1. Builds the project with Maven
2. Deploys updated JAR to all Lambda functions automatically

## Tech Stack

Java 21, AWS Lambda, AWS API Gateway, AWS DynamoDB, AWS IAM, GitHub Actions, Maven