package com.ex.dguide.search;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ex.dguide.R;
import com.ex.dguide.sqlitehelper.SearchPlace;

import java.util.List;

public class PlaceViewHolder extends RecyclerView.ViewHolder {

    TextView infoTextView;
    private ItemClickListener clickListener;

    public PlaceViewHolder(@NonNull View itemView, List<SearchPlace> placesList) {
        super(itemView);
        infoTextView = itemView.findViewById(R.id.infoTextView);

        // Set click listener for the item view
        itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    SearchPlace clickedPlace = placesList.get(position);
                    clickListener.onItemClick(position, clickedPlace.getLatitude(), clickedPlace.getLongitude());

                    Log.d("PlaceViewHolder", "Item clicked at position: " + position +
                            ", Latitude: " + clickedPlace.getLatitude() +
                            ", Longitude: " + clickedPlace.getLongitude());
                }
            }
        });
    }

    public void bind(SearchPlace place) {
        // Set the concatenated information to the TextView
        String placeInfo = place.getPlaceName() + " ," +
                place.getAddress() + "\n" +
                place.getCategory();
        infoTextView.setText(placeInfo);
    }

    // Setter method for click listener
    public void setOnItemClickListener(ItemClickListener listener) {
        this.clickListener = listener;
    }

    // ClickListener interface
    public interface ItemClickListener {
        void onItemClick(int position, double latitude, double longitude);
    }
}













