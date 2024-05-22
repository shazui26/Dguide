package com.ex.dguide.firbasehelper;

public class Users {
    public String fullName;
    public String username;
    public String email;
    public String gender;

    public Users() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Users(String fullName, String username, String email, String gender) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.gender = gender;
    }
}
