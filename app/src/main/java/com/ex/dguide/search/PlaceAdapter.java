package com.ex.dguide.search;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ex.dguide.R;
import com.ex.dguide.sqlitehelper.SearchPlace;

import java.util.List;

// PlaceAdapter.java
public class PlaceAdapter extends RecyclerView.Adapter<PlaceViewHolder> {

    private final List<SearchPlace> placesList;
    private ItemClickListener clickListener;

    public PlaceAdapter(List<SearchPlace> placesList) {
        this.placesList = placesList;
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view, placesList);
    }

    @Override
    public void onBindViewHolder(PlaceViewHolder holder, int position) {
        SearchPlace place = placesList.get(position);
        holder.bind(place);
        holder.setOnItemClickListener((position1, latitude, longitude) -> {
            if (clickListener != null) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    SearchPlace clickedPlace = placesList.get(adapterPosition);
                    clickListener.onItemClick(adapterPosition, clickedPlace.getLatitude(), clickedPlace.getLongitude());

                    Log.d("PlaceAdapter", "Item clicked at position: " + adapterPosition +
                            ", Latitude: " + clickedPlace.getLatitude() +
                            ", Longitude: " + clickedPlace.getLongitude());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return placesList.size();
    }

    // Method to set the click listener
    public void setOnItemClickListener(ItemClickListener listener) {
        this.clickListener = listener;
    }

    // ClickListener interface
    public interface ItemClickListener {
        void onItemClick(int position, double latitude, double longitude);
    }
}










