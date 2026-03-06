package com.tweetaudit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true) // Prevents crashes if JSON has extra fields
public record Config(
        @JsonProperty("gemini_api_key") String apiKey,
        @JsonProperty("archive_path") String archivePath,
        @JsonProperty("batch_size") int batchSize,
        @JsonProperty("criteria") Map<String, Object> criteria,
        @JsonProperty("user_name") String username
) {}


