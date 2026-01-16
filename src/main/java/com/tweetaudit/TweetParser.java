package com.tweetaudit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TweetParser {
    private static final ObjectMapper mapper = new ObjectMapper();

    public List<Tweet> parse(String filePath) throws IOException {
        String content = Files.readString(Path.of(filePath));

        // Remove the JS variable assignment to get raw JSON
        // Matches "window.YTD.tweet.part0 = " (flexible spacing)
        Pattern pattern = Pattern.compile("window\\.YTD\\.tweet\\.part0\\s*=\s*");
        Matcher matcher = pattern.matcher(content);
        String jsonContent = matcher.replaceFirst("");

        JsonNode rootNode = mapper.readTree(jsonContent);
        List<Tweet> tweets = new ArrayList<>();

        if (rootNode.isArray()) {
            for (JsonNode node : rootNode) {
                // The archive nests the actual data inside a "tweet" property
                JsonNode tweetNode = node.get("tweet");
                if (tweetNode != null) {
                    tweets.add(mapper.treeToValue(tweetNode, Tweet.class));
                }
            }
        }
        return tweets;
    }
}