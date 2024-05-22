package com.ex.dguide;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.ex.dguide.firbasehelper.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class SignUp extends AppCompatActivity {

    EditText fullname, user, email, password, con_pass;
    Button btn_register;
    RadioButton rmale, rfemale;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        fullname = findViewById(R.id.fullname);
        user = findViewById(R.id.user);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        con_pass = findViewById(R.id.con_pass);
        btn_register = findViewById(R.id.register);
        rmale = findViewById(R.id.male);
        rfemale = findViewById(R.id.female);


        btn_register.setOnClickListener(v -> registerUser());

    }

    private void registerUser() {
        String fullNameValue = fullname.getText().toString();
        String usernameValue = user.getText().toString();
        String emailValue = email.getText().toString();
        String passwordValue = password.getText().toString();
        String confirmPasswordValue = con_pass.getText().toString();
        String gender = "";


        if (rmale.isChecked()) {
            gender = "male";
        }
        if (rfemale.isChecked()) {
            gender = "female";
        }

        if (!passwordValue.equals(confirmPasswordValue)) {
            Toast.makeText(this, "Passwords do not match. PLease try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fullNameValue.isEmpty() || usernameValue.isEmpty() || emailValue.isEmpty() || passwordValue.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String finalGender = gender;

        mAuth.createUserWithEmailAndPassword(emailValue, passwordValue)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(usernameValue)
                                .build();
                        assert user != null;
                        user.updateProfile(profileUpdates)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(SignUp.this, "Registration successful.", Toast.LENGTH_SHORT).show();
                                        // Save additional user details to the database
                                        saveUserDetails(fullNameValue, usernameValue, emailValue, finalGender);
                                    } else {
                                        Toast.makeText(SignUp.this, "Failed to set display name.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(SignUp.this, "Registration failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserDetails(String fullName, String username, String email, String gender) {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        Users newUser = new Users(fullName, username, email, gender);

        mDatabase.child(userId).setValue(newUser);
        finish();

    }


}