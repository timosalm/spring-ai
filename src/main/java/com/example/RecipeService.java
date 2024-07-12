package com.example;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.image.ImageMessage;
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
    private final FetchAvailableIngredientsService availableIngredientsService;

    @Value("classpath:/prompts/recipe-for-ingredients")
    private Resource recipeForIngredientsPromptResource;

    @Value("classpath:/prompts/recipe-for-available-ingredients")
    private Resource recipeForAvailableIngredientsPromptResource;

    @Value("classpath:/prompts/image-for-recipe")
    private Resource imageForRecipePromptResource;

    public RecipeService(ChatClient chatClient, ImageModel imageModel, FetchAvailableIngredientsService availableIngredientsService) {
        this.chatClient = chatClient;
        this.imageModel = imageModel;
        this.availableIngredientsService = availableIngredientsService;
    }

    public Recipe fetchRecipeFor(List<String> ingredients, boolean preferAvailableIngredients) {
        var promptTemplate = new PromptTemplate(preferAvailableIngredients ? recipeForAvailableIngredientsPromptResource : recipeForIngredientsPromptResource);
        var promptMessage = promptTemplate.createMessage(Map.of("ingredients", String.join(",", ingredients)));
        var recipe = chatClient.prompt()
                .messages(promptMessage)
                .function("FetchAvailableIngredientsService", "Fetches ingredients that are available at home", availableIngredientsService)
                .call()
                .entity(Recipe.class);

        var imagePromptTemplate = new PromptTemplate(imageForRecipePromptResource);
        var imagePromptMessage = new ImageMessage(imagePromptTemplate.render(Map.of("recipe", recipe.name(), "ingredients", String.join(",", recipe.ingredients()))));
        var imageGeneration = imageModel.call(new ImagePrompt(List.of(imagePromptMessage))).getResult();
        return new Recipe(recipe, imageGeneration.getOutput().getUrl());
    }

    public void addRecipes(List<Recipe> newRecipes) {
    }

    public List<Recipe> fetchRecipes() {
        return null;
    }
}
