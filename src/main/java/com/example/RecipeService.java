package com.example;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.model.function.FunctionCallingOptions;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RecipeService {

    private static final Logger log = LoggerFactory.getLogger(RecipeService.class);

    private final ChatClient chatClient;
    private final Optional<ImageModel> imageModel;
    private final VectorStore vectorStore;

    @Value("classpath:/prompts/recipe-for-ingredients")
    private Resource recipeForIngredientsPromptResource;

    @Value("classpath:/prompts/recipe-for-available-ingredients")
    private Resource recipeForAvailableIngredientsPromptResource;

    @Value("classpath:/prompts/prefer-own-recipe")
    private Resource preferOwnRecipePromptResource;

    @Value("classpath:/prompts/image-for-recipe")
    private Resource imageForRecipePromptResource;

    public RecipeService(ChatClient chatClient, Optional<ImageModel> imageModel, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.imageModel = imageModel;
        this.vectorStore = vectorStore;
    }

    public void addRecipeDocumentForRag(Resource pdfResource) {
        log.info("Add recipe document {} for rag", pdfResource.getFilename());
        var documentReader = new PagePdfDocumentReader(pdfResource);
        var documents = new TokenTextSplitter().apply(documentReader.get());
        vectorStore.accept(documents);
    }

    public Recipe fetchRecipeFor(List<String> ingredients, boolean preferAvailableIngredients, boolean preferOwnRecipes) {
        setChatClientDefaults();

        Recipe recipe;
        if (!preferAvailableIngredients && !preferOwnRecipes) {
            recipe = fetchRecipeFor(ingredients);
        } else if (preferAvailableIngredients && !preferOwnRecipes) {
            recipe = fetchRecipeWithFunctionCallingFor(ingredients);
        } else if (!preferAvailableIngredients && preferOwnRecipes) {
            recipe = fetchRecipeWithRagFor(ingredients);
        } else {
            recipe = fetchRecipeWithRagAndFunctionCallingFor(ingredients);
        }

        if (imageModel.isPresent()) {
            var imagePromptTemplate = new PromptTemplate(imageForRecipePromptResource);
            var imagePromptInstructions = imagePromptTemplate.render(Map.of("recipe", recipe.name(), "ingredients", String.join(",", recipe.ingredients())));
            var imageGeneration = imageModel.get().call(new ImagePrompt(imagePromptInstructions)).getResult();
            return new Recipe(recipe, imageGeneration.getOutput().getUrl());
        }
        return recipe;
    }

    private Recipe fetchRecipeFor(List<String> ingredients) {
        log.info("Fetch recipe without additional information");

        return chatClient.prompt()
                .user(us -> us
                        .text(recipeForIngredientsPromptResource)
                        .param("ingredients", String.join(",", ingredients)))
                .call()
                .entity(Recipe.class);
    }

    private Recipe fetchRecipeWithFunctionCallingFor(List<String> ingredients) {
        log.info("Fetch recipe with additional information from function calling");

        return chatClient.prompt()
                .user(us -> us
                        .text(recipeForAvailableIngredientsPromptResource)
                        .param("ingredients", String.join(",", ingredients)))
                .functions("fetchIngredientsAvailableAtHome")
                .call()
                .entity(Recipe.class);
    }

    private Recipe fetchRecipeWithRagFor(List<String> ingredients) {
        log.info("Fetch recipe with additional information from vector store");
        var promptTemplate = new PromptTemplate(recipeForIngredientsPromptResource,
                Map.of("ingredients", String.join(",", ingredients)));
        var advise = new PromptTemplate(preferOwnRecipePromptResource).getTemplate();
        var advisorSearchRequest = SearchRequest.defaults().withTopK(2).withSimilarityThreshold(0.7);

        return chatClient.prompt()
                .user(promptTemplate.render())
                .advisors(new QuestionAnswerAdvisor(vectorStore, advisorSearchRequest, advise))
                .call()
                .entity(Recipe.class);
    }

    private Recipe fetchRecipeWithRagAndFunctionCallingFor(List<String> ingredients) {
        log.info("Fetch recipe with additional information from vector store and function calling");
        var promptTemplate = new PromptTemplate(recipeForAvailableIngredientsPromptResource,
                Map.of("ingredients", String.join(",", ingredients)));
        var advise = new PromptTemplate(preferOwnRecipePromptResource).getTemplate();
        var advisorSearchRequest = SearchRequest.defaults().withTopK(2).withSimilarityThreshold(0.7);

        return chatClient.prompt()
                .user(promptTemplate.render())
                .functions("fetchIngredientsAvailableAtHome")
                .advisors(new QuestionAnswerAdvisor(vectorStore, advisorSearchRequest, advise))
                .call()
                .entity(Recipe.class);
    }

    private void setChatClientDefaults() {
        // Workaround: Configurations like function names for Function Calling will be saved between requests. Which means that once Function Calling is used, it is always configured.
        try {
            var defaultClientRequest = (ChatClient.ChatClientRequest) FieldUtils.readField(chatClient, "defaultChatClientRequest", true);
            var chatOptions = FieldUtils.readField(defaultClientRequest, "chatOptions", true);
            if (chatOptions instanceof FunctionCallingOptions) {
                FieldUtils.writeField(chatOptions, "functions", new HashSet<String>(), true);
            }
        } catch (Exception e) {
            log.info("Failed to set defaults for chat client request", e);
        }
    }
}