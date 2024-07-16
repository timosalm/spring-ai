package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/")
public class RecipeUiController {

    private static final Logger log = LoggerFactory.getLogger(RecipeUiController.class);

    private final RecipeService recipeService;
    private final ChatModel chatModel;
    private final Optional<ImageModel> imageModel;

    public RecipeUiController(RecipeService recipeService, ChatModel chatModel, Optional<ImageModel> imageModel) {
        this.recipeService = recipeService;
        this.chatModel = chatModel;
        this.imageModel = imageModel;
    }

    @GetMapping
    public String fetchUI(Model model) {
        model.addAttribute("aiModel", getAiModelNames());
        model.addAttribute("fetchRecipeData", new FetchRecipeData());
        return "index";
    }

    @PostMapping
    public String fetchRecipeUiFor(FetchRecipeData fetchRecipeData, Model model) throws Exception {
        Recipe recipe;
        try {
            recipe = recipeService.fetchRecipeFor(fetchRecipeData.ingredients(), fetchRecipeData.isPreferAvailableIngredients(), fetchRecipeData.isPreferOwnRecipes());
        } catch (Exception e) {
            log.info("Retry RecipeUiController:fetchRecipeFor after exception caused by LLM");
            recipe = recipeService.fetchRecipeFor(fetchRecipeData.ingredients(), fetchRecipeData.isPreferAvailableIngredients(), fetchRecipeData.isPreferOwnRecipes());
        }
        model.addAttribute("recipe", recipe);
        model.addAttribute("aiModel", getAiModelNames());
        model.addAttribute("fetchRecipeData", fetchRecipeData);
        return "index";
    }

    private String getAiModelNames() {
        var chatModelName = chatModel.getClass().getSimpleName().replace("ChatModel", "");
        var imageModelName = imageModel.map(
                model -> model.getClass().getSimpleName().replace("ImageModel", "")).orElse("");
        if (chatModelName.equals(imageModelName) || imageModelName.isEmpty()) {
            return chatModelName;
        }
        return chatModelName + " & " + imageModelName;
    }
}
