package com.pickem;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubmitScoreHandler implements RequestHandler
    <APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DynamoDbClient dynamoDb = DynamoDbClient.create();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String GAMES_TABLE = "pickem-games";
    private final String PICKS_TABLE = "pickem-picks";
    private final String USERS_TABLE = "pickem-users";

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
        APIGatewayProxyRequestEvent request, Context context) {

        try {
            String requestBody = request.getBody();

            if (requestBody == null || requestBody.isEmpty()) {
                return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody("{\"error\": \"Request body is empty\"}");
            }

            Map<String, String> body = mapper.readValue(requestBody,
                new TypeReference<Map<String, String>>() {});

            String gameId = body.get("gameId");
            String winner = body.get("winner");

            Map<String, AttributeValue> gameKey = new HashMap<>();
            gameKey.put("gameId", AttributeValue.builder().s(gameId).build());

            dynamoDb.updateItem(UpdateItemRequest.builder()
                .tableName(GAMES_TABLE)
                .key(gameKey)
                .updateExpression("SET #s = :s, winner = :w")
                .expressionAttributeNames(Map.of("#s", "status"))
                .expressionAttributeValues(Map.of(
                    ":s", AttributeValue.builder().s("completed").build(),
                    ":w", AttributeValue.builder().s(winner).build()
                ))
                .build());

            ScanResponse picksResponse = dynamoDb.scan(ScanRequest.builder()
                .tableName(PICKS_TABLE)
                .filterExpression("gameId = :gid")
                .expressionAttributeValues(Map.of(
                    ":gid", AttributeValue.builder().s(gameId).build()
                ))
                .build());

            List<Map<String, AttributeValue>> picks = picksResponse.items();
            int correctPicks = 0;

            for (Map<String, AttributeValue> pick : picks) {
                String userId = pick.get("userId").s();
                String pickedTeam = pick.get("pickedTeam").s();
                String pickId = pick.get("pickId").s();
                boolean correct = pickedTeam.equals(winner);

                Map<String, AttributeValue> pickKey = new HashMap<>();
                pickKey.put("pickId", AttributeValue.builder().s(pickId).build());

                dynamoDb.updateItem(UpdateItemRequest.builder()
                    .tableName(PICKS_TABLE)
                    .key(pickKey)
                    .updateExpression("SET #r = :r")
                    .expressionAttributeNames(Map.of("#r", "result"))
                    .expressionAttributeValues(Map.of(
                        ":r", AttributeValue.builder().s(correct ? "won" : "lost").build()
                    ))
                    .build());

                Map<String, AttributeValue> userKey = new HashMap<>();
                userKey.put("userId", AttributeValue.builder().s(userId).build());

                if (correct) {
                    correctPicks++;
                    dynamoDb.updateItem(UpdateItemRequest.builder()
                        .tableName(USERS_TABLE)
                        .key(userKey)
                        .updateExpression("SET points = points + :p, wins = wins + :w")
                        .expressionAttributeValues(Map.of(
                            ":p", AttributeValue.builder().n("1").build(),
                            ":w", AttributeValue.builder().n("1").build()
                        ))
                        .build());
                } else {
                    dynamoDb.updateItem(UpdateItemRequest.builder()
                        .tableName(USERS_TABLE)
                        .key(userKey)
                        .updateExpression("SET losses = losses + :l")
                        .expressionAttributeValues(Map.of(
                            ":l", AttributeValue.builder().n("1").build()
                        ))
                        .build());
                }
            }

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("gameId", gameId);
            responseBody.put("winner", winner);
            responseBody.put("totalPicks", String.valueOf(picks.size()));
            responseBody.put("correctPicks", String.valueOf(correctPicks));
            responseBody.put("message", "Score submitted and standings updated");

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(mapper.writeValueAsString(responseBody));

        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withBody("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
