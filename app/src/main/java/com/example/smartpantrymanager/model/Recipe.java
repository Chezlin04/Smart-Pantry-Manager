package com.example.smartpantrymanager.model;

public class Recipe {

    // Unique ID of the recipe in database
    private int id;
    private String recipeName;
    private String instructions;

    // Constructor used when reading an existing recipe from database
    public Recipe(int id, String recipeName, String instructions) {
        this.id = id;
        this.recipeName = recipeName;
        this.instructions = instructions;
    }

    // Returns recipe's database ID
    public int getId() {
        return id;
    }

    // Returns recipe name
    public String getRecipeName() {
        return recipeName;
    }

    // Returns preparation instructions
    public String getInstructions() {
        return instructions;
    }

    // Allows recipe name to be changed if needed
    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    // Allows instructions to be changed if needed
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
