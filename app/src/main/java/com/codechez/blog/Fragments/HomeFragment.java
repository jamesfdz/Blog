package com.codechez.blog.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.codechez.blog.Adapters.BlogRecyclerAdapter;
import com.codechez.blog.Model.BlogPost;
import com.codechez.blog.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private RecyclerView homeFragBlogPost;
    private List<BlogPost> blogPostList;
    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private FirebaseAuth mAuth;
    private DocumentSnapshot lastVisible;
    private boolean isFirstDataLoaded = true;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        homeFragBlogPost = view.findViewById(R.id.homeFrag_blogPost);

        mAuth = FirebaseAuth.getInstance();

        blogPostList = new ArrayList<>();

        blogRecyclerAdapter = new BlogRecyclerAdapter(blogPostList, Glide.with(getActivity().getApplicationContext()), getActivity().getApplicationContext());
        homeFragBlogPost.setLayoutManager(new LinearLayoutManager(container.getContext()));
        homeFragBlogPost.setAdapter(blogRecyclerAdapter);

        // Getting the data from Firestore

        if(mAuth.getCurrentUser() != null){
            firebaseFirestore = FirebaseFirestore.getInstance();

            homeFragBlogPost.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean reachedBottom = !recyclerView.canScrollVertically(1);

                    if(reachedBottom){
                        loadMorePost();
                    }

                }
            });

            Query firstQuery = firebaseFirestore.collection("Posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(10);

            firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                    if(!value.isEmpty()){
                        if(isFirstDataLoaded){
                            lastVisible = value.getDocuments().get(value.size() - 1);
                        }
                        // getting data when post is added
                        for(DocumentChange doc : value.getDocumentChanges()){
                            if(doc.getType() == DocumentChange.Type.ADDED){

                                String blogPostId = doc.getDocument().getId();

                                BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);

                                if(isFirstDataLoaded){
                                    blogPostList.add(blogPost);
                                }else{
                                    blogPostList.add(0, blogPost);
                                }


                                blogRecyclerAdapter.notifyDataSetChanged();
                            }
                        }
                        isFirstDataLoaded = false;

                    }

                }
            });
        }

        return view;
    }


    public void loadMorePost(){
        Query nextQuery = firebaseFirestore.collection("Posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible)
                .limit(10);

        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if(!value.isEmpty()){
                    lastVisible = value.getDocuments().get(value.size() - 1);
                    // getting data when post is added
                    for(DocumentChange doc : value.getDocumentChanges()){
                        if(doc.getType() == DocumentChange.Type.ADDED){

                            String blogPostId = doc.getDocument().getId();

                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                            blogPostList.add(blogPost);

                            blogRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }

            }
        });
    }
}