package com.ex.dguide;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.ex.dguide.firbasehelper.Places;
import com.ex.dguide.sqlitehelper.DbHelper;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddPlaceActivity extends AppCompatActivity {

    EditText latitude, longitude, description, placeName, address, category;

    Button submit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);
        latitude = findViewById(R.id.Lat);
        longitude = findViewById(R.id.Lon);
        placeName = findViewById(R.id.Place_name);
        description = findViewById(R.id.description);
        address = findViewById(R.id.adress);
        category = findViewById(R.id.Category);
        submit = findViewById(R.id.button);


        double lat = getIntent().getDoubleExtra("latitude", 0.0);
        double lon = getIntent().getDoubleExtra("longitude", 0.0);
        latitude.setText(String.valueOf(lat));
        longitude.setText(String.valueOf(lon));

        FirebaseApp.initializeApp(this);
        try (DbHelper db = new DbHelper(this, "placesData.db", null, 1)) {


            submit.setOnClickListener(v -> {
                if (isConnectedToInternet()) {

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                    if (currentUser == null) {
                        // User is not authenticated, redirect to login
                        Intent loginIntent = new Intent(AddPlaceActivity.this, Login.class);
                        startActivity(loginIntent);
                    } else {
                        // User is authenticated, proceed with place submission
                        String placeNameValue = placeName.getText().toString();
                        String descriptionValue = description.getText().toString();
                        String addressValue = address.getText().toString();
                        String categoryValue = category.getText().toString();
                        double Lat = Double.parseDouble(latitude.getText().toString());
                        double Lon = Double.parseDouble(longitude.getText().toString());
                        String username = currentUser.getDisplayName();
                        if (placeNameValue.isEmpty() || descriptionValue.isEmpty() || addressValue.isEmpty() || categoryValue.isEmpty()) {
                            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("places");
                        DatabaseReference searchDatabaseReference = FirebaseDatabase.getInstance().getReference("searchPlaces");

                        // Create a unique key for the new place
                        String key = databaseReference.push().getKey();

                        // Create a Place object with the values
                        Places place = new Places(Lat, Lon, placeNameValue, descriptionValue, addressValue, categoryValue, username);
                        Places searchPlace = new Places(Lat, Lon, placeNameValue, descriptionValue, addressValue, categoryValue, username);

                        // Save the place to the database
                        assert key != null;
                        databaseReference.child(key).setValue(place);
                        searchDatabaseReference.child(key).setValue(searchPlace)
                                .addOnSuccessListener(aVoid -> {
                                    // Data successfully saved
                                    Toast.makeText(AddPlaceActivity.this, "Place added successfully", Toast.LENGTH_SHORT).show();

                                    new Handler().postDelayed(() -> {
                                        Toast.makeText(AddPlaceActivity.this, "Logged in as " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_OK);
                                        // Finish the current activity
                                        finish();
                                    }, 2000);
                                })
                                .addOnFailureListener(e -> {
                                    // Data failed to save
                                    Toast.makeText(AddPlaceActivity.this, "Failed to add place. Please try again.", Toast.LENGTH_SHORT).show();
                                });
                    }
                } else {
                    // No internet connection
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        // User is authenticated, proceed with place submission to SQLite
                        String placeNameValue = placeName.getText().toString();
                        String descriptionValue = description.getText().toString();
                        String addressValue = address.getText().toString();
                        String categoryValue = category.getText().toString();
                        double Lat = Double.parseDouble(latitude.getText().toString());
                        double Lon = Double.parseDouble(longitude.getText().toString());
                        String username = currentUser.getDisplayName();
                        if (placeNameValue.isEmpty() || descriptionValue.isEmpty() || addressValue.isEmpty() || categoryValue.isEmpty()) {
                            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        boolean success = db.insertPlace(new Places(Lat, Lon, placeNameValue, descriptionValue, addressValue, categoryValue, username));
                        if (success) {
                            Toast.makeText(AddPlaceActivity.this, "Place added to local database", Toast.LENGTH_SHORT).show();


                        } else {
                            Toast.makeText(AddPlaceActivity.this, "Failed to add place. Place already exists.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Intent broadcastIntent = new Intent("NEW_PLACE_ADDED");
                        LocalBroadcastManager.getInstance(AddPlaceActivity.this).sendBroadcast(broadcastIntent);

                        new Handler().postDelayed(() -> {
                            Toast.makeText(AddPlaceActivity.this, "Logged in as " + currentUser.getEmail(), Toast.LENGTH_SHORT).show();

                            setResult(RESULT_OK);
                            // Finish the current activity
                            finish();
                        }, 2000);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

}



