package com.example;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@Service
public class FetchAvailableIngredientsService implements Function<FetchAvailableIngredientsService.Request, FetchAvailableIngredientsService.Response> {

    @Override
    public Response apply(Request request) {
        var alwaysAvailableIngredients = Arrays.asList("wine", "salt", "pepper", "olive oil", "broth", "rice");
        var availableIngredientsInFridge = Arrays.asList("salmon", "zucchini");
        var availableIngredients = Stream.concat(alwaysAvailableIngredients.stream(), availableIngredientsInFridge.stream()).toList();
        return new Response(availableIngredients);
    }

    public record Request() {}
    public record Response(List<String> availableIngredients) {}
}
