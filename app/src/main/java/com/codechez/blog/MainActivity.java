package com.codechez.blog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.codechez.blog.Fragments.AccountFragment;
import com.codechez.blog.Fragments.HomeFragment;
import com.codechez.blog.Fragments.NotificationsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private Toolbar mainToolbar;
    private FloatingActionButton addPostBtn;
    private BottomNavigationView mainBottomNav;
    private HomeFragment homeFragment;
    private NotificationsFragment notificationsFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mainToolbar = findViewById(R.id.custom_toolbar);
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Blog");

        if(mAuth.getCurrentUser() != null){
            addPostBtn = findViewById(R.id.add_post_btn);
            addPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendToNewPost();
                }
            });

            mainBottomNav = findViewById(R.id.main_bottom_nav);

            // Fragments initializations
            homeFragment = new HomeFragment();
            notificationsFragment = new NotificationsFragment();
            accountFragment = new AccountFragment();

            replaceFragment(homeFragment);

            mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()){
                        case R.id.bottom_home:
                            replaceFragment(homeFragment);
                            return true;
                        case R.id.bottom_notifications:
                            replaceFragment(notificationsFragment);
                            return true;
                        case R.id.bottom_account:
                            replaceFragment(accountFragment);
                            return true;
                        default:
                            return false;

                    }
                }
            });
        }


    }

    private void sendToNewPost() {
        Intent sendToNewPost = new Intent(MainActivity.this, NewPostActivity.class);
        startActivity(sendToNewPost);
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

    private void replaceFragment(Fragment fragment){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container, fragment);
        fragmentTransaction.commit();

    }
}