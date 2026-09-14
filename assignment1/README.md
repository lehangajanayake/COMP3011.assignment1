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
| `GET` | `/api/status/uptime` | Returns application start time and elapsed seconds |
| `GET` | `/api/status/stats` | Returns received, successful, and failed transcription counts |
| `POST` | `/api/status/shutdown` | Requests a Spring application shutdown; protect this endpoint before deployment |

The exact status paths are kept together under `/api/status` because no YAML
endpoint specification was included in the repository. They can be changed in
`StatusController` when the specification is supplied.

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