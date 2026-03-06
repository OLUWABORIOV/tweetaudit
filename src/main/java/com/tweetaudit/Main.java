package com.tweetaudit;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileWriter;
import java.io.IOException;
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

            if (allTweets.isEmpty()) {
                System.out.println("No tweets to analyze.");
                return;
            }

            // 3. Analyze in Batches
            GeminiAnalyzer analyzer = new GeminiAnalyzer(config.apiKey(), config.criteria());
            List<String> flaggedUrls = new ArrayList<>();
            int batchSize = config.batchSize() > 0 ? config.batchSize() : 20;

            System.out.println("Starting analysis...");

            for (int i = 0; i < allTweets.size(); i += batchSize) {
                int end = Math.min(allTweets.size(), i + batchSize);
                List<Tweet> batch = allTweets.subList(i, end);

                System.out.printf("Processing batch %d/%d%n",
                        (i/batchSize) + 1,
                        (int)Math.ceil((double)allTweets.size()/batchSize));

                try {
                    List<String> flaggedIds = analyzer.getFlaggedIds(batch);

                    for (String id : flaggedIds) {
                        // Get actual username from config or tweet data
                        String username = config.username(); //Add this to
                        flaggedUrls.add("https://x.com/" + username + "/status/" + id);
                    }
                } catch (Exception e) {
                    System.err.println("Error analyzing batch " + (i/batchSize + 1) + ": " + e.getMessage());
                    // Continue with next batch instead of crashing
                }

                // Rate limit handling
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Analysis interrupted");
                    break;
                }
            }

            // 4. Write Output
            writeResults(flaggedUrls);

            System.out.println("Done. Flagged " + flaggedUrls.size() + " tweets. Check flagged_tweets.csv");

        } catch (IOException e) {
            System.err.println("IO Error: " + e.getMessage());
            e.printStackTrace();


        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();




        }
    }

    private static void writeResults(List<String> flaggedUrls) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter("flagged_tweets.csv"))) {
            writer.println("tweet_url,deleted");
            for (String url : flaggedUrls) {
                writer.println(url + ",false");
            }
        }
    }
}




































































/* package com.tweetaudit;

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
} */