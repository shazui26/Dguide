package com.ex.dguide;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;

public class Login extends AppCompatActivity {


    Button login;
    EditText email, password;

    private FirebaseAuth mAuth;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mAuth = FirebaseAuth.getInstance();

        login = findViewById(R.id.signin);
        email = findViewById(R.id.userid);
        password = findViewById(R.id.pass);

        login.setOnClickListener(v -> loginUser(email.getText().toString(), password.getText().toString()));

    }


    private void loginUser(String username, String password) {
        mAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                        returnToAddPlaceActivity();
                    } else {
                        if (task.getException() != null) {
                            // Check if the exception is due to an incorrect password
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(Login.this, "Incorrect email or password provided", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Login.this, "Authentication failed: User not found. Please sign up. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void returnToAddPlaceActivity() {
        if (mAuth.getCurrentUser() != null) {
            finish();
        }
    }

    public void gotoSignUp(View view) {

        startActivity(new Intent(getApplicationContext(), SignUp.class));
    }
}
