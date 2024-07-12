package com.example;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RecipeService {

    private final ChatClient chatClient;
    private final ImageModel imageModel;

    @Value("classpath:/prompts/recipe-for-ingredients")
    private Resource recipeForIngredientsPromptResource;

    @Value("classpath:/prompts/recipe-for-available-ingredients")
    private Resource recipeForAvailableIngredientsPromptResource;

    @Value("classpath:/prompts/image-for-recipe")
    private Resource imageForRecipePromptResource;

    public RecipeService(ChatClient chatClient, ImageModel imageModel) {
        this.chatClient = chatClient;
        this.imageModel = imageModel;
    }

    public Recipe fetchRecipeFor(List<String> ingredients, boolean preferAvailableIngredients) {
        Recipe recipe;
        if (!preferAvailableIngredients) {
            recipe = fetchRecipeFor(ingredients);
        } else {
            recipe = fetchRecipeWithFunctionCallingFor(ingredients);
        }

        var imagePromptTemplate = new PromptTemplate(imageForRecipePromptResource);
        var imagePromptInstructions = imagePromptTemplate.render(Map.of("recipe", recipe.name(), "ingredients", String.join(",", recipe.ingredients())));
        var imageGeneration = imageModel.call(new ImagePrompt(imagePromptInstructions)).getResult();
        return new Recipe(recipe, imageGeneration.getOutput().getUrl());
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
}
