package com.codechez.blog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {

    private Toolbar setupToolbar;
    private CircleImageView setUpImage;
    private Uri mainImageUri = null;
    private EditText setupName;
    private Button setupBtn;
    private StorageReference mStorageRef;
    private FirebaseAuth mAuth;
    private ProgressBar setupProgress;
    private FirebaseFirestore firestore;
    private String user_id;
    private boolean isChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        setupToolbar = findViewById(R.id.custom_toolbar);
        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");

        setupProgress = findViewById(R.id.setup_progress);

        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        firestore = FirebaseFirestore.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        setUpImage = findViewById(R.id.setup_image);
        setupName = findViewById(R.id.setup_name);
        setupBtn = findViewById(R.id.setup_save_btn);

        setupProgress.setVisibility(View.VISIBLE);
        setupBtn.setEnabled(false);

        firestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().exists()){
                        String name = task.getResult().getString("name");
                        String img_url = task.getResult().getString("img");

                        mainImageUri = Uri.parse(img_url);

                        setupName.setText(name);
                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.profile_default);

                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(img_url).into(setUpImage);
                    }
                }else{
                    String errMess = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Error: " + errMess, Toast.LENGTH_SHORT).show();
                }

                setupProgress.setVisibility(View.INVISIBLE);
                setupBtn.setEnabled(true);
            }
        });

        setUpImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(SetupActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(SetupActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }else{
//                        https://github.com/ArthurHub/Android-Image-Cropper
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1, 1)
                                .start(SetupActivity.this);
                    }
                }else{
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1, 1)
                            .start(SetupActivity.this);
                }
            }
        });

        setupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String username = setupName.getText().toString();
                setupProgress.setVisibility(View.VISIBLE);

                if(isChanged){

                    if(!TextUtils.isEmpty(username) && mainImageUri != null){
                        //upload image to firebase storage
                        user_id = mAuth.getCurrentUser().getUid();

                        StorageReference imagePath = mStorageRef.child("profile images").child(user_id + ".jpg");

                        imagePath.putFile(mainImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if(task.isSuccessful()){

                                    storeInFirestore(task, username);

                                }else{
                                    String errMess = task.getException().getMessage();
                                    Toast.makeText(SetupActivity.this, "Error: " + errMess, Toast.LENGTH_SHORT).show();
                                    setupProgress.setVisibility(View.INVISIBLE);
                                }


                            }
                        });
                    }else{
                        storeInFirestore(null, username);
                    }
                }else{
                    storeInFirestore(null, username);
                }

            }
        });
    }

    private void storeInFirestore(Task<UploadTask.TaskSnapshot> task, String username) {

        Uri url;

        if(task != null){
            Task<Uri> download_uri = task.getResult().getStorage().getDownloadUrl();
            while(!download_uri.isComplete());
            url = download_uri.getResult();
        }else{
            url = mainImageUri;
        }

        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", username);
        userMap.put("img", url.toString());

        firestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    Toast.makeText(SetupActivity.this, "Account settings saved successfully", Toast.LENGTH_SHORT).show();
                    sendToMain();
                }else{
                    String errMess = task.getException().getMessage();
                    Toast.makeText(SetupActivity.this, "Error: " + errMess, Toast.LENGTH_SHORT).show();
                }

                setupProgress.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void sendToMain() {
        Intent sendToMain = new Intent(SetupActivity.this, MainActivity.class);
        startActivity(sendToMain);
        finish();
    }

    // once image is cropped the result is given back here
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageUri = result.getUri();

                setUpImage.setImageURI(mainImageUri);

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}