package com.codechez.blog.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.codechez.blog.MainActivity;
import com.codechez.blog.Model.BlogPost;
import com.codechez.blog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.circularreveal.cardview.CircularRevealCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blogPostList;
    private RequestManager glide;
    private FirebaseFirestore firebaseFirestore;
    private Context ctx;
    private FirebaseAuth mAuth;

    public BlogRecyclerAdapter(List<BlogPost> blogPostList, RequestManager glide, Context ctx){
        this.blogPostList = blogPostList;
        this.glide = glide;
        this.ctx = ctx;
    }

    @NonNull
    @Override
    public BlogRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.posts_layout, parent, false);
        mAuth = FirebaseAuth.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BlogRecyclerAdapter.ViewHolder holder, int position) {

        holder.setIsRecyclable(false);

        //getting data from firebase
        String content = blogPostList.get(position).getPost_content();
        String thumbUri = blogPostList.get(position).getThumb();
        String downloadUri = blogPostList.get(position).getImage_url();
        final String userid = blogPostList.get(position).getUser_id();
        long milliseconds = blogPostList.get(position).getTimestamp().getTime();
        String dateString = DateFormat.format("MMM dd, yyyy", new Date(milliseconds)).toString();
        final String blogPostId = blogPostList.get(position).BlogPostId;
        final String current_userid = mAuth.getCurrentUser().getUid();

        //setting it in view
        holder.post_content_view.setText(content);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.post_dummy);
        glide.applyDefaultRequestOptions(requestOptions)
                .load(downloadUri)
                .thumbnail(glide.load(thumbUri))
                .into(holder.post_imageView);

        //setting username and profile image after getting it from firestore using userid
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Users").document(userid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    String name = task.getResult().getString("name");
                    String img_url = task.getResult().getString("img");

                    holder.username_view.setText(name);

                    RequestOptions placeholderRequest = new RequestOptions();
                    placeholderRequest.placeholder(R.drawable.profile_dummy);

                    glide.setDefaultRequestOptions(placeholderRequest).load(img_url).into(holder.profile_imgView);

                }else{
                    String err = task.getException().getMessage();
                    Toast.makeText(ctx, "Error Fetching: " + err, Toast.LENGTH_SHORT).show();
                }
            }
        });

        holder.post_date_view.setText(dateString);

        //get likes count
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (!value.isEmpty()){
                            int count = value.size();
                            holder.postsLikeCount.setText(Integer.toString(count));
                        }else{
                            holder.postsLikeCount.setText("0");
                        }
                    }
                });

        //get likes
        firebaseFirestore.collection("Posts/" + blogPostId + "/Likes")
                .document(current_userid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @SuppressLint("NewApi")
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value.exists()){
                    holder.postsLikeBtn.setImageDrawable(ctx.getDrawable(R.drawable.like_icon_colored));
                }else{
                    holder.postsLikeBtn.setImageDrawable(ctx.getDrawable(R.drawable.like_icon));
                }
            }
        });

        //likes feature
        holder.postsLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts/" + blogPostId + "/Likes").document(current_userid).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(!task.getResult().exists()){
                                    Map<String, Object> likesMap = new HashMap<>();
                                    likesMap.put("timestamp", FieldValue.serverTimestamp());


                                    firebaseFirestore.collection("Posts/" + blogPostId + "/Likes")
                                            .document(current_userid).set(likesMap);
                                }else{
                                    firebaseFirestore.collection("Posts/" + blogPostId + "/Likes")
                                            .document(current_userid).delete();
                                }
                            }
                        });


            }
        });

    }

    @Override
    public int getItemCount() {
        return blogPostList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView post_content_view;
        private ImageView post_imageView;
        private TextView username_view;
        private CircleImageView profile_imgView;
        private TextView post_date_view;
        private ImageView postsLikeBtn;
        private TextView postsLikeCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            post_content_view = itemView.findViewById(R.id.posts_content);
            post_imageView = itemView.findViewById(R.id.posts_mainImg);
            username_view = itemView.findViewById(R.id.posts_username);
            profile_imgView = itemView.findViewById(R.id.posts_user_img);
            post_date_view = itemView.findViewById(R.id.posts_date);

            postsLikeBtn = itemView.findViewById(R.id.posts_like);
            postsLikeCount = itemView.findViewById(R.id.posts_like_count);

        }

    }
}
