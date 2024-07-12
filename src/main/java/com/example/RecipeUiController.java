package com.example;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.image.ImageModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/")
public class RecipeUiController {

    final RecipeService recipeService;
    private final ChatModel chatModel;
    private final ImageModel imageModel;

    public RecipeUiController(RecipeService recipeService, ChatModel chatModel, ImageModel imageModel) {
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

    @PostMapping("/recipe")
    public ModelAndView fetchRecipeUiFor(FetchRecipeData fetchRecipeData, Model model) {
        var recipe = recipeService.fetchRecipeFor(fetchRecipeData.ingredients(), fetchRecipeData.isPreferAvailableIngredients());
        var view = new ModelAndView("index");
        model.addAttribute("aiModel", getAiModelNames());
        model.addAttribute("fetchRecipeData", fetchRecipeData);
        model.addAttribute("recipe", recipe);
        return view;
    }

    private String getAiModelNames() {
        var chatModelName = chatModel.getClass().getSimpleName().replace("ChatModel", "");
        var imageModelName = imageModel.getClass().getSimpleName().replace("ImageModel", "");
        if (chatModelName.equals(imageModelName)) {
            return chatModelName;
        }
        return chatModelName + " & " + imageModelName;
    }
}
