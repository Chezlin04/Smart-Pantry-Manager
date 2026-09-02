package com.example.smartpantrymanager.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.smartpantrymanager.model.PantryItem;
import com.example.smartpantrymanager.model.Recipe;
import com.example.smartpantrymanager.model.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "SmartPantry.db";
    private static final int DATABASE_VERSION = 3;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Enables foreign key relationships in SQLite
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);

        db.setForeignKeyConstraintsEnabled(true);
    }

    // Creates pantry table when the database is created
    @Override
    public void onCreate(SQLiteDatabase db) {

        // Pantry Table
        String createPantryTable = "CREATE TABLE pantry_items (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ingredient_name TEXT NOT NULL, " +
                "quantity REAL NOT NULL, " +
                "unit TEXT NOT NULL, " +
                "expiry_date TEXT" +
                ")";
        db.execSQL(createPantryTable);

        // Recipe Table
        String createRecipesTable = "CREATE TABLE recipes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_name TEXT NOT NULL, " +
                "instructions TEXT NOT NULL" +
                ")";
        db.execSQL(createRecipesTable);

        // Stores all ingredients required by each recipe
        String createRecipeIngredientsTable = "CREATE TABLE recipe_ingredients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "recipe_id INTEGER NOT NULL, " +
                "ingredient_name TEXT NOT NULL, " +
                "quantity REAL NOT NULL, " +
                "unit TEXT NOT NULL, " +
                "FOREIGN KEY(recipe_id) REFERENCES recipes(id) " +
                "ON DELETE CASCADE" +
                ")";
        db.execSQL(createRecipeIngredientsTable);

        // Preload recipe collection when database is created
        seedRecipes(db);
    }



    // Handles database upgrades when the database version changes
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // Version 2 adds the recipe system
        if (oldVersion < 2) {
            String createRecipesTable = "CREATE TABLE IF NOT EXISTS recipes (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "recipe_name TEXT NOT NULL, " +
                    "instructions TEXT NOT NULL" +
                    ")";
            db.execSQL(createRecipesTable);

            String createRecipeIngredientsTable = "CREATE TABLE IF NOT EXISTS recipe_ingredients (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "recipe_id INTEGER NOT NULL, " +
                    "ingredient_name TEXT NOT NULL, " +
                    "quantity REAL NOT NULL, " +
                    "unit TEXT NOT NULL, " +
                    "FOREIGN KEY(recipe_id) REFERENCES recipes(id) " +
                    "ON DELETE CASCADE" +
                    ")";
            db.execSQL(createRecipeIngredientsTable);
        }

        // Version 3 adds the preloaded recipes
        if (oldVersion < 3) {
            seedRecipes(db);
        }
    }

    // Retrieves all recipes stored in database
    public List<Recipe> getAllRecipes() {

        List<Recipe> recipes = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        // Retrieve all recipes in alphabetical order
        Cursor cursor = db.rawQuery("SELECT * FROM recipes ORDER BY recipe_name ASC", null);

        // Go through every recipe record
        if (cursor.moveToFirst()) {

            do {

                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String recipeName = cursor.getString(cursor.getColumnIndexOrThrow("recipe_name"));
                String instructions = cursor.getString(cursor.getColumnIndexOrThrow("instructions"));

                // Convert the database row into a Recipe object
                Recipe recipe = new Recipe(id, recipeName, instructions);
                recipes.add(recipe);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return recipes;
    }

    // Retrieves all required ingredients for one recipe
    public List<RecipeIngredient> getRecipeIngredients(int recipeId) {

        List<RecipeIngredient> ingredients = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        // Only retrieve ingredients belonging to selected recipe
        Cursor cursor = db.rawQuery("SELECT * FROM recipe_ingredients " +
                "WHERE recipe_id = ? " +
                "ORDER BY ingredient_name ASC",
                new String[]{
                        String.valueOf(recipeId)
                }
        );

        // Go through every required ingredient
        if (cursor.moveToFirst()) {

            do {

                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int storedRecipeId = cursor.getInt(cursor.getColumnIndexOrThrow("recipe_id"));
                String ingredientName = cursor.getString(cursor.getColumnIndexOrThrow("ingredient_name"));
                double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow("quantity"));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow("unit"));

                // Convert the row into a RecipeIngredient object
                RecipeIngredient ingredient = new RecipeIngredient(
                                id,
                                storedRecipeId,
                                ingredientName,
                                quantity,
                                unit);

                ingredients.add(ingredient);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return ingredients;
    }

    // Retrieves one specific recipe using its database ID
    public Recipe getRecipeById(int recipeId) {

        SQLiteDatabase db = this.getReadableDatabase();

        Recipe recipe = null;

        // Find the recipe with the matching ID
        Cursor cursor = db.rawQuery("SELECT * FROM recipes WHERE id = ?",
                new String[]{
                        String.valueOf(recipeId)
                });

        if (cursor.moveToFirst()) {

            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String recipeName = cursor.getString(cursor.getColumnIndexOrThrow("recipe_name"));
            String instructions = cursor.getString(cursor.getColumnIndexOrThrow("instructions"));

            recipe = new Recipe(id, recipeName, instructions);
        }

        cursor.close();
        db.close();

        return recipe;
    }

    // Adds a new Pantry item to the database
    public long insertPantryItem(PantryItem pantryItem) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Store PantryItem values in the database columns
        values.put("ingredient_name", pantryItem.getIngredientName());
        values.put("quantity", pantryItem.getQuantity());
        values.put("unit", pantryItem.getUnit());
        values.put("expiry_date", pantryItem.getExpiryDate());

        // Insert the new record into the pantry_items table
        long result = db.insert("pantry_items", null, values);

        db.close();
        return result;
    }

    // Retrieves all pantry items from database
    public List<PantryItem> getAllPantryItems() {

        List<PantryItem> pantryItems = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Retrieve all ingredients in alphabetical order
        Cursor cursor = db.rawQuery(
                "SELECT * FROM pantry_items ORDER BY ingredient_name ASC",
                null);

        // Go through each database row/record
        if(cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id")
                );

                String ingredientName = cursor.getString(
                        cursor.getColumnIndexOrThrow("ingredient_name")
                );

                double quantity = cursor.getDouble(
                        cursor.getColumnIndexOrThrow("quantity")
                );

                String unit = cursor.getString(
                        cursor.getColumnIndexOrThrow("unit")
                );

                String expiryDate = cursor.getString(
                        cursor.getColumnIndexOrThrow("expiry_date")
                );

                // Convert the database row into a PantryItem object
                PantryItem pantryItem = new PantryItem(
                        id,
                        ingredientName,
                        quantity,
                        unit,
                        expiryDate
                );

                pantryItems.add(pantryItem);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return pantryItems;
    }

    // Updates an existing pantry item in the database
    public int updatePantryItem(PantryItem pantryItem) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Store updated values
        values.put("ingredient_name", pantryItem.getIngredientName());
        values.put("quantity", pantryItem.getQuantity());
        values.put("unit", pantryItem.getUnit());
        values.put("expiry_date", pantryItem.getExpiryDate());

        // Update only the item with the matching ID
        int result = db.update("pantry_items", values, "id = ?",
                new String[]{String.valueOf(pantryItem.getId())}
        );

        db.close();

        return result;
    }

    // Deletes a pantry item from the database using its ID
    public int deletePantryItem(int id) {

        SQLiteDatabase db = this.getWritableDatabase();

        // Delete only the row/record with the matching ID
        int result = db.delete("pantry_items", "id = ?",
                new String[]{String.valueOf(id)}
        );

        db.close();

        return result;
    }

    // Adds one recipe to the recipes table
    private long insertRecipe(SQLiteDatabase db, String recipeName, String instructions) {
        ContentValues values = new ContentValues();

        values.put("recipe_name", recipeName);
        values.put("instructions", instructions);

        // Returns the ID of newly created recipe
        return db.insert("recipes", null, values);
    }

    // Adds one required ingredient to a recipe
    private void insertRecipeIngredient(SQLiteDatabase db, long recipeId, String ingredientName, double quantity, String unit) {
        ContentValues values = new ContentValues();

        // Connect this ingredient to its recipe
        values.put("recipe_id", recipeId);
        values.put("ingredient_name", ingredientName);
        values.put("quantity", quantity);
        values.put("unit", unit);

        db.insert("recipe_ingredients", null, values);
    }

    // Pre-loads recipes into the database
    private void seedRecipes(SQLiteDatabase db) {

        // Checks if recipes already exist
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM recipes", null);

        int recipeCount = 0;

        if (cursor.moveToFirst()) {
            recipeCount = cursor.getInt(0);
        }

        cursor.close();

        // Stop if recipes have already been selected
        if(recipeCount > 0) {
            return;
        }

        // --- 1. Scrambled Eggs ---
        long scrambledEggsId = insertRecipe(db, "Scrambled Eggs",
                "Beat the eggs with the milk. Melt the butter in a pan. " +
                        "Add the egg mixture and stir gently until cooked.");

        insertRecipeIngredient(db, scrambledEggsId, "egg", 2, "piece");
        insertRecipeIngredient(db, scrambledEggsId, "milk", 50, "ml");
        insertRecipeIngredient(db, scrambledEggsId, "butter", 10, "g");

        // --- 2. Cheese Omelette ---
        long cheeseOmeletteId = insertRecipe(db, "Cheese Omelette",
                "Beat the eggs and pour them into a heated pan with butter. " +
                        "Add the cheese and fold the omelette when cooked.");

        insertRecipeIngredient(db, cheeseOmeletteId, "egg", 2, "piece");
        insertRecipeIngredient(db, cheeseOmeletteId, "cheese", 50, "g");
        insertRecipeIngredient(db, cheeseOmeletteId, "butter", 10, "g");

        // --- 3. Tomato Omelette ---
        long tomatoOmeletteId = insertRecipe(db, "Tomato Omelette",
                "Beat the eggs. Chop the tomato and add it to a pan with butter. " +
                        "Pour in the eggs and cook until set.");

        insertRecipeIngredient(db, tomatoOmeletteId, "egg", 2, "piece");
        insertRecipeIngredient(db, tomatoOmeletteId, "tomato", 1, "piece");
        insertRecipeIngredient(db, tomatoOmeletteId, "butter", 10, "g");

        // --- 4. Grilled Cheese Sandwich ---
        long grilledCheeseId = insertRecipe(db, "Grilled Cheese Sandwich",
                "Butter the bread and place the cheese between the slices. " +
                        "Cook in a pan until the bread is golden and cheese has melted.");

        insertRecipeIngredient(db, grilledCheeseId, "bread", 2, "slice");
        insertRecipeIngredient(db, grilledCheeseId, "cheese", 50, "g");
        insertRecipeIngredient(db, grilledCheeseId, "butter", 10, "g");

        // --- 5. Tomato Sandwich ---
        long tomatoSandwichId = insertRecipe(db, "Tomato Sandwich",
                "Slice the tomato and place it between the slices of bread. " +
                        "Serve immediately.");

        insertRecipeIngredient(db, tomatoSandwichId, "bread", 2, "slice");
        insertRecipeIngredient(db, tomatoSandwichId, "tomato", 1, "piece");

        // --- 6. Chicken Sandwich ---
        long chickenSandwichId = insertRecipe(db, "Chicken Sandwich",
                "Cook and slice the chicken. Place the chicken and tomato " +
                        "between the slices of bread and serve.");

        insertRecipeIngredient(db, chickenSandwichId, "bread", 2, "slice");
        insertRecipeIngredient(db, chickenSandwichId, "chicken", 100, "g");
        insertRecipeIngredient(db, chickenSandwichId, "tomato", 1, "piece");

        // --- 7. Peanut Butter Toast ---
        long peanutButterToastId = insertRecipe(db, "Peanut Butter Toast",
                "Toast the bread until golden. Spread peanut butter over " +
                        "each slice and serve.");

        insertRecipeIngredient(db, peanutButterToastId, "bread", 2, "slice");
        insertRecipeIngredient(db, peanutButterToastId, "peanut butter", 30, "g");

        // --- 8. Cheesy Noodles  ---
        long cheeseNoodlesId = insertRecipe(db, "Cheesy Noodles",
                "Cook noodles until tender. Heat cheese until smooth, " +
                        "then combine it with the noodles.");

        insertRecipeIngredient(db, cheeseNoodlesId, "noodles", 60, "g");
        insertRecipeIngredient(db, cheeseNoodlesId, "cheese", 20, "g");

        // --- 9. Cereal with Milk---
        long cerealId = insertRecipe(db, "Cereal with Milk",
                "Place the cereal in a bowl and pour the milk over it.");

        insertRecipeIngredient(db, cerealId, "cereal", 60, "g");
        insertRecipeIngredient(db, cerealId, "milk", 200, "ml");

        // --- 10. Banana Oatmeal ---
        long oatmealId = insertRecipe(db, "Banana Oatmeal",
                "Cook the oats with milk until soft and creamy. " +
                        "Slice the banana and add it on top before serving.");

        insertRecipeIngredient(db, oatmealId, "oats", 60, "g");
        insertRecipeIngredient(db, oatmealId, "milk", 250, "ml");
        insertRecipeIngredient(db, oatmealId, "banana", 1, "piece");

        // --- 11. Egg and Rice Bowl ---
        long eggRiceId = insertRecipe(db, "Egg and Rice Bowl",
                "Cook the rice until soft. Boil the egg separately " +
                        "and serve it over the rice.");

        insertRecipeIngredient(db, eggRiceId, "rice", 200, "g");
        insertRecipeIngredient(db, eggRiceId, "egg", 1, "piece");

        // --- 12. Chicken and Rice ---
        long chickenRiceId = insertRecipe(db, "Chicken and Rice",
                "Cook the rice until tender. Cook the chicken thoroughly, " +
                        "slice it and serve it with the rice.");

        insertRecipeIngredient(db, chickenRiceId, "rice", 200, "g");
        insertRecipeIngredient(db, chickenRiceId, "chicken", 150, "g");

        // --- 13. Tomato Pasta ---
        long tomatoPastaId = insertRecipe(db, "Tomato Pasta",
                "Cook the pasta until tender. Chop the tomatoes and cook " +
                        "them with the oil. Combine the tomato mixture with the pasta.");

        insertRecipeIngredient(db, tomatoPastaId, "pasta", 200, "g");
        insertRecipeIngredient(db, tomatoPastaId, "tomato", 2, "piece");
        insertRecipeIngredient(db, tomatoPastaId, "oil", 15, "ml");

        // --- 14. Creamy Cheese Pasta ---
        long cheesePastaId = insertRecipe(db, "Creamy Cheese Pasta",
                "Cook the pasta until tender. Heat the milk and cheese " +
                        "together until smooth, then combine with the pasta.");

        insertRecipeIngredient(db, cheesePastaId, "pasta", 200, "g");
        insertRecipeIngredient(db, cheesePastaId, "cheese", 60, "g");
        insertRecipeIngredient(db, cheesePastaId, "milk", 100, "ml");

        // --- 15. Fruit Salad ---
        long fruitSaladId = insertRecipe(db, "Fruit Salad",
                "Peel and chop the apple, banana and orange. " +
                        "Combine the fruit in a bowl and serve.");

        insertRecipeIngredient(db, fruitSaladId, "apple", 1, "piece");
        insertRecipeIngredient(db, fruitSaladId, "banana", 1, "piece");
        insertRecipeIngredient(db, fruitSaladId, "orange", 1, "piece");

    }
}
