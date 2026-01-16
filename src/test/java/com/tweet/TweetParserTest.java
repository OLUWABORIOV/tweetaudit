package com.tweetaudit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TweetParserTest {

    @Test
    void testParseExtractsTweetsCorrectly(@TempDir Path tempDir) throws IOException {
        Path jsFile = tempDir.resolve("tweets.js");
        String fakeContent = "window.YTD.tweet.part0 = [" +
                "{\"tweet\": {\"id_str\": \"101\", \"full_text\": \"Hello\", \"created_at\": \"Mon Sep\"}}," +
                "{\"tweet\": {\"id_str\": \"102\", \"full_text\": \"World\", \"created_at\": \"Tue Sep\"}}" +
                "]";

        Files.writeString(jsFile, fakeContent);

        TweetParser parser = new TweetParser();
        List<Tweet> tweets = parser.parse(jsFile.toString());

        assertEquals(2, tweets.size());
        assertEquals("101", tweets.get(0).id());
        assertEquals("World", tweets.get(1).fullText());
    }
}
