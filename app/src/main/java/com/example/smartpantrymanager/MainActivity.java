package com.example.smartpantrymanager;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.adapter.PantryAdapter;
import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.model.PantryItem;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPantry;
    private PantryAdapter pantryAdapter;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connects this Activity to Pantry screen layout
        setContentView(R.layout.activity_main);

        // Finds the RecyclerView from the layout
        recyclerViewPantry = findViewById(R.id.recyclerViewPantry);

        // Set the RecyclerView to display items vertically
        recyclerViewPantry.setLayoutManager(new LinearLayoutManager(this));

        // Create the database helper
        databaseHelper = new DatabaseHelper(this);

        // Add Ingredient Navigation
        Button btnAddIngredient = findViewById(R.id.btnAddIngredient);

        // Open the Add Ingredient screen when clicked
        btnAddIngredient.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditIngredientActivity.class);
            startActivity(intent);
        });

        // Recipes Navigation
        Button btnRecipes = findViewById(R.id.btnRecipes);

        // Open the Suggested Recipes screen when clicked
        btnRecipes.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SuggestedRecipesActivity.class);
            startActivity(intent);
        });

        // Load pantry items from database
        loadPantryItems();
    }

    // Retrieves pantry data and displays it in the RecyclerView
    private void loadPantryItems() {

        List<PantryItem> pantryItems = databaseHelper.getAllPantryItems();

        // Create the adapter and provide actions for Edit and Delete
        pantryAdapter = new PantryAdapter(pantryItems,
                new PantryAdapter.OnPantryItemActionListener() {

                    @Override
                    public void onEdit(PantryItem pantryItem) {
                        openEditScreen(pantryItem);
                    }

                    @Override
                    public void onDelete(PantryItem pantryItem) {
                        confirmDelete(pantryItem);
                    }
                }
        );

        // Connect the adapter to the RecyclerView
        recyclerViewPantry.setAdapter(pantryAdapter);
    }

    // Opens the Add/Edit screen with the selected item
    private void openEditScreen(PantryItem pantryItem) {

        Intent intent = new Intent(MainActivity.this,
                AddEditIngredientActivity.class
        );

        // Pass the selected item's information to the Edit screen
        intent.putExtra("edit_id", pantryItem.getId());
        intent.putExtra("edit_name", pantryItem.getIngredientName());
        intent.putExtra("edit_quantity", pantryItem.getQuantity());
        intent.putExtra("edit_unit", pantryItem.getUnit());
        intent.putExtra("edit_expiry", pantryItem.getExpiryDate());

        startActivity(intent);
    }

    // Asks the user to confirm before deleting an ingredient
    private void confirmDelete(PantryItem pantryItem) {

        new AlertDialog.Builder(this)
                .setTitle("Delete Ingredient")
                .setMessage("Are you sure you want to delete "
                        + pantryItem.getIngredientName()
                        + "?"
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) -> {

                            // Delete the item from SQLite
                            int result =
                                    databaseHelper.deletePantryItem(
                                            pantryItem.getId()
                                    );

                            if (result > 0) {

                                // Refresh the RecyclerView
                                // after deletion
                                loadPantryItems();
                            }
                        }
                )
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Reload the pantry whenever the Activity becomes visible again
    @Override
    protected void onResume() {
        super.onResume();

        // Refresh the list after returning from Add/Edit screen
        if (databaseHelper != null) {
            loadPantryItems();
        }
    }
}