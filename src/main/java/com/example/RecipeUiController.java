package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        var aiModelNames = getAiModelNames();
        model.addAttribute("aiModel", String.join(" & ", aiModelNames));
        model.addAttribute("preferAvailableIngredientsOptionEnabled", !aiModelNames.contains("Ollama"));
        if (!model.containsAttribute("fetchRecipeData")) {
            model.addAttribute("fetchRecipeData", new FetchRecipeData());
        }
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
        model.addAttribute("fetchRecipeData", fetchRecipeData);
        return fetchUI(model);
    }

    private Set<String> getAiModelNames() {
        var modelNames = new HashSet<String>();
        modelNames.add(chatModel.getClass().getSimpleName().replace("ChatModel", ""));
        imageModel.map(
                model -> model.getClass().getSimpleName().replace("ImageModel", "")
        ).ifPresent(modelNames::add);
        return modelNames;
    }
}
