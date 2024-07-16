package com.example;

import com.fasterxml.jackson.core.io.JsonEOFException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Controller
@RequestMapping("/")
public class RecipeUiController {

    final RecipeService recipeService;
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
        try {
            var recipe = recipeService.fetchRecipeFor(fetchRecipeData.ingredients(), fetchRecipeData.isPreferAvailableIngredients(), fetchRecipeData.isPreferOwnRecipes());
            model.addAttribute("recipe", recipe);
        } catch (Exception e) {
            handleException(model, e);
        }
        model.addAttribute("aiModel", getAiModelNames());
        model.addAttribute("fetchRecipeData", fetchRecipeData);
        return "index";
    }

    private String getAiModelNames() {
        var chatModelName = chatModel.getClass().getSimpleName().replace("ChatModel", "");
        var imageModelName = imageModel.map(
                model -> model.getClass().getSimpleName().replace("ImageModel", "")).orElse("-");
        if (chatModelName.equals(imageModelName)) {
            return chatModelName;
        }
        return chatModelName + " & " + imageModelName;
    }

    private static void handleException(Model model, Exception e) throws Exception {
        if (e instanceof JsonEOFException) {
            model.addAttribute("errorMessage", "Unable to parse LLM response");
        } else {
            throw e;
        }
    }
}
