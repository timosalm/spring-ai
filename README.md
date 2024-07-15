#Spring AI Recipe Finder

# Setup
## LLM
### Local LLM (Ollama)
#### Option 1
As Ollama doesn't yet provide a text-to-image model, recipe image generation is not available with this setup.

1. Download and install Ollama on your local machine [link](https://ollama.com/)
2. Start llama3 model
    ```
    ollama run llama3
    ```
#### Option 2
By enabling the "ollama-compose" Spring profile, the llama3 model will be automatically started and configured with docker compose.
Depending on your system (e.g. ARM macs) this is not a recommended setup due to performance reasons.
```
SPRING_PROFILES_ACTIVE=ollama-compose
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
```
### Vector DB
On your local machine, a Redis database is automatically started and configured with docker compose.

# Running the application locally
```
./gradlew bootRun
```
Open [http://localhost:8080](http://localhost:8080) in your browser. 
Enter the ingredients you want to find a recipe for in the form and press the "find" button.

## Function Calling 
By checking the "Prefer available ingredients" checkbox, [Function Calling](https://docs.spring.io/spring-ai/reference/1.0/concepts.html#_function_calling) will be enabled.
As the functionalities to add always available ingredients and for the API call to check the available ingredients in the fridge are not yet implemented, they can be configured via the
`app.always-available-ingredients` and `app.available-ingredients-in-fridge` properties in [application.yaml](src/main/resources/application.yaml).

## Retrieval-Augmented Generation(RAG)
By checking the "Prefer own recipes" checkbox, [Retrieval-Augmented Generation](https://docs.spring.io/spring-ai/reference/1.0/concepts.html#concept-rag) will be enabled.

To upload your own PDF documents for recipes to the vector database, there is a REST API endpoint implemented. 
````
curl -XPOST -F "file=@$HOME/my-recipe.pdf" http://localhost:8080/api/v1/recipes/upload
```