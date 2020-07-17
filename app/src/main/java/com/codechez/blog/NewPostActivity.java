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

public class NewPostActivity extends AppCompatActivity {

    private static final int MAX_LENGTH = 100;
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
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful()){

//                                compression starts

                                File newImageFile = new File(newPostUri.getPath());

                                //compress the image and put the thumbnail
//                                byte[] thumbData = decodeFile(newImageFile);
                                Bitmap b = BitmapFactory.decodeFile(newPostUri.getPath());
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                b.compress(Bitmap.CompressFormat.PNG, 75, baos);
                                byte[] thumbData = baos.toByteArray();

                                UploadTask uploadTask = mStorageRef.child("post_images/thumbs")
                                        .child(randomName + ".jpg").putBytes(thumbData);

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        String errMess = e.getMessage();
                                        Toast.makeText(NewPostActivity.this, "Error: " + errMess, Toast.LENGTH_SHORT).show();
                                    }
                                });

//                                Compression ends

                                Task<Uri> uri = task.getResult().getStorage().getDownloadUrl();
                                while(!uri.isComplete());
                                Uri actual_uri = uri.getResult();
                                String downloadUrl = actual_uri.toString();

                                Map<String, Object> postMap = new HashMap<>();
                                postMap.put("image_url", downloadUrl);
                                postMap.put("post_content", postContent);
                                postMap.put("user_id", current_user_id);
                                postMap.put("timestamp", FieldValue.serverTimestamp());

                                mFirestore.collection("Posts").add(postMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {

                                        if(task.isSuccessful()){
                                            Toast.makeText(NewPostActivity.this, "Post added successfully", Toast.LENGTH_SHORT).show();
                                            Intent sendToMain = new Intent(NewPostActivity.this, MainActivity.class);
                                            startActivity(sendToMain);
                                            finish();
                                        }else{
                                            String errMess = task.getException().getMessage();
                                            Toast.makeText(NewPostActivity.this, "Error: " + errMess, Toast.LENGTH_SHORT).show();
                                        }

                                        newPostProgress.setVisibility(View.INVISIBLE);
                                    }
                                });

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

    //compressing file
    private byte[] decodeFile(File f) {
        Bitmap b = null;

        //Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int IMAGE_MAX_SIZE = 1024;
        int scale = 1;
        if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
            scale = (int) Math.pow(2, (int) Math.ceil(Math.log(IMAGE_MAX_SIZE /
                    (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        try {
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Width :" + b.getWidth() + " Height :" + b.getHeight());


        byte[] data = new byte[0];
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.PNG, 75, baos);
            data = baos.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}