package com.example.smartpantrymanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.adapter.RecipeAdapter;
import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SuggestedRecipesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewRecipes;
    private TextView tvNoRecipes;
    private RecipeAdapter recipeAdapter;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect this Activity to Suggested Recipes layout
        setContentView(R.layout.activity_suggested_recipes);

        // Find views from XML layout
        recyclerViewRecipes = findViewById(R.id.recyclerViewRecipes);
        tvNoRecipes = findViewById(R.id.tvNoRecipes);

        // Bottom navigation buttons
        Button btnPantry = findViewById(R.id.btnPantry);
        Button btnRecipes = findViewById(R.id.btnRecipes);
        Button btnSettings = findViewById(R.id.btnSettings);

        // Display recipes vertically
        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(this));

        // Provides access to SQLite
        databaseHelper = new DatabaseHelper(this);

        // Pantry Navigation
        btnPantry.setOnClickListener(v -> {
            Intent intent = new Intent(SuggestedRecipesActivity.this, MainActivity.class);
            startActivity(intent);

            finish();
        });


        // Recipes Navigation
        btnRecipes.setOnClickListener(v -> {
            // Currently viewing
        });


        // Settings Navigation
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(SuggestedRecipesActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Find and display recipes the user can make
        loadSuggestedRecipes();
    }

    // Checks all recipes and displays only the ones
    // that can be made using the current pantry
    private void loadSuggestedRecipes() {

        List<Recipe> suggestedRecipes = getSuggestedRecipes();

        // No recipes passed the strict-matching rule
        if (suggestedRecipes.isEmpty()) {
            tvNoRecipes.setVisibility(View.VISIBLE);
            recyclerViewRecipes.setVisibility(View.GONE);
        } else {
            // At least one recipe can be made
            tvNoRecipes.setVisibility(View.GONE);
            recyclerViewRecipes.setVisibility(View.VISIBLE);

            // Connect suggested recipes to the RecyclerView
            recipeAdapter = new RecipeAdapter(suggestedRecipes, recipe -> {

                // Open Recipe Detail screen
                Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);

                // RecipeDetailActivity will use this ID to get
                // the correct recipe from database
                intent.putExtra("recipe_id", recipe.getId());

                startActivity(intent);
            });

            recyclerViewRecipes.setAdapter(recipeAdapter);
        }
    }


    // Goes through each recipe in database and keeps only
    // recipes that match the pantry
    private List<Recipe> getSuggestedRecipes() {

        List<Recipe> allRecipes = databaseHelper.getAllRecipes();
        List<PantryItem> pantryItems = databaseHelper.getAllPantryItems();
        List<Recipe> suggestedRecipes = new ArrayList<>();

        // Checks every recipe individually
        for (Recipe recipe : allRecipes) {

            List<RecipeIngredient> requiredIngredients =
                    databaseHelper.getRecipeIngredients(
                            recipe.getId()
                    );

            boolean recipeCanBeMade = true;

            // Every ingredient must be available (If one is missing, the recipe fails)
            for (RecipeIngredient requiredIngredient : requiredIngredients) {

                if (!hasEnoughIngredient(requiredIngredient, pantryItems)) {
                    recipeCanBeMade = false;

                    break;
                }
            }

            // Only add recipes where every requirement passed
            if (recipeCanBeMade && !requiredIngredients.isEmpty()) {
                suggestedRecipes.add(recipe);
            }
        }

        return suggestedRecipes;
    }


    // Checks whether the pantry contains enough of one
    // required recipe ingredient (e.g. 2 eggs for Scrambled eggs)
    private boolean hasEnoughIngredient(RecipeIngredient requiredIngredient,
            List<PantryItem> pantryItems) {

        double totalAvailable = 0;

        for (PantryItem pantryItem : pantryItems) {

            // Compare cleaned ingredient names
            String pantryName = normalizeIngredientName(pantryItem.getIngredientName());
            String requiredName = normalizeIngredientName(requiredIngredient.getIngredientName());


            // Ingredient names must match
            if (pantryName.equals(requiredName)) {

                // Convert quantity into unit used by recipe
                Double convertedQuantity = convertQuantity(
                        pantryItem.getQuantity(),
                        pantryItem.getUnit(),
                        requiredIngredient.getUnit()
                );



                if (convertedQuantity != null) {
                    totalAvailable += convertedQuantity;
                }
            }
        }


        // The pantry must contain at least the required amount
        return totalAvailable >= requiredIngredient.getQuantity();
    }


    // Normalizes ingredient names (e.g. "Eggs" and "eggs" match)
    private String normalizeIngredientName(String name) {

        if (name == null) {
            return "";
        }

        String normalized = name.toLowerCase(Locale.ROOT).trim();

        // Remove extra spaces
        normalized = normalized.replaceAll("\\s+", " ");


        // Handles plural forms (e.g. "tomato" -> "tomatoes")
        if (normalized.endsWith("oes") && normalized.length() > 3) {

            normalized = normalized.substring(
                    0,
                    normalized.length() - 2
            );

            // Example "Berries" -> "berry"
        } else if (normalized.endsWith("ies") && normalized.length() > 3) {

            normalized = normalized.substring(
                    0,
                    normalized.length() - 3
            ) + "y";

            // Handles normal plurals (e.g. "eggs" -> "egg")
        } else if (normalized.endsWith("s") && !normalized.endsWith("ss") && normalized.length() > 1) {

            normalized = normalized.substring(
                    0,
                    normalized.length() - 1
            );
        }

        return normalized;
    }


    // Converts quantities when compatible units are used (e.g. 1kg = 1000g)
    private Double convertQuantity(double quantity, String fromUnit, String toUnit) {

        String from = normalizeUnit(fromUnit);
        String to = normalizeUnit(toUnit);

        // Same unit - no conversion needed
        if (from.equals(to)) {
            return quantity;
        }


        // Weight Conversion

        if (isWeightUnit(from) && isWeightUnit(to)) {
            // First convert value to grams
            double grams;

            if (from.equals("kg")) {
                grams = quantity * 1000;
            } else {
                grams = quantity;
            }


            // Convert grams into the recipe's required unit
            if (to.equals("kg")) {
                return grams / 1000;
            }

            return grams;
        }


        // Volume Conversion

        if (isVolumeUnit(from) && isVolumeUnit(to)) {
            // First convert the value to millilitres
            double millilitres;

            if (from.equals("l")) {
                millilitres = quantity * 1000;
            } else {
                millilitres = quantity;
            }


            // Convert millilitres into required unit
            if (to.equals("l")) {
                return millilitres / 1000;
            }

            return millilitres;
        }

        return null;
    }


    // Converts unit variations into one standard form
    private String normalizeUnit(String unit) {

        if (unit == null) {
            return "";
        }

        String normalized = unit.toLowerCase(Locale.ROOT).trim();

        // Grams
        if (normalized.equals("gram") || normalized.equals("grams")) {
            return "g";
        }

        // Kilograms
        if (normalized.equals("kilogram") || normalized.equals("kilograms") || normalized.equals("kgs")) {
            return "kg";
        }


        // Millilitres
        if (normalized.equals("millilitre") || normalized.equals("millilitres") || normalized.equals("milliliter") || normalized.equals("milliliters")) {
            return "ml";
        }

        // Litres
        if (normalized.equals("litre") || normalized.equals("litres") || normalized.equals("liter") || normalized.equals("liters")) {
            return "l";
        }

        // Pieces
        if (normalized.equals("pieces") || normalized.equals("pcs") || normalized.equals("pc") || normalized.equals("items")) {
            return "piece";
        }


        // Slices
        if (normalized.equals("slices")) {
            return "slice";
        }

        return normalized;
    }


    // Returns true when the unit represents weight
    private boolean isWeightUnit(String unit) {

        return unit.equals("g") || unit.equals("kg");
    }


    // Returns true when the unit represents volume
    private boolean isVolumeUnit(String unit) {

        return unit.equals("ml") || unit.equals("l");
    }


    // Reload suggestions when user returns to this screen
    @Override
    protected void onResume() {
        super.onResume();

        if (databaseHelper != null) {
            loadSuggestedRecipes();
        }
    }
}