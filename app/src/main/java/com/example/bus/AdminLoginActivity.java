package com.example.bus;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bus.admin.AdminActivity;
import com.parse.ParseUser;
import com.parse.LogInCallback;
import com.parse.ParseException;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText etAdminUsername, etAdminPassword;
    private Button btnAdminLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        etAdminUsername = findViewById(R.id.etAdminUsername);
        etAdminPassword = findViewById(R.id.etAdminPassword);
        btnAdminLogin = findViewById(R.id.btnAdminLogin);

        btnAdminLogin.setOnClickListener(v -> loginAdmin());
    }

    private void loginAdmin() {
        String username = etAdminUsername.getText().toString().trim();
        String password = etAdminPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Enter admin username and password", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = ProgressDialog.show(this, "Logging in", "Please wait...", true);

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                progressDialog.dismiss();
                if (user != null) {
                    String role = user.getString("role"); // Check user role
                    if ("admin".equals(role)) {
                        Toast.makeText(AdminLoginActivity.this, "Admin Login Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AdminLoginActivity.this, AdminActivity.class));
                        finish();
                    } else {
                        Toast.makeText(AdminLoginActivity.this, "Access Denied! You are not an admin.", Toast.LENGTH_LONG).show();
                        ParseUser.logOut(); // Log out non-admin users
                    }
                } else {
                    Toast.makeText(AdminLoginActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
