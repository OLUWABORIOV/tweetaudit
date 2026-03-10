package com.tweetaudit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GeminiAnalyzer {

    private static final String GEMINI_ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private final String apiKey;
    private final Map<String, Object> criteria;
    private final HttpClient client;
    private final ObjectMapper mapper;

    //  passed-in parameters
    public GeminiAnalyzer(String apiKey, Map<String, Object> criteria, HttpClient client, ObjectMapper mapper) {
        this.apiKey = apiKey;
        this.criteria = criteria;
        this.client = client;
        this.mapper = mapper;
    }

    // Convenience constructor for production use
    public GeminiAnalyzer(String apiKey, Map<String, Object> criteria) {
        this(apiKey, criteria, HttpClient.newHttpClient(), new ObjectMapper());
    }

    public List<String> getFlaggedIds(List<Tweet> tweets) {
        try {
            String prompt = buildPrompt(tweets);
            String requestBody = buildPayload(prompt);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_ENDPOINT + "?key=" + apiKey))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            //  Safety check for HTTP errors
            if (response.statusCode() != 200) {
                throw new GeminiAnalyzerException("Gemini API Error", response.statusCode(), response.body());
            }


            return extractFlaggedIds(response.body());

        } catch (GeminiAnalyzerException e) {
            throw e;


        } catch (Exception e) {
            // Wrap generic IO/Interrupted exceptions into your custom one
            throw new GeminiAnalyzerException("Execution failed: " + e.getMessage(), 500, null);


        }
    }
                // HELPER METHODS  //

    private String buildPrompt(List<Tweet> tweets) throws Exception {
        return


        """
Review the following tweets using the criteria provided.

Criteria (JSON):
%s

Tweets (JSON):
%s

Return ONLY valid JSON in this exact format:
{"flagged":["id1","id2"]}

Do not include explanations or extra text.
""".formatted(
                mapper.writeValueAsString(criteria),
                mapper.writeValueAsString(tweets)
        );
    }

    private String buildPayload(String prompt) throws Exception {
        Map<String, Object> payload = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );
        return mapper.writeValueAsString(payload);
    }

    private List<String> extractFlaggedIds(String responseBody) throws Exception {
        JsonNode root = mapper.readTree(responseBody);

        // Path navigation
        JsonNode textNode = root.at("/candidates/0/content/parts/0/text");

        if (textNode.isMissingNode()) {
            return List.of();
        }

        // Gemini returns JSON inside a string field, so we parse that string
        JsonNode flaggedNode = mapper.readTree(textNode.asText()).get("flagged");

        List<String> flaggedIds = new ArrayList<>();
        if (flaggedNode != null && flaggedNode.isArray()) {
            for (JsonNode id : flaggedNode) {
                flaggedIds.add(id.asText());
            }
        }
        return flaggedIds;
    }
}




































