package com.example.smartpantrymanager.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartpantrymanager.R;
import com.example.smartpantrymanager.model.PantryItem;

import java.util.List;

public class PantryAdapter
        extends RecyclerView.Adapter<PantryAdapter.PantryViewHolder> {

    private List<PantryItem> pantryItems;

    // Listener used to communicate Edit and Delete actions to MainActivity
    public interface OnPantryItemActionListener {

        void onEdit(PantryItem pantryItem);

        void onDelete(PantryItem pantryItem);
    }

    private OnPantryItemActionListener listener;

    // Receives the pantry items and the action listener
    public PantryAdapter(List<PantryItem> pantryItems,
            OnPantryItemActionListener listener) {

        this.pantryItems = pantryItems;
        this.listener = listener;
    }

    // Creates the layout used for one pantry item
    @NonNull
    @Override
    public PantryViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pantry, parent, false);

        return new PantryViewHolder(view);
    }

    // Places the pantry item's information into the layout
    @Override
    public void onBindViewHolder(
            @NonNull PantryViewHolder holder,
            int position) {

        PantryItem item = pantryItems.get(position);

        // Display the ingredient name
        holder.tvIngredientName.setText(item.getIngredientName());

        // Display the quantity and unit
        holder.tvQuantity.setText(item.getQuantity() + " " + item.getUnit());

        // Display the expiry date if one exists
        if (item.getExpiryDate() != null
                && !item.getExpiryDate().isEmpty()) {

            holder.tvExpiryDate.setText(
                    "Expires: " + item.getExpiryDate()
            );

        } else {

            holder.tvExpiryDate.setText(
                    "No expiry date"
            );
        }

        // Tell MainActivity when the Edit button is pressed
        holder.btnEdit.setOnClickListener(v -> {
            listener.onEdit(item);
        });

        // Tell MainActivity when the Delete button is pressed
        holder.btnDelete.setOnClickListener(v -> {
            listener.onDelete(item);
        });
    }

    // Returns the number of pantry items in the list
    @Override
    public int getItemCount() {
        return pantryItems.size();
    }

    // Holds references to the views used for each pantry item
    public static class PantryViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvIngredientName;
        TextView tvQuantity;
        TextView tvExpiryDate;

        Button btnEdit;
        Button btnDelete;

        public PantryViewHolder(
                @NonNull View itemView) {

            super(itemView);

            // Connect the TextViews to the item_pantry layout
            tvIngredientName = itemView.findViewById(R.id.tvIngredientName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvExpiryDate = itemView.findViewById(R.id.tvExpiryDate);

            // Connect the Edit and Delete buttons
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}