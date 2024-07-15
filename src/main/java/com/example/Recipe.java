package com.example;

import java.util.List;

public record Recipe(String name, String description, List<String> ingredients, List<String> instructions, String imageUrl) {

    public Recipe(Recipe recipe, String imageUrl) {
        this(recipe.name, recipe.description, recipe.ingredients, recipe.instructions, imageUrl);
    }
}