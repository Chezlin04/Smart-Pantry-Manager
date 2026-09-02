package com.example.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.database.DatabaseHelper;
import com.example.smartpantrymanager.model.Recipe;

import java.util.List;
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    // List of recipes that will be displayed
    private List<Recipe> recipes;

    // Database helper used to count ingredients in each recipe
    private DatabaseHelper databaseHelper;

    // Listener used to tell the Activity when the user presses the View Recipe button
    public interface OnRecipeClickListener {

        void onRecipeClick(Recipe recipe);
    }

    private OnRecipeClickListener listener;

    // Receives the recipe list and click listener
    public RecipeAdapter(
            List<Recipe> recipes,
            OnRecipeClickListener listener) {

        this.recipes = recipes;
        this.listener = listener;
    }

    // Creates layout for one recipe item
    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_recipe,
                parent,
                false);

        // Creates the database helper
        databaseHelper = new DatabaseHelper(parent.getContext());
        return new RecipeViewHolder(view);
    }

    // Places the recipe information into layout
    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {

        Recipe recipe = recipes.get(position);

        // Display the recipe name
        holder.tvRecipeName.setText(recipe.getRecipeName());

        // Retrieve the ingredients belonging to this recipe
        // and display how many ingredients it requires.
        int ingredientCount = databaseHelper.getRecipeIngredients(recipe.getId()).size();

        String ingredientText;

        // Use singular wording when there is only one ingredient
        if (ingredientCount == 1) {
            ingredientText = ingredientCount + " ingredient";

        } else {
            ingredientText = ingredientCount + " ingredients";
        }

        holder.tvIngredientCount.setText(ingredientText);


        //Tell the Activity which recipe was selected when
        // the View Recipe button is clicked
        holder.btnViewRecipe.setOnClickListener(v -> {

            if (listener != null) {
                listener.onRecipeClick(recipe);
            }
        });
    }

    // Returns number of recipes being displayed
    @Override
    public int getItemCount() {
        return recipes.size();
    }


    //Holds references to the views inside item_recipe.xml.
    public static class RecipeViewHolder extends RecyclerView.ViewHolder {

        TextView tvRecipeName;
        TextView tvIngredientCount;
        Button btnViewRecipe;

        public RecipeViewHolder(@NonNull View itemView) {

            super(itemView);

            // Connect Java variables to the XML views
            tvRecipeName = itemView.findViewById(R.id.tvRecipeName);
            tvIngredientCount = itemView.findViewById(R.id.tvIngredientCount);
            btnViewRecipe = itemView.findViewById(R.id.btnViewRecipe);
        }
    }
}
