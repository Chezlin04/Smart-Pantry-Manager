package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.model.PantryItem;

public class AddEditIngredientActivity extends AppCompatActivity {

    private EditText etIngredientName;
    private EditText etQuantity;
    private EditText etUnit;
    private EditText etExpiryDate;

    private DatabaseHelper databaseHelper;

    // ID of the item being edited (-1 means a new item is being added)
    private int editId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Connect Activity to the XML layout
        setContentView(R.layout.activity_add_edit_ingredient);

        // Find heading and description
        TextView tvFormTitle = findViewById(R.id.tvFormTitle);
        TextView tvFormSubtitle = findViewById(R.id.tvFormSubtitle);

        // Find input fields
        etIngredientName = findViewById(R.id.etIngredientName);
        etQuantity = findViewById(R.id.etQuantity);
        etUnit = findViewById(R.id.etUnit);
        etExpiryDate = findViewById(R.id.etExpiryDate);

        // Find the main form button
        Button btnSaveIngredient = findViewById(R.id.btnSaveIngredient);

        // Create connection to SQLite
        databaseHelper = new DatabaseHelper(this);

        // Check whether an existing pantry item was passed
        editId = getIntent().getIntExtra("edit_id", -1);


        // Edit Mode
        if (editId != -1) {
            tvFormTitle.setText("Edit Ingredient");
            tvFormSubtitle.setText("Update the ingredient details below");
            btnSaveIngredient.setText("Update Ingredient");

            // Blue represents Edit/Update actions
            btnSaveIngredient.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.edit_blue));

            // Fill form with existing ingredient data
            etIngredientName.setText(getIntent().getStringExtra("edit_name"));
            etQuantity.setText(String.valueOf(getIntent().getDoubleExtra("edit_quantity", 0)));
            etUnit.setText(getIntent().getStringExtra("edit_unit"));
            etExpiryDate.setText(getIntent().getStringExtra("edit_expiry"));

        } else {
            // Add Mode
            tvFormTitle.setText("Add Ingredient");
            tvFormSubtitle.setText("Add an ingredient to your pantry");
            btnSaveIngredient.setText("Save Ingredient");

            // Green represents Add/Create actions
            btnSaveIngredient.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.add_green));
        }

        // Save or update when the user presses the button
        btnSaveIngredient.setOnClickListener(v -> saveIngredient());
    }

    // Validate the form and save ingredient to SQLite
    private void saveIngredient() {
        String ingredientName = etIngredientName.getText().toString().trim();
        String quantityText = etQuantity.getText().toString().trim();
        String unit = etUnit.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();

        // Checks if required fields are complete
        if (ingredientName.isEmpty()) {
            etIngredientName.setError("Please enter an ingredient");
            etIngredientName.requestFocus();
            return;
        }
        if (quantityText.isEmpty()) {
            etQuantity.setError("Please enter a quantity");
            etQuantity.requestFocus();
            return;
        }
        if (unit.isEmpty()) {
            etUnit.setError("Please enter a unit");
            etUnit.requestFocus();
            return;
        }

        // Convert the quantity from text into a number
        double quantity;
        try {
            quantity = Double.parseDouble(quantityText);
        } catch (NumberFormatException e) {
            etQuantity.setError("Please enter a valid number");
            etQuantity.requestFocus();
            return;
        }

        // Quantity must be greater than zero
        if (quantity <= 0) {
            etQuantity.setError("Quantity must be greater than zero");
            etQuantity.requestFocus();
            return;
        }

        // Decides whether this is a new item or an existing item
        if (editId == -1) {

            // CREATE: Add a new pantry item
            PantryItem pantryItem = new PantryItem(
                    ingredientName,
                    quantity,
                    unit,
                    expiryDate
            );

            long result = databaseHelper.insertPantryItem(pantryItem);

            if (result != -1) {

                Toast.makeText(
                        this,
                        "Ingredient added successfully",
                        Toast.LENGTH_SHORT
                ).show();

                finish();

            } else {

                Toast.makeText(
                        this,
                        "Failed to add ingredient",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } else {

            // UPDATE: Modify the existing pantry item
            PantryItem pantryItem = new PantryItem(
                    editId,
                    ingredientName,
                    quantity,
                    unit,
                    expiryDate
            );

            int result = databaseHelper.updatePantryItem(
                    pantryItem
            );

            if (result > 0) {

                Toast.makeText(
                        this,
                        "Ingredient updated successfully",
                        Toast.LENGTH_SHORT
                ).show();

                finish();

            } else {

                Toast.makeText(
                        this,
                        "Failed to update ingredient",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }
}