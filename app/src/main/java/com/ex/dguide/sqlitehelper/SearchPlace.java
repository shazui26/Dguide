package com.ex.dguide.sqlitehelper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SearchPlace {
    private final DbHelper dbHelper;
    private double latitude;
    private double longitude;
    private String placeName;
    private String description;
    private String address;
    private String category;
    private String username;

    // Constructor for initializing the DbHelper
    public SearchPlace(DbHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    // Constructor that takes a Cursor as an argument
    public SearchPlace(DbHelper dbHelper, Cursor cursor) {
        this.dbHelper = dbHelper;
        setPropertiesFromCursor(cursor);
    }
    // Setter methods for other properties
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getPlaceName() {
        return placeName;
    }

    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public String getCategory() {
        return category;
    }
    public String getUsername() {
        return username;
    }



    private void setPropertiesFromCursor(Cursor cursor) {
        if (cursor != null) {
            int latitudeIndex = cursor.getColumnIndex("latitude");
            int longitudeIndex = cursor.getColumnIndex("longitude");
            int placeNameIndex = cursor.getColumnIndex("place_name");
            int descriptionIndex = cursor.getColumnIndex("description");
            int addressIndex = cursor.getColumnIndex("address");
            int categoryIndex = cursor.getColumnIndex("category");
            int usernameIndex = cursor.getColumnIndex("username");
            // Add more indices as needed

            if (latitudeIndex >= 0 && longitudeIndex >= 0 &&
                    placeNameIndex >= 0 && descriptionIndex >= 0 &&
                    addressIndex >= 0 && categoryIndex >= 0) {

                setLatitude(cursor.getDouble(latitudeIndex));
                setLongitude(cursor.getDouble(longitudeIndex));
                setPlaceName(cursor.getString(placeNameIndex));
                setDescription(cursor.getString(descriptionIndex));
                setAddress(cursor.getString(addressIndex));
                setCategory(cursor.getString(categoryIndex));
                setUsername(cursor.getString(usernameIndex));

                // Add more setters as needed
            } else {
                // Handle the case where any column does not exist
                Log.e("SearchPlace", "One or more columns not found");
            }
        }
    }

    private boolean isLatLongExistsInFirebase(double latitude, double longitude, DatabaseReference databaseReference) {
        Query query = databaseReference.orderByChild("latitude").equalTo(latitude);

        AtomicBoolean exists = new AtomicBoolean(false);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                exists.set(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error, if any
            }
        });

        return exists.get();
    }
    // Add a search method that returns a list of SearchPlace instances
    public List<SearchPlace> search(String query, int limit) {
        List<SearchPlace> searchResults = new ArrayList<>();

        // Assuming you have a TABLE_NAME for your searchPlaces table
        String tableName = "searchPlaces";

        // Assuming you have a COLUMN_NAME for the placeName column in your searchPlaces table
        String columnName = "place_name";

        // Perform the SQLite query
        String selectQuery = "SELECT * FROM " + tableName + " WHERE " + columnName + " LIKE ?";
        try (SQLiteDatabase database = dbHelper.getReadableDatabase();
             Cursor cursor = database.rawQuery(selectQuery, new String[]{"%" + query + "%"})) {

            // Process the results
            while (cursor.moveToNext()) {
                SearchPlace searchPlace = new SearchPlace(dbHelper, cursor);
                searchResults.add(searchPlace);
            }
        } catch (Exception e) {
            // Handle any potential exceptions
            e.printStackTrace();
        }

        return searchResults;
    }

    public List<SearchPlace> searchByCategory(String category, int limit) {
        List<SearchPlace> searchResults = new ArrayList<>();

        // Assuming you have a TABLE_NAME for your searchPlaces table
        String tableName = "searchPlaces";

        // Assuming you have a COLUMN_NAME for the category column in your searchPlaces table
        String categoryColumnName = "category";

        // Perform the SQLite query
        String selectQuery = "SELECT * FROM " + tableName + " WHERE " + categoryColumnName + " LIKE ? LIMIT ?";
        try (SQLiteDatabase database = dbHelper.getReadableDatabase();
             Cursor cursor = database.rawQuery(selectQuery, new String[]{"%" + category + "%", String.valueOf(limit)})) {

            // Process the results
            while (cursor.moveToNext()) {
                SearchPlace searchPlace = new SearchPlace(dbHelper, cursor);
                searchResults.add(searchPlace);
            }
        } catch (Exception e) {
            // Handle any potential exceptions
            e.printStackTrace();
        }

        return searchResults;
    }

    public SearchPlace searchClosest(String query) {
        // Assuming you have a TABLE_NAME for your searchPlaces table
        String tableName = "searchPlaces";

        // Assuming you have a COLUMN_NAME for the placeName column in your searchPlaces table
        String columnName = "place_name";

        // Perform the SQLite query with ORDER BY and LIMIT clauses
        String selectQuery = "SELECT * FROM " + tableName + " WHERE " + columnName + " LIKE ? ORDER BY " +
                "CASE WHEN " + columnName + " = ? THEN 0 " +
                "WHEN " + columnName + " LIKE ? THEN 1 " +
                "WHEN " + columnName + " LIKE ? THEN 2 " +
                // Add more conditions based on your matching criteria if needed
                "ELSE 3 END LIMIT 1";

        try (SQLiteDatabase database = dbHelper.getReadableDatabase();
             Cursor cursor = database.rawQuery(selectQuery, new String[]{"%" + query + "%", query, query + "%", "%" + query})) {

            // Process the result
            if (cursor.moveToFirst()) {
                return new SearchPlace(dbHelper, cursor);
            }
        } catch (Exception e) {
            // Handle any potential exceptions
            e.printStackTrace();
        }

        return null;
    }

}



