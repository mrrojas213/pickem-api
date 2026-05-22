package com.pickem;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateGameHandler implements RequestHandler
    <APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DynamoDbClient dynamoDb = DynamoDbClient.create();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String TABLE_NAME = "pickem-games";

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

            String homeTeam = body.get("homeTeam");
            String awayTeam = body.get("awayTeam");
            String gameDate = body.get("gameDate");
            String sport = body.get("sport");
            String gameId = UUID.randomUUID().toString();

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("gameId", AttributeValue.builder().s(gameId).build());
            item.put("homeTeam", AttributeValue.builder().s(homeTeam).build());
            item.put("awayTeam", AttributeValue.builder().s(awayTeam).build());
            item.put("gameDate", AttributeValue.builder().s(gameDate).build());
            item.put("sport", AttributeValue.builder().s(sport).build());
            item.put("status", AttributeValue.builder().s("scheduled").build());
            item.put("winner", AttributeValue.builder().s("TBD").build());

            dynamoDb.putItem(PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build());

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("gameId", gameId);
            responseBody.put("homeTeam", homeTeam);
            responseBody.put("awayTeam", awayTeam);
            responseBody.put("gameDate", gameDate);
            responseBody.put("sport", sport);
            responseBody.put("status", "scheduled");
            responseBody.put("message", "Game created successfully");

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(201)
                .withBody(mapper.writeValueAsString(responseBody));

        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withBody("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
