package com.example.bus.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bus.R;
import com.google.android.material.button.MaterialButton;

public class AdminActivity extends AppCompatActivity {
    private MaterialButton btnAddRoute, btnAddBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        btnAddRoute = findViewById(R.id.btn_add_route);
        btnAddBus = findViewById(R.id.btn_add_bus);

        btnAddRoute.setOnClickListener(v ->
                startActivity(new Intent(AdminActivity.this, AddRouteActivity.class)));

        btnAddBus.setOnClickListener(v ->
                startActivity(new Intent(AdminActivity.this, AddBusActivity.class)));
    }
}
