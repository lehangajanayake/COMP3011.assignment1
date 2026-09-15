# Speech App

This Spring Boot application records microphone audio in the browser, sends it
to the backend as multipart form data, and delegates transcription to OpenAI's
`/v1/audio/transcriptions` endpoint.

## Run

Use JDK 26 (or a compatible JDK supported by the Maven compiler configuration),
export the credential, and start the application:

```text
export OPENAI_API_KEY=your-key
./mvnw spring-boot:run
```

The application is available at `http://localhost:8080/`. The API key is read
from the environment through `openai.api-key`; it is never sent to the browser,
written to logs, or included in an error response.

## Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/` | Serves the recording page |
| `POST` | `/api/transcribe` | Accepts multipart field `audio` and returns `{ "text": "..." }` |
| `GET` | `/api/v1/admin/uptime` | Returns UTC server start time, current UTC time, and uptime in seconds |
| `GET` | `/api/v1/admin/stats` | Returns received, successful, and failed transcription counts |
| `POST` | `/api/v1/admin/shutdown` | Requests a graceful Spring application shutdown; protect this endpoint before deployment |
| `GET` | `/api/v1/global/stats` | Returns cumulative input and output token usage since server start |

The shutdown endpoint returns `202 Accepted` when shutdown begins and `409
Conflict` when another shutdown request is already in progress. Graceful
shutdown waits up to 30 seconds per shutdown phase for in-flight work to finish.

## Concurrency and tests

`spring.threads.virtual.enabled` lets the embedded Tomcat use virtual request
threads, so blocking calls to the cloud provider do not consume a scarce pool of
platform threads. `StatsService` uses `AtomicLong` counters and controllers hold
no request-specific mutable state.

The tests include a stubbed provider contract test and a 256-request blocking
controller regression test. Run them with:

```text
./mvnw test
```