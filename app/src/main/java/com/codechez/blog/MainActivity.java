package com.codechez.blog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mainToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mainToolbar = findViewById(R.id.custom_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Blog");
        mainToolbar.getOverflowIcon().setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.white), PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    protected void onStart() {
        // this is invoked even when user opens the app from background
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            // User is signed in
            String uid = user.getUid();
        } else {
            // No user is signed in
            Intent mainToLogin = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(mainToLogin);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.menu_logout:
                logout();
                return true;
            case R.id.menu_settings:
                sendToSetUp();
                return true;
            default:
                return false;
        }
    }

    private void sendToSetUp() {
        Intent sendToSetUp = new Intent(MainActivity.this, SetupActivity.class);
        startActivity(sendToSetUp);
    }

    private void logout(){
        mAuth.signOut();
        sendToLogin();
    }

    private void sendToLogin(){
        Intent sendToLogin = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(sendToLogin);
        finish();
    }
}