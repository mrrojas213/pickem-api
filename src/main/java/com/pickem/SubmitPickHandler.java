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

public class SubmitPickHandler implements RequestHandler
    <APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DynamoDbClient dynamoDb = DynamoDbClient.create();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String TABLE_NAME = "pickem-picks";

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

            String userId = body.get("userId");
            String gameId = body.get("gameId");
            String pickedTeam = body.get("pickedTeam");
            String pickId = UUID.randomUUID().toString();

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("pickId", AttributeValue.builder().s(pickId).build());
            item.put("userId", AttributeValue.builder().s(userId).build());
            item.put("gameId", AttributeValue.builder().s(gameId).build());
            item.put("pickedTeam", AttributeValue.builder().s(pickedTeam).build());
            item.put("result", AttributeValue.builder().s("pending").build());

            dynamoDb.putItem(PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build());

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("pickId", pickId);
            responseBody.put("userId", userId);
            responseBody.put("gameId", gameId);
            responseBody.put("pickedTeam", pickedTeam);
            responseBody.put("result", "pending");
            responseBody.put("message", "Pick submitted successfully");

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
