#Spring AI Recipe Finder

# Setup
## LLM
### Local LLM (Ollama)
As Ollama doesn't yet provide a text-to-image model, recipe image generation is not available with this setup.

1. Download and install Ollama on your local machine [link](https://ollama.com/)
2. Start llama3 model
    ```
    ollama run llama3
    ```
### OpenAI (TODO)
### Azure OpenAI
Set the API key and endpoint via environment variables or in [application.yaml](src/main/resources/application.yaml).
```
export SPRING_AI_AZURE_OPENAI_API_KEY=<INSERT KEY HERE>
export SPRING_AI_AZURE_OPENAI_ENDPOINT=<INSERT ENDPOINT URL HERE>
```
Run your application with the "azure" Spring Profile.
```
SPRING_PROFILES_ACTIVE=azure
./gradlew bootRun
```
### Vector DB
On your local machine, a Redis database is automatically started and configured with docker compose.

