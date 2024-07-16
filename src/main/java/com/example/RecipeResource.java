package com.example;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/recipes")
public class RecipeResource {

    private final RecipeService recipeService;

    public RecipeResource(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @PostMapping("upload")
    public ResponseEntity<Void> addRecipeDocumentsForRag(@RequestParam("file") MultipartFile file) {
        recipeService.addRecipeDocumentForRag(file.getResource());
        return ResponseEntity.noContent().build();
    }

}
