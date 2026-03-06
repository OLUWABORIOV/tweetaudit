/*I learnt that jackson translate Json text, so I dont need to rewritr string id,  */

package com.tweetaudit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Tweet(
        @JsonProperty("id_str") String id,
        @JsonProperty("full_text") String fullText,
        @JsonProperty("created_at") String createdAt
) {}


