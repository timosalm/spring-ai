package com.example;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
class RecipeServiceTest {

    @MockBean
    ImageModel imageModelMock;

    @MockBean
    FetchAvailableIngredientsFunction fetchAvailableIngredientsServiceMock;

    @Autowired
    RecipeService recipeService;

    @Disabled
    @Test
    void testFunctionCalling() {
      var imageModelResponse = mock(ImageResponse.class, Mockito.RETURNS_DEEP_STUBS);
      given(this.imageModelMock.call(any())).willReturn(imageModelResponse);
      given(this.fetchAvailableIngredientsServiceMock.apply(any())).willReturn(new FetchAvailableIngredientsFunction.Response(List.of("Salmon")));

      var recipe = recipeService.fetchRecipeFor(Arrays.asList("Curry Paste", "Rice", "Coconut milk"), false);
      verify(fetchAvailableIngredientsServiceMock, times(0)).apply(any());

      var recipeWithFunctionCalling = recipeService.fetchRecipeFor(Arrays.asList("Curry Paste", "Rice", "Coconut milk"), true);
      verify(fetchAvailableIngredientsServiceMock, times(1)).apply(any());
    }

    @Disabled
    @Test
    void testImageGeneration() {
        given(this.fetchAvailableIngredientsServiceMock.apply(any())).willReturn(new FetchAvailableIngredientsFunction.Response(List.of("Salmon")));
        var recipe = recipeService.fetchRecipeFor(Arrays.asList("Curry Paste", "Rice", "Coconut milk"), false);
        assertTrue(StringUtils.hasText(recipe.imageUrl()) && !recipe.imageUrl().equalsIgnoreCase("example"));
    }
  
}