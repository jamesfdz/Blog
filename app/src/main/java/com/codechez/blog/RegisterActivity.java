package com.codechez.blog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.w3c.dom.Text;

public class RegisterActivity extends AppCompatActivity {

    private EditText registerEmail;
    private EditText registerPass;
    private EditText registerConfirmPass;
    private Button registerBtn;
    private ProgressBar registerProgress;
    private Button registerAlreadyAccount;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerEmail = findViewById(R.id.register_email);
        registerPass = findViewById(R.id.register_pass);
        registerConfirmPass = findViewById(R.id.register_confirm_pass_btn);
        registerBtn = findViewById(R.id.register_btn);
        registerProgress = findViewById(R.id.register_progress);
        registerAlreadyAccount = findViewById(R.id.already_acnt_btn);

        mAuth = FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(isNetworkAvailable()){
                    String email = registerEmail.getText().toString();
                    String pass = registerPass.getText().toString();
                    String confirmPass = registerConfirmPass.getText().toString();

                    if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confirmPass)){
                        if(pass.equals(confirmPass)){
                            registerProgress.setVisibility(View.VISIBLE);

                            mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        sendToSetup();
                                    }else{
                                        String errMess = task.getException().getMessage();
                                        Toast.makeText(RegisterActivity.this, "Error: " + errMess, Toast.LENGTH_SHORT).show();
                                    }
                                    registerProgress.setVisibility(View.INVISIBLE);
                                }
                            });

                        }else{
                            Toast.makeText(RegisterActivity.this, "Confirm password field does not match with Password field", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    }

                }else{
                    Toast.makeText(RegisterActivity.this, "No Internet. Please connect to a network", Toast.LENGTH_SHORT).show();
                }

            }
        });

        registerAlreadyAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToLogin();
            }
        });
    }

    private void sendToLogin(){
        Intent sendToLogin = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(sendToLogin);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser != null){
            sendToMain();
        }
    }

    private void sendToSetup(){
        Intent registerToSetup = new Intent(RegisterActivity.this, SetupActivity.class);
        startActivity(registerToSetup);
        finish();
    }

    private void sendToMain() {
        Intent registerToMain = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(registerToMain);
        finish();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}