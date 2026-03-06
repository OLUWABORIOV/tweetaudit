package com.tweetaudit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GeminiAnalyzerTest {

    @Test
    void testGetFlaggedIdsParsesResponse() throws Exception {

        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);

        // Key must be "flagged" not "flagged_ids"
        String fakeGeminiResponse = """
        {
          "candidates": [{
            "content": {
              "parts": [{
                "text": "{ \\"flagged\\": [\\"999\\"] }"
              }]
            }
          }]
        }
        """;

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(fakeGeminiResponse);
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        GeminiAnalyzer analyzer = new GeminiAnalyzer(
                "dummy_key",
                Map.of("forbidden", "bad"),  // Map<String, Object> compatible
                mockClient,
                new ObjectMapper()            // must pass ObjectMapper
        );

        List<Tweet> batch = List.of(new Tweet("999", "bad tweet", "date"));

        List<String> result = analyzer.getFlaggedIds(batch);  // correct method name

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("999", result.get(0));
    }
}//package com.tweet;
//
//import org.junit.jupiter.api.Test;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//class GeminiAnalyzerTest {
//
//    @Test
//    void testAnalyzeBatchParsesResponse() throws Exception {
//
//        HttpClient mockClient = mock(HttpClient.class);
//        HttpResponse<String> mockResponse = mock(HttpResponse.class);
//
//        String fakeGeminiResponse = """
//        {
//          "candidates": [{
//            "content": {
//              "parts": [{
//                "text": "{ \\"flagged_ids\\": [\\"999\\"] }"
//              }]
//            }
//          }]
//        }
//        """;
//
//        when(mockResponse.statusCode()).thenReturn(200);
//        when(mockResponse.body()).thenReturn(fakeGeminiResponse);
//
//        when(mockClient.send(
//                any(HttpRequest.class),
//                any(HttpResponse.BodyHandler.class)
//        )).thenReturn(mockResponse);
//
//        GeminiAnalyzer analyzer =
//                new GeminiAnalyzer("dummy_key", Map.of("forbidden", "bad"), mockClient);
//
//        List<Tweet> batch =
//                List.of(new Tweet("999", "bad tweet", "date"));
//
//        List<String> result = analyzer.analyzeBatch(batch);
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals("999", result.get(0));
//    }
//}