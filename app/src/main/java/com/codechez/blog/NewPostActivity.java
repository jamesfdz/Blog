package com.codechez.blog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private static final String TAG = "NewPostActivity";
    private Toolbar newPostToolbar;
    private EditText newPostContent;
    private ImageView newPostImage;
    private Button newPostBtn;
    private ProgressBar newPostProgress;
    private Uri newPostUri = null;
    private StorageReference mStorageRef;
    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private String current_user_id;
    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        newPostToolbar = findViewById(R.id.custom_toolbar);
        setSupportActionBar(newPostToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        newPostContent = findViewById(R.id.new_post_content);
        newPostImage = findViewById(R.id.new_post_image);
        newPostBtn = findViewById(R.id.new_post_blog_btn);
        newPostProgress = findViewById(R.id.new_post_progress);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(ContextCompat.checkSelfPermission(NewPostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(NewPostActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }else{
//                        https://github.com/ArthurHub/Android-Image-Cropper
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setMinCropResultSize(512, 512)
                                .setAspectRatio(1, 1)
                                .start(NewPostActivity.this);
                    }
                }else{
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setMinCropResultSize(512, 512)
                            .setAspectRatio(1, 1)
                            .start(NewPostActivity.this);
                }
            }
        });

        newPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String postContent = newPostContent.getText().toString();

                if(!TextUtils.isEmpty(postContent) && newPostUri != null){
                    newPostProgress.setVisibility(View.VISIBLE);

                    final String randomName = UUID.randomUUID().toString();

                    StorageReference filepath = mStorageRef.child("post_images").child(randomName + ".jpg");

                    filepath.putFile(newPostUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){

                                Task<Uri> uri = task.getResult().getStorage().getDownloadUrl();
                                while(!uri.isComplete());
                                Uri actual_uri = uri.getResult();

                                final String downloadUrl = actual_uri.toString();

//                                compression starts

                                File newImageFile = new File(newPostUri.getPath());

                                try {
                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxHeight(100)
                                            .setMaxWidth(100)
                                            .setQuality(2)
                                            .compressToBitmap(newImageFile);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] thumbData = baos.toByteArray();

                                //compress the image and put the thumbnail

                                UploadTask uploadTask = mStorageRef.child("post_images/thumbs")
                                        .child(randomName + ".jpg").putBytes(thumbData);

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        String downloadThumbUri = taskSnapshot.getStorage().getDownloadUrl().toString();

                                        Map<String, Object> postMap = new HashMap<>();
                                        postMap.put("image_url", downloadUrl);
                                        postMap.put("thumb", downloadThumbUri);
                                        postMap.put("post_content", postContent);
                                        postMap.put("user_id", current_user_id);
                                        postMap.put("timestamp", FieldValue.serverTimestamp());

                                        mFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {

                                                if(task.isSuccessful()){
                                                    Toast.makeText(NewPostActivity.this, "Post added successfully", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                }else{
                                                    String errMess = task.getException().getMessage();
                                                    Toast.makeText(NewPostActivity.this, "Error: " + errMess, Toast.LENGTH_SHORT).show();
                                                }

                                                newPostProgress.setVisibility(View.INVISIBLE);
                                            }
                                        });
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        String errMess = e.getMessage();
                                        Toast.makeText(NewPostActivity.this, "Error: " + errMess, Toast.LENGTH_SHORT).show();
                                    }
                                });

//                                Compression ends

                            }else{
                                String errMess = task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this, "Error: " + errMess, Toast.LENGTH_SHORT).show();
                                newPostProgress.setVisibility(View.INVISIBLE);
                            }

                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                newPostUri = result.getUri();

                newPostImage.setImageURI(newPostUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        }
    }
}