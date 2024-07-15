package com.example;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RecipeService {

    private final ChatClient chatClient;
    private final Optional<ImageModel> imageModel;
    private final VectorStore vectorStore;

    @Value("classpath:/prompts/recipe-for-ingredients")
    private Resource recipeForIngredientsPromptResource;

    @Value("classpath:/prompts/recipe-for-available-ingredients")
    private Resource recipeForAvailableIngredientsPromptResource;

    @Value("classpath:/prompts/image-for-recipe")
    private Resource imageForRecipePromptResource;

    public RecipeService(ChatClient chatClient, Optional<ImageModel> imageModel, VectorStore vectorStore) {
        this.chatClient = chatClient;
        this.imageModel = imageModel;
        this.vectorStore = vectorStore;
    }

    public Recipe fetchRecipeFor(List<String> ingredients, boolean preferAvailableIngredients, boolean preferOwnRecipes) {
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
        var promptTemplate = new PromptTemplate(recipeForIngredientsPromptResource);
        var promptMessage = promptTemplate.createMessage(Map.of("ingredients", String.join(",", ingredients)));

        return chatClient.prompt()
                .messages(promptMessage)
                .call()
                .entity(Recipe.class);
    }

    private Recipe fetchRecipeWithFunctionCallingFor(List<String> ingredients) {
        var promptTemplate = new PromptTemplate(recipeForAvailableIngredientsPromptResource);
        var promptMessage = promptTemplate.createMessage(Map.of("ingredients", String.join(",", ingredients)));

        return chatClient.prompt()
                .messages(promptMessage)
                .functions("fetchAvailableIngredients")
                .call()
                .entity(Recipe.class);
    }

    private Recipe fetchRecipeWithRagFor(List<String> ingredients) {
        var promptTemplate = new PromptTemplate(recipeForIngredientsPromptResource);
        var promptMessage = promptTemplate.createMessage(Map.of("ingredients", String.join(",", ingredients)));

        return chatClient.prompt()
                .messages(promptMessage)
                .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()))
                .call()
                .entity(Recipe.class);
    }

    private Recipe fetchRecipeWithRagAndFunctionCallingFor(List<String> ingredients) {
        var promptTemplate = new PromptTemplate(recipeForAvailableIngredientsPromptResource);
        var promptMessage = promptTemplate.createMessage(Map.of("ingredients", String.join(",", ingredients)));

        return chatClient.prompt()
                .messages(promptMessage)
                .advisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()))
                .functions("fetchAvailableIngredients")
                .call()
                .entity(Recipe.class);
    }

    public void addRecipeDocumentsForRag(Resource pdfResource) {
        var documentReader = new PagePdfDocumentReader(pdfResource);
        var documents = new TokenTextSplitter().apply(documentReader.get());
        vectorStore.accept(documents);
    }
}
