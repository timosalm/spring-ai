package com.example;

import java.util.Arrays;
import java.util.List;

public class FetchRecipeData {

    private String ingredientsStr;
    private boolean preferAvailableIngredients = false;

    public List<String> ingredients() {
        return Arrays.asList(ingredientsStr.split("\\s*,\\s*"));
    }

    public String getIngredientsStr() {
        return ingredientsStr;
    }

    public void setIngredientsStr(String ingredientsStr) {
        this.ingredientsStr = ingredientsStr;
    }

    public boolean isPreferAvailableIngredients() {
        return preferAvailableIngredients;
    }

    public void setPreferAvailableIngredients(boolean preferAvailableIngredients) {
        this.preferAvailableIngredients = preferAvailableIngredients;
    }
}
