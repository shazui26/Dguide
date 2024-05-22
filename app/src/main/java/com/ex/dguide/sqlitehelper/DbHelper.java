package com.ex.dguide.sqlitehelper;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ex.dguide.firbasehelper.Places;
import com.ex.dguide.firbasehelper.SearchPlaces;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "placesData.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "places";
    private static final String SEARCH_TABLE_NAME = "searchPlaces";

    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_PLACE_NAME = "place_name";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_USERNAME = "username";


    public DbHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " ("
                + COLUMN_LATITUDE + " REAL, "
                + COLUMN_LONGITUDE + " REAL, "
                + COLUMN_PLACE_NAME + " TEXT, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_ADDRESS + " TEXT, "
                + COLUMN_CATEGORY + " TEXT, "
                + COLUMN_USERNAME + " TEXT)";


        db.execSQL(createTableQuery);
        createSearchTable(db);

    }

    private void createSearchTable(SQLiteDatabase db) {
        String createSearchTableQuery = "CREATE TABLE IF NOT EXISTS " + SEARCH_TABLE_NAME + " ("
                + COLUMN_LATITUDE + " REAL, "
                + COLUMN_LONGITUDE + " REAL, "
                + COLUMN_PLACE_NAME + " TEXT, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_ADDRESS + " TEXT, "
                + COLUMN_CATEGORY + " TEXT, "
                + COLUMN_USERNAME + " TEXT)";
        db.execSQL(createSearchTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void initializeSearchTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        createSearchTable(db); // Ensure that the searchPlaces table is created
    }

    public boolean isSearchTableExists() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM sqlite_master WHERE type='table' AND name=?", new String[]{SEARCH_TABLE_NAME});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }


    public boolean insertPlace(Places place) {
        SQLiteDatabase db = this.getWritableDatabase();

        if (isPlaceNameExists(place.getPlaceName(), db)) {
            Log.d("DbHelper", "Place with the same name already exists");
            db.close(); // Close the database connection
            return false;
        }
        if (isLatLongExists(place.getLatitude(), place.getLongitude(), db)) {
            Log.d("DbHelper", "Place with the same latitude and longitude already exists");
            db.close(); // Close the database connection
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_LATITUDE, place.getLatitude());
        values.put(COLUMN_LONGITUDE, place.getLongitude());
        values.put(COLUMN_PLACE_NAME, place.getPlaceName());
        values.put(COLUMN_DESCRIPTION, place.getDescription());
        values.put(COLUMN_ADDRESS, place.getAddress());
        values.put(COLUMN_CATEGORY, place.getCategory());
        values.put(COLUMN_USERNAME, place.getUsername());

        try {
            long rowId = db.insert(TABLE_NAME, null, values);

            if (rowId != -1) {
                // Data inserted successfully
                Log.d("DbHelper", "Place inserted successfully");
                return true;
            } else {
                // Failed to insert data
                Log.e("DbHelper", "Failed to insert place data");
                return false;
            }
        } catch (Exception e) {
            // Log the exception for debugging
            Log.e("DbHelper", "Exception during database insertion: " + e.getMessage());
            return false;
        } finally {
            logAllPlaces();
            db.close();
        }
    }

    boolean isPlaceNameExists(String placeName, SQLiteDatabase db) {
        try {
            Cursor cursor = db.query(TABLE_NAME, null, COLUMN_PLACE_NAME + "=?", new String[]{placeName}, null, null, null);
            boolean exists = cursor.moveToFirst();
            cursor.close();
            return exists;
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Handle the exception appropriately based on your requirements
        }
    }

    boolean isLatLongExists(double latitude, double longitude, SQLiteDatabase db) {
        try {
            Cursor cursor = db.query(TABLE_NAME, null, COLUMN_LATITUDE + "=? AND " + COLUMN_LONGITUDE + "=?",
                    new String[]{String.valueOf(latitude), String.valueOf(longitude)}, null, null, null);
            boolean exists = cursor.moveToFirst();
            cursor.close();
            return exists;
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Handle the exception appropriately based on your requirements
        }
    }


    public List<Places> getAllPlaces() {
        List<Places> placesList = new ArrayList<>();
        try {
            String selectQuery = "SELECT * FROM " + TABLE_NAME;
            Log.d("DbHelper", "Query: " + selectQuery);
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);
            Log.d("DbHelper", "Before cursor loop");
            if (cursor.moveToFirst()) {
                do {

                    int latitudeIndex = cursor.getColumnIndex(COLUMN_LATITUDE);
                    int longitudeIndex = cursor.getColumnIndex(COLUMN_LONGITUDE);
                    int placeNameIndex = cursor.getColumnIndex(COLUMN_PLACE_NAME);
                    int descriptionIndex = cursor.getColumnIndex(COLUMN_DESCRIPTION);
                    int addressIndex = cursor.getColumnIndex(COLUMN_ADDRESS);
                    int categoryIndex = cursor.getColumnIndex(COLUMN_CATEGORY);
                    int usernameIndex = cursor.getColumnIndex(COLUMN_USERNAME);

                    if (latitudeIndex >= 0 && longitudeIndex >= 0 &&
                            placeNameIndex >= 0 && descriptionIndex >= 0 &&
                            addressIndex >= 0 && categoryIndex >= 0 && usernameIndex >= 0) {

                        double latitude = cursor.getDouble(latitudeIndex);
                        double longitude = cursor.getDouble(longitudeIndex);
                        String placeName = cursor.getString(placeNameIndex);
                        String description = cursor.getString(descriptionIndex);
                        String address = cursor.getString(addressIndex);
                        String category = cursor.getString(categoryIndex);
                        String username = cursor.getString(usernameIndex);

                        Places place = new Places(latitude, longitude, placeName, description, address, category, username);
                        placesList.add(place);
                    }
                } while (cursor.moveToNext());
            }
            Log.d("DbHelper", "Number of places retrieved: " + placesList.size());
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return placesList;
    }

    @SuppressLint("Range")
    public void logAllPlaces() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                // Log data from the cursor
                Log.d("DbHelper", "Place: " +
                        "Latitude: " + cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE)) +
                        ", Longitude: " + cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE)) +
                        ", Name: " + cursor.getString(cursor.getColumnIndex(COLUMN_PLACE_NAME)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
    }

    public void uploadPlacesToFirebase(DatabaseReference mainTableReference, DatabaseReference searchTableReference) {
        List<Places> placesList = getAllPlaces();

        for (Places place : placesList) {
            uploadPlaceToFirebase(place, mainTableReference, searchTableReference);
        }
    }

    public void uploadPlaceToFirebase(Places place, DatabaseReference mainTableReference, DatabaseReference searchTableReference) {
        checkPlaceNameExistence(place.getPlaceName(), mainTableReference, place, exists -> {
            if (!exists) {
                checkLatLongExistence(place.getLatitude(), place.getLongitude(), mainTableReference, place, latLongExists -> {
                    if (!latLongExists) {
                        // Upload to the main table
                        String mainTableKey = mainTableReference.push().getKey();
                        if (mainTableKey != null) {
                            mainTableReference.child(mainTableKey).setValue(place)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("DbHelper", "Place uploaded to main table");

                                        // Once the main table upload is successful, proceed to upload to the search table
                                        String searchTableKey = searchTableReference.push().getKey();
                                        if (searchTableKey != null) {
                                            Log.d("DbHelper", "Before uploading to search table");
                                            searchTableReference.child(searchTableKey).setValue(place)
                                                    .addOnSuccessListener(aVoid1 -> Log.d("DbHelper", "Place uploaded to search table"))
                                                    .addOnFailureListener(e -> {
                                                        Log.e("DbHelper", "Failed to upload place to search table: " + e.getMessage());
                                                        e.printStackTrace(); // Print the stack trace for detailed information
                                                    });
                                        } else {
                                            Log.e("DbHelper", "Failed to generate a unique key for the place in search table");
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("DbHelper", "Failed to upload place to main table: " + e.getMessage()));
                        } else {
                            Log.e("DbHelper", "Failed to generate a unique key for the place in main table");
                        }
                    } else {
                        Log.d("DbHelper", "Place with the same latitude and longitude already exists in main table");
                    }
                });
            } else {
                Log.d("DbHelper", "Place with the same name already exists in main table");
            }
        });
    }



    private void checkPlaceNameExistence(String placeName, DatabaseReference databaseReference, Places place, Consumer<Boolean> callback) {
        if (placeName != null) {
            checkFirebaseExistence("placeName", placeName, databaseReference, exists -> {
                Log.d("DbHelper", "Place name existence check result: " + exists);
                callback.accept(exists);
            });
        } else {
            // Handle the case where placeName is null
            Log.e("DbHelper", "placeName is null");
            callback.accept(false);
        }
    }

    private void checkLatLongExistence(double latitude, double longitude, DatabaseReference databaseReference, Places place, Consumer<Boolean> callback) {
        checkFirebaseExistence("latitude", latitude, databaseReference, callback);
    }

    private void checkFirebaseExistence(String fieldName, Object value, DatabaseReference databaseReference, Consumer<Boolean> callback) {
        Query query;

        if (value instanceof String) {
            Log.d("DbHelper", "Checking existence for place name: " + value);
            query = databaseReference.orderByChild(fieldName).equalTo((String) value);
        } else if (value instanceof Double) {
            long longValue = Double.doubleToRawLongBits((Double) value);
            query = databaseReference.orderByChild(fieldName).equalTo(longValue);
        } else {
            Log.e("DbHelper", "Unsupported data type for value");
            callback.accept(false);
            return;
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean exists = dataSnapshot.exists();
                Log.d("DbHelper", "Firebase existence check result: " + exists);
                callback.accept(exists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DbHelper", "Firebase query cancelled: " + databaseError.getMessage());
                callback.accept(false);
            }
        });
    }





    private boolean isLatLongExistsInFirebase(double latitude, double longitude, DatabaseReference databaseReference) {
        AtomicBoolean exists = new AtomicBoolean(false);

        checkFirebaseExistence("latitude", latitude, databaseReference, exists::set);

        return exists.get();
    }


    public void syncWithFirebase(DatabaseReference firebaseReference) {
        firebaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                Places newPlace = dataSnapshot.getValue(Places.class);
                assert newPlace != null;
                boolean success = insertPlace(newPlace);


                if (success) {

                    Log.e("onChildAdded: ", "new place added from firebase");
                } else {

                    Log.e("DbHelper", "onChildAdded: place already exists");
                }
            }


            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void insertFirebaseDataToSQLite() {
        DatabaseReference firebaseReference = FirebaseDatabase.getInstance().getReference("searchPlaces");

        firebaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<SearchPlaces> firebasePlacesList = new ArrayList<>();

                for (DataSnapshot placeSnapshot : dataSnapshot.getChildren()) {
                    SearchPlaces firebasePlace = placeSnapshot.getValue(SearchPlaces.class);
                    if (firebasePlace != null) {
                        firebasePlacesList.add(firebasePlace);
                    }
                }

                // Insert data into SQLite search_table
                for (SearchPlaces firebasePlace : firebasePlacesList) {
                    insertDataIntoSearchTable(firebasePlace);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle errors if needed
            }
        });
    }


    private void insertDataIntoSearchTable(SearchPlaces place) {
        try (SQLiteDatabase db = this.getWritableDatabase()) {

            if (!isPlaceExistsInSearchTable(place.getPlaceName(), place.getLatitude(), place.getLongitude())) {
                ContentValues values = new ContentValues();
                values.put(COLUMN_LATITUDE, place.getLatitude());
                values.put(COLUMN_LONGITUDE, place.getLongitude());
                values.put(COLUMN_PLACE_NAME, place.getPlaceName());
                values.put(COLUMN_DESCRIPTION, place.getDescription());
                values.put(COLUMN_ADDRESS, place.getAddress());
                values.put(COLUMN_CATEGORY, place.getCategory());
                values.put(COLUMN_USERNAME, place.getUsername());

                // Insert the row into the search_table
                long newRowId = db.insertWithOnConflict(SEARCH_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);


                // Check if the insertion was successful
                if (newRowId != -1) {
                    Log.d("DbHelper", "Row inserted successfully into search_table. Number of rows inserted: " + newRowId);
                } else {
                    Log.e("DbHelper", "Failed to insert row into search_table");
                }
            } else {
                Log.d("DbHelper", "Place with the same name and coordinates already exists in search_table");
            }

       }catch (Exception e) {
            Log.e("DbHelper", "Error during database operation: " + e.getMessage());
            e.printStackTrace();
        }
        // Close the database connection
    }

    private boolean isPlaceExistsInSearchTable(String placeName, double latitude, double longitude) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                SEARCH_TABLE_NAME,
                new String[]{COLUMN_PLACE_NAME, COLUMN_LATITUDE, COLUMN_LONGITUDE},
                COLUMN_PLACE_NAME + " = ? AND " + COLUMN_LATITUDE + " = ? AND " + COLUMN_LONGITUDE + " = ?",
                new String[]{placeName, String.valueOf(latitude), String.valueOf(longitude)},
                null,
                null,
                null
        );

        boolean exists = cursor != null && cursor.getCount() > 0;

        // Close the cursor and database connection
        if (cursor != null) {
            cursor.close();
        }

        return exists;

    }


}





