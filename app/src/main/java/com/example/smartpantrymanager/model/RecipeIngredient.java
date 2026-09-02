package com.example.smartpantrymanager.model;

public class RecipeIngredient {

    private int id;
    private int recipeId;
    private String ingredientName;
    private double quantity;
    private String unit;


    // Constructor used when reading from database
    public RecipeIngredient(int id, int recipeId, String ingredientName, double quantity, String unit) {
        this.id = id;
        this.recipeId = recipeId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
    }

    // Constructor used when creating a recipe ingredient
    public RecipeIngredient(int recipeId, String ingredientName, double quantity, String unit) {
        this.recipeId = recipeId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
    }

    // Returns ingredient record ID
    public int getId() {
        return id;
    }

    // Returns recipe this ingredient belongs to
    public int getRecipeId() {
        return recipeId;
    }

    // Returns ingredient name
    public String getIngredientName() {
        return ingredientName;
    }

    // Returns required quantity
    public double getQuantity() {
        return quantity;
    }

    // Returns unit
    public String getUnit() {
        return unit;
    }
}