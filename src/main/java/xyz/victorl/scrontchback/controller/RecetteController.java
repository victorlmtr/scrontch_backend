package xyz.victorl.scrontchback.controller;

import xyz.victorl.scrontchback.model.mapper.RecetteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.victorl.scrontchback.model.dto.*;
import xyz.victorl.scrontchback.service.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecetteController {


    private final RecetteService recetteService;
    private final CommentaireService commentaireService;
    private final IngredientRecetteService ingredientRecetteService;
    private final UtilisateurService utilisateurService;
    private final StockIngredientService stockIngredientService;
    private final DietsListService dietsListService;
    private final RecetteMapper recetteMapper;

    @Autowired
    public RecetteController(RecetteService recetteService, CommentaireService commentaireService,
                             IngredientRecetteService ingredientRecetteService,
                             UtilisateurService utilisateurService, StockIngredientService stockIngredientService,
                             DietsListService dietsListService, RecetteMapper recetteMapper) {
        this.recetteService = recetteService;
        this.commentaireService = commentaireService;
        this.ingredientRecetteService = ingredientRecetteService;
        this.utilisateurService = utilisateurService;
        this.stockIngredientService = stockIngredientService;
        this.dietsListService = dietsListService;
        this.recetteMapper = recetteMapper;
    }

    @PostMapping
    public ResponseEntity<RecetteDto> createRecette(@RequestBody RecetteDto recetteDto) {
        RecetteDto createdRecette = recetteService.createRecette(recetteDto);
        return new ResponseEntity<>(createdRecette, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecetteDto> updateRecette(@PathVariable Integer id, @RequestBody RecetteDto recetteDto) {
        RecetteDto updatedRecette = recetteService.updateRecette(recetteDto);
        return new ResponseEntity<>(updatedRecette, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecette(@PathVariable Integer id) {
        recetteService.deleteRecette(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    public ResponseEntity<List<RecetteDto>> getAllRecettes() {
        List<RecetteDto> recettes = recetteService.getAllRecettes();
        return new ResponseEntity<>(recettes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecetteDto> getRecetteById(@PathVariable Integer id) {
        RecetteDto recette = recetteService.getRecetteById(id);
        return new ResponseEntity<>(recette, HttpStatus.OK);
    }

    @GetMapping("/{id}/ingredients")
    public ResponseEntity<List<IngredientRecetteDto>> getIngredientsForAllSteps(@PathVariable Integer id) {
        List<IngredientRecetteDto> ingredients = recetteService.getIngredientsForAllSteps(id);
        return new ResponseEntity<>(ingredients, HttpStatus.OK);
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<CommentaireDto>> getCommentsForRecipe(@PathVariable Integer id) {
        List<CommentaireDto> comments = commentaireService.getCommentsByRecetteId(id);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    @GetMapping("/{id}/rating-info")
    public ResponseEntity<RatingInfoDto> getRatingInfoForRecipe(@PathVariable Integer id) {
        RatingInfoDto ratingInfo = commentaireService.getRatingInfoByRecetteId(id);
        return new ResponseEntity<>(ratingInfo, HttpStatus.OK);
    }

    @GetMapping("/{recipeId}/missing-ingredients/{userId}/count")
    public ResponseEntity<MissingIngredientsResponse> getMissingIngredientsCount(
            @PathVariable Integer recipeId, @PathVariable Integer userId) {

        List<IngredientRecetteDto> recipeIngredients = recetteService.getIngredientsForAllSteps(recipeId);
        List<IngredientDto> userIngredients = stockIngredientService.getIngredientsForUser(userId);

        // Extract the ingredient IDs from the user's ingredients for easier comparison
        List<Integer> userIngredientIds = userIngredients.stream()
                .map(IngredientDto::getId)
                .toList();

        // Count missing ingredients by checking if the recipe ingredient's ID is not in the user's ingredient list
        long missingIngredientsCount = recipeIngredients.stream()
                .map(IngredientRecetteDto::getIdIngredient)  // Extract the IngredientDto from IngredientRecetteDto
                .map(IngredientDto::getId)                   // Extract the ID from IngredientDto
                .filter(idIngredient -> !userIngredientIds.contains(idIngredient))  // Check if not in user's ingredients
                .count();

        return new ResponseEntity<>(new MissingIngredientsResponse(missingIngredientsCount), HttpStatus.OK);
    }

    @GetMapping("/{recipeId}/missing-ingredients/{userId}")
    public ResponseEntity<List<IngredientWithPantryStatusDto>> getMissingIngredients(
            @PathVariable Integer recipeId, @PathVariable Integer userId) {

        List<IngredientRecetteDto> recipeIngredients = recetteService.getIngredientsForAllSteps(recipeId);

        List<IngredientDto> userIngredients = stockIngredientService.getIngredientsForUser(userId);

        List<IngredientWithPantryStatusDto> ingredientsWithPantryStatus = recipeIngredients.stream()
                .map(ingredientRecette -> {

                    IngredientDto ingredientDto = ingredientRecette.getIdIngredient();
                    IngredientDetailsDto ingredientDetailsDto = ingredientRecette.getIdIngredientDetails(); // Assuming this method exists

                    boolean inPantry = userIngredients.stream()
                            .anyMatch(userIngredient -> userIngredient.getId().equals(ingredientDto.getId()));

                    return new IngredientWithPantryStatusDto(ingredientDto, ingredientDetailsDto, inPantry);
                })
                .toList();

        return new ResponseEntity<>(ingredientsWithPantryStatus, HttpStatus.OK);
    }



    @GetMapping("/recent")
    public ResponseEntity<List<RecetteDto>> getFiveMostRecentRecipes() {
        List<RecetteDto> recentRecipes = recetteService.getFiveMostRecentRecipes();
        return new ResponseEntity<>(recentRecipes, HttpStatus.OK);
    }

    @GetMapping("/country/{countryId}")
    public ResponseEntity<List<RecetteDto>> getRecettesByCountry(@PathVariable Integer countryId) {
        List<RecetteDto> recettes = recetteService.getRecipesByCountry(countryId);
        return new ResponseEntity<>(recettes, HttpStatus.OK);
    }

    @GetMapping("/continent/{continentId}")
    public ResponseEntity<List<RecetteDto>> getRecettesByContinent(@PathVariable Integer continentId) {
        List<RecetteDto> recettes = recetteService.getRecipesByContinent(continentId);
        return new ResponseEntity<>(recettes, HttpStatus.OK);
    }

    @GetMapping("/type/{idTypeRecette}")
    public ResponseEntity<List<RecetteDto>> getRecettesByType(@PathVariable Integer idTypeRecette) {
        List<RecetteDto> recettes = recetteService.getRecipesByType(idTypeRecette);
        return new ResponseEntity<>(recettes, HttpStatus.OK);
    }

    @GetMapping("/diet/{idRegimeRecette}")
    public ResponseEntity<List<RecetteDto>> getRecettesByRegime(@PathVariable Integer idRegimeRecette) {
        List<RecetteDto> recettes = recetteService.getRecipesByRegime(idRegimeRecette);
        return new ResponseEntity<>(recettes, HttpStatus.OK);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<RecetteDto>> getRecettesByFilters(
            @RequestParam(required = false) Integer idTypeRecette,
            @RequestParam(required = false) Integer idRegimeRecette,
            @RequestParam(required = false) Integer difficulteRecette,
            @RequestParam(required = false) Integer idPays,
            @RequestParam(required = false) Integer idContinent) {

        List<RecetteDto> recettes = recetteService.getRecipesByFilters(idTypeRecette, idRegimeRecette, difficulteRecette, idPays, idContinent);
        return new ResponseEntity<>(recettes, HttpStatus.OK);
    }



}
