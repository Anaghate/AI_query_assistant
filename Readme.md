# AI Query Assistant

A Spring Boot REST API that integrates with the Anthropic Claude API to generate AI-powered responses for user queries. Built with Java 17 and Spring Boot 3.2.

---

## Demo

Type any question into the chat UI and get an instant response powered by Claude.


---

## Tech Stack

- **Java 17**
- **Spring Boot 3.2**
- **Anthropic Claude API** (`claude-3-5-sonnet`)
- **Maven** (build tool)
- **HTML/CSS/JS** (frontend chat UI)

---

## Project Structure

```
ai-query-assistant/
├── src/
│   └── main/
│       ├── java/com/example/aiassistant/
│       │   ├── AiAssistantApplication.java
│       │   ├── controller/
│       │   │   └── ChatController.java
│       │   ├── service/
│       │   │   └── AnthropicService.java
│       │   ├── model/
│       │   │   ├── ChatRequest.java
│       │   │   └── ChatResponse.java
│       │   └── config/
│       │       └── AppConfig.java
│       └── resources/
│           ├── application.yml
│           └── static/
│               └── index.html
├── .env.example
├── .gitignore
├── pom.xml
└── README.md
```

---

## Prerequisites

Make sure you have the following installed:

- [Java 17+](https://adoptium.net)
- [Maven 3.8+](https://maven.apache.org/install.html)
- An [Anthropic API key](https://console.anthropic.com)

Verify your installations:
```bash
java -version
mvn -version
```

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/your-username/ai-query-assistant.git
cd ai-query-assistant
```

### 2. Set up your API key

Copy the example env file:
```bash
cp .env.example .env
```

Open `.env` and add your Anthropic API key:
```
ANTHROPIC_API_KEY=sk-ant-xxxxxxxxxxxxxxxx
```

> ⚠️ Never commit your `.env` file. It is already listed in `.gitignore`.

### 3. Build the project

```bash
mvn clean package -DskipTests
```

### 4. Run the app

```bash
export $(cat .env | xargs) && mvn spring-boot:run
```

### 5. Open the chat UI

```
http://localhost:8080
```

---

## API Reference

### `POST /api/ask`

Send a question and receive an AI-generated answer.

**Request:**
```bash
curl -X POST http://localhost:8080/api/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "What is Spring Boot?"}'
```

**Response:**
```json
{
  "answer": "Spring Boot is a framework that simplifies building production-ready Java applications..."
}
```

---

## Configuration

All configuration lives in `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

anthropic:
  api:
    key: ${ANTHROPIC_API_KEY}   # loaded from environment variable
  model: claude-sonnet-4-5-20250929
```

To change the port, update `server.port`. To switch models, update `anthropic.model`.

---

## Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `ANTHROPIC_API_KEY` | Your Anthropic API key | ✅ Yes |

See `.env.example` for a template.

---

## How It Works

1. User types a question in the browser UI
2. The frontend sends a `POST /api/ask` request to Spring Boot
3. `ChatController` receives the request and calls `AnthropicService`
4. `AnthropicService` builds the JSON payload and calls the Anthropic API
5. Claude's response is parsed and returned as a `ChatResponse`
6. The answer is displayed in the chat UI

---

## Common Issues

| Problem | Fix |
|---------|-----|
| `401 Unauthorized` | Check your `ANTHROPIC_API_KEY` is set correctly |
| `Port 8080 already in use` | Change `server.port` in `application.yml` to `8081` |
| `BUILD FAILURE` | Make sure you are in the root folder (where `pom.xml` is) |
| Response is slow on first call | Normal — JVM warms up after the first request |

---

## Security

- API keys are loaded from environment variables, never hardcoded
- `.env` is excluded from version control via `.gitignore`
- CORS is configured on the controller for local development

---

## Resume Entry

**AI Query Assistant — Spring Boot + LLM Integration**
- Developed a Spring Boot REST API integrating the Anthropic Claude API to generate AI-powered responses for user queries
- Designed backend endpoints to handle query processing, API integration, and response formatting with proper error handling
- Implemented prompt engineering techniques to control LLM output and improve response relevance
- Secured API credentials using environment variables to prevent key exposure in version control

---
