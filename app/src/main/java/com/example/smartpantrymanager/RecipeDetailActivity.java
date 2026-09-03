package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    private TextView tvRecipeDetailName;
    private TextView tvRecipeIngredients;
    private TextView tvRecipeInstructions;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect this Activity to Recipe Detail layout
        setContentView(R.layout.activity_recipe_detail);

        // Connect Java variables to the XML views
        tvRecipeDetailName = findViewById(R.id.tvRecipeDetailName);
        tvRecipeIngredients = findViewById(R.id.tvRecipeIngredients);
        tvRecipeInstructions = findViewById(R.id.tvRecipeInstructions);
        Button btnBackToRecipes = findViewById(R.id.btnBackToRecipes);

        // Create access to the SQLite database
        databaseHelper = new DatabaseHelper(this);

        // Get recipe ID that was passed from Suggested Recipes screen
        int recipeId = getIntent().getIntExtra("recipe_id", -1);

        // Check if a valid recipe ID was received
        if(recipeId == -1) {
            Toast.makeText(this, "Unable to load recipe", Toast.LENGTH_SHORT).show();
            finish();

            return;
        }

        // Load selected recipe from SQLite
        loadRecipeDetails(recipeId);


        // Return to the previous screen
        btnBackToRecipes.setOnClickListener(v -> {
            finish();
        });
    }

    // Get selected recipe and its ingredients from
    // database and display them
    private void loadRecipeDetails(int recipeId) {

        // Get the main recipe information
        Recipe recipe = databaseHelper.getRecipeById(recipeId);

        // Ensure the recipe exists
        if(recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();

            return;
        }

        // Display recipe name
        tvRecipeDetailName.setText(recipe.getRecipeName());

        // Display preparation instructions
        tvRecipeInstructions.setText(recipe.getInstructions());

        // Retrieve all ingredients required by recipe
        List<RecipeIngredient> ingredients = databaseHelper.getRecipeIngredients(recipeId);

        // Build on readable ingredient list
        StringBuilder ingredientList = new StringBuilder();

        for(RecipeIngredient ingredient : ingredients) {
            ingredientList.append("• ").append(formatQuantity(ingredient.getQuantity()
                    ))
                    .append(" ")
                    .append(ingredient.getUnit())
                    .append(" ")
                    .append(ingredient.getIngredientName())
                    .append("\n");
        }

        // Remove final unnecessary line break
        if(ingredientList.length() > 0) {
            ingredientList.setLength(ingredientList.length() - 1);
        }

        // Display the complete ingredient list
        tvRecipeIngredients.setText(ingredientList.toString());
    }

    // Removes decimal from whole numbers (e.g. 2.0 -> 2)
    private String formatQuantity(double quantity) {

        if (quantity == Math.floor(quantity)) {
            return String.valueOf((int) quantity);
        }

        return String.valueOf(quantity);
    }
}
