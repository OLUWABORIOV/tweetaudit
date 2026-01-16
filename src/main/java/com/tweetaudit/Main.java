package com.tweetaudit;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
            // 1. Load Config
            Path configPath = Path.of("config.json");
            if (!Files.exists(configPath)) {
                System.err.println("config.json not found.");
                return;
            }
            ObjectMapper mapper = new ObjectMapper();
            Config config = mapper.readValue(configPath.toFile(), Config.class);

            // 2. Parse Tweets
            System.out.println("Parsing archive...");
            TweetParser parser = new TweetParser();
            List<Tweet> allTweets = parser.parse(config.archivePath());
            System.out.println("Loaded " + allTweets.size() + " tweets.");

            // 3. Analyze in Batches
            GeminiAnalyzer analyzer = new GeminiAnalyzer(config.apiKey(), config.criteria());
            List<String> flaggedUrls = new ArrayList<>();
            int batchSize = config.batchSize() > 0 ? config.batchSize() : 20;

            System.out.println("Starting analysis...");

            for (int i = 0; i < allTweets.size(); i += batchSize) {
                int end = Math.min(allTweets.size(), i + batchSize);
                List<Tweet> batch = allTweets.subList(i, end);

                System.out.printf("Processing batch %d/%d%n", (i/batchSize) + 1, (int)Math.ceil((double)allTweets.size()/batchSize));

                List<String> flaggedIds = analyzer.analyzeBatch(batch);

                for (String id : flaggedIds) {
                    flaggedUrls.add("https://x.com/username/status/" + id);
                }

                // Simple rate limit handling (1 sec pause)
                Thread.sleep(1000);
            }

            // 4. Write Output
            try (PrintWriter writer = new PrintWriter(new FileWriter("flagged_tweets.csv"))) {
                writer.println("tweet_url,deleted");
                for (String url : flaggedUrls) {
                    writer.println(url + ",false");
                }
            }

            System.out.println("Done. Flagged " + flaggedUrls.size() + " tweets. Check flagged_tweets.csv");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}