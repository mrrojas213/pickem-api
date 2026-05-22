package com.pickem;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;

import java.util.*;

public class GetLeaderboardHandler implements RequestHandler
    <APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    private final DynamoDbClient dynamoDb = DynamoDbClient.create();
    private final ObjectMapper mapper = new ObjectMapper();
    private final String USERS_TABLE = "pickem-users";

    @Override
    public APIGatewayProxyResponseEvent handleRequest(
        APIGatewayProxyRequestEvent request, Context context) {

        try {
            ScanResponse response = dynamoDb.scan(ScanRequest.builder()
                .tableName(USERS_TABLE)
                .build());

            List<Map<String, String>> leaderboard = new ArrayList<>();

            for (Map<String, AttributeValue> item : response.items()) {
                Map<String, String> entry = new HashMap<>();
                entry.put("userId", item.get("userId").s());
                entry.put("username", item.get("username").s());
                entry.put("points", item.get("points").n());
                entry.put("wins", item.get("wins").n());
                entry.put("losses", item.get("losses").n());
                leaderboard.add(entry);
            }

            leaderboard.sort((a, b) ->
                Integer.parseInt(b.get("points")) - Integer.parseInt(a.get("points"))
            );

            for (int i = 0; i < leaderboard.size(); i++) {
                leaderboard.get(i).put("rank", String.valueOf(i + 1));
            }

            return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withBody(mapper.writeValueAsString(leaderboard));

        } catch (Exception e) {
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withBody("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
