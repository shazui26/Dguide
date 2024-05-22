package com.ex.dguide.search;

import android.annotation.SuppressLint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.ex.dguide.sqlitehelper.DbHelper;
import com.ex.dguide.sqlitehelper.SearchPlace;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchManager {
    private final DbHelper dbHelper;
    private final List<SearchPlace> placesList;
    private final PlaceAdapter adapter;
    private final RecyclerView recyclerView;

    public SearchManager(DbHelper dbHelper, List<SearchPlace> placesList, PlaceAdapter adapter, RecyclerView recyclerView) {
        this.dbHelper = dbHelper;
        this.placesList = placesList;
        this.adapter = adapter;
        this.recyclerView = recyclerView;
    }

    public void initializeSearch(TextInputEditText searchEditText, boolean searchClosest) {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Not needed for this example
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Trigger search when text changes
                searchPlaces(charSequence.toString(), searchClosest, "", 10);

                // Hide RecyclerView when searchEditText is empty
                if (charSequence.toString().isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not needed for this example
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void searchPlaces(String query, boolean searchClosest, String category, int limit) {
        if (query.isEmpty()) {
            Log.d("SearchManager", "Query is empty. Skipping search.");
            placesList.clear();
            adapter.notifyDataSetChanged();
            return;
        }

        Log.d("SearchManager", "Query: " + query);

        // Pass dbHelper to the constructor
        SearchPlace search = new SearchPlace(dbHelper);

        List<SearchPlace> searchResults;

        if (searchClosest) {
            // Use the searchClosest method to retrieve the closest result
            SearchPlace closestPlace = search.searchClosest(query);
            searchResults = (closestPlace != null) ? Collections.singletonList(closestPlace) : new ArrayList<>();
        } else if (!category.isEmpty()) {
            // Use the new searchByCategory method to retrieve results by category
            searchResults = search.searchByCategory(category, limit);
        } else {
            // Use the search method to retrieve search results
            searchResults = search.search(query, limit);
        }

        placesList.clear();
        // Assuming that SearchPlaces can be converted to Places (adjust accordingly)
        for (SearchPlace searchPlace : searchResults) {
            // Create a new SearchPlace instance and set its properties individually
            SearchPlace places = new SearchPlace(dbHelper);
            places.setLatitude(searchPlace.getLatitude());
            places.setLongitude(searchPlace.getLongitude());
            places.setPlaceName(searchPlace.getPlaceName());
            places.setDescription(searchPlace.getDescription());
            places.setAddress(searchPlace.getAddress());
            places.setCategory(searchPlace.getCategory());
            placesList.add(places);
        }

        recyclerView.setVisibility(View.VISIBLE);
        adapter.notifyDataSetChanged();

        Log.d("SearchManager", "Number of places retrieved: " + placesList.size());
    }

    // Method for searching by category
    public void searchByCategory(String category, int limit) {
        searchPlaces("", false, category, limit);
    }
}


