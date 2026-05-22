package com.pickem;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CreateUserHandler implements RequestHandler
    <APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DynamoDbClient dynamoDb = DynamoDbClient.create();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String TABLE_NAME = "pickem-users";

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
        APIGatewayProxyRequestEvent request, Context context) {

        try {
            String requestBody = request.getBody();

            // Debug log
            System.out.println("Request body: " + requestBody);

            if (requestBody == null || requestBody.isEmpty()) {
                return new APIGatewayProxyResponseEvent()
                    .withStatusCode(400)
                    .withBody("{\"error\": \"Request body is empty\"}");
            }

            Map<String, String> body = mapper.readValue(requestBody,
                new TypeReference<Map<String, String>>() {});

            String username = body.get("username");
            String userId = UUID.randomUUID().toString();

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("userId", AttributeValue.builder().s(userId).build());
            item.put("username", AttributeValue.builder().s(username).build());
            item.put("points", AttributeValue.builder().n("0").build());
            item.put("wins", AttributeValue.builder().n("0").build());
            item.put("losses", AttributeValue.builder().n("0").build());

            dynamoDb.putItem(PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build());

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("userId", userId);
            responseBody.put("username", username);
            responseBody.put("message", "User created successfully");

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
