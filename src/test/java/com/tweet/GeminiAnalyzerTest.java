package com.tweetaudit;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static jdk.jfr.internal.jfc.model.Constraint.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GeminiAnalyzerTest {

    @Test
    void testAnalyzeBatchParsesResponse() throws Exception {
        // Mock HTTP Client
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        // This is the raw JSON structure Gemini returns
        String fakeGeminiResponse = "{" +
                "\"candidates\": [{" +
                "\"content\": {" +
                "\"parts\": [{" +
                "\"text\": \"{ \\\"flagged_ids\\\": [\\\"999\\\"] }\"" +
                "}]" +
                "}" +
                "}]" +
                "}";

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(fakeGeminiResponse);
        when(mockClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockResponse);

        GeminiAnalyzer analyzer = new GeminiAnalyzer("dummy_key", Map.of("forbidden", "bad"), mockClient);

        List<Tweet> batch = List.of(new Tweet("999", "bad tweet", "date"));
        List<String> result = analyzer.analyzeBatch(batch);

        assertEquals(1, result.size());
        assertEquals("999", result.get(0));
    }
}