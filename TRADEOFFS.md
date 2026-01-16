# Implementation Trade-offs (Java)

## Architecture
I utilized a **standard synchronous Java application** structure.
* **Why Java?** Java provides strong type safety (Records) and robust error handling, which reduces runtime errors when processing large archives.
* **Dependency Management:** Maven was chosen for its ubiquity. I restricted dependencies to `Jackson` (standard for JSON) and `JUnit` to keep the build artifacts lightweight.

## Concurrency Strategy
The application processes batches **sequentially** on the main thread with a standard `Thread.sleep()` throttle.
* **Decision:** While Java 21 Virtual Threads (`ExecutorService.newVirtualThreadPerTaskExecutor`) offer high concurrency, LLM APIs have strict Rate Limits (RPM).
* **Trade-off:** A simple synchronous loop is easier to debug and naturally respects rate limits without complex semaphore logic. The performance cost is acceptable for an offline audit tool.

## Error Handling
I implemented a **Defensive/Skip** strategy.
* **JSON Parsing:** Malformed tweets are skipped rather than halting the parser.
* **API Calls:** If a batch request fails (network or 500 error), the error is logged to `System.err`, and the batch is skipped. This ensures the script finishes even if intermittent API errors occur.

## Gemini Integration
I used **Java's native `HttpClient`** instead of the Google Cloud Java Client Library.
* **Reason:** The Google Cloud library is heavy (large JAR size, transitive dependencies). For a simple stateless REST call to the Gemini Generative Language API, `HttpClient` is cleaner, faster to build, and easier to mock in tests.