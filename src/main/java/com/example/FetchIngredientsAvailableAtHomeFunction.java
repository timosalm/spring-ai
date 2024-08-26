package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

@Description("Fetches ingredients that are available at home")
@Service("fetchIngredientsAvailableAtHome")
public class FetchIngredientsAvailableAtHomeFunction implements Function<FetchIngredientsAvailableAtHomeFunction.Request, FetchIngredientsAvailableAtHomeFunction.Response> {

    private static final Logger log = LoggerFactory.getLogger(FetchIngredientsAvailableAtHomeFunction.class);

    private final List<String> alwaysAvailableIngredients;
    private final List<String> availableIngredientsInFridge;

    public FetchIngredientsAvailableAtHomeFunction(@Value("${app.always-available-ingredients}") List<String> alwaysAvailableIngredients,
                                                   @Value("${app.available-ingredients-in-fridge}") List<String> availableIngredientsInFridge) {
        this.alwaysAvailableIngredients = alwaysAvailableIngredients;
        this.availableIngredientsInFridge = availableIngredientsInFridge;
    }

    @Override
    public Response apply(Request request) {
        log.info("Fetching ingredients available at home function called by LLM");
        var availableIngredients = Stream.concat(availableIngredientsInFridge.stream(),alwaysAvailableIngredients.stream()).toList();
        return new Response(availableIngredients);
    }

    public record Request() {}

    public record Response(List<String> ingredientsAvailableAtHome) {}
}
