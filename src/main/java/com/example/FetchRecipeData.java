package com.example;

import java.util.Arrays;
import java.util.List;

public class FetchRecipeData {

    private String ingredientsStr;
    private boolean preferAvailableIngredients = false;
    private boolean preferOwnRecipes = false;
    private boolean scanMyDish = false;

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

    public boolean isPreferOwnRecipes() {
        return preferOwnRecipes;
    }

    public void setPreferOwnRecipes(boolean preferOwnRecipes) {
        this.preferOwnRecipes = preferOwnRecipes;
    }

    public boolean isScanMyDish() {
        return scanMyDish;
    }

    public void setScanMyDish(boolean scanMyDish) {
        this.scanMyDish = scanMyDish;
    }
}
