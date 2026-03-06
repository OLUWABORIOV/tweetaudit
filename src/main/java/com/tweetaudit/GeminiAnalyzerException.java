package com.tweetaudit;

public class GeminiAnalyzerException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public GeminiAnalyzerException(String message, int statusCode, String responseBody) {
        super(message + " (HTTP " + statusCode + ")");
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}

