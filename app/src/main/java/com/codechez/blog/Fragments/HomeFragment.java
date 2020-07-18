package com.codechez.blog.Fragments;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codechez.blog.Adapters.BlogRecyclerAdapter;
import com.codechez.blog.Model.BlogPost;
import com.codechez.blog.R;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {

    private RecyclerView homeFragBlogPost;
    private List<BlogPost> blogPostList;
    private FirebaseFirestore firebaseFirestore;
    private BlogRecyclerAdapter blogRecyclerAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        homeFragBlogPost = view.findViewById(R.id.homeFrag_blogPost);

        blogPostList = new ArrayList<>();

        blogRecyclerAdapter = new BlogRecyclerAdapter(blogPostList);
        homeFragBlogPost.setLayoutManager(new LinearLayoutManager(container.getContext()));
        homeFragBlogPost.setAdapter(blogRecyclerAdapter);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Posts").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for(DocumentChange doc : value.getDocumentChanges()){
                    if(doc.getType() == DocumentChange.Type.ADDED){
                        BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);
                        blogPostList.add(blogPost);

                        blogRecyclerAdapter.notifyDataSetChanged();
                    }
                }
            }
        });


        return view;
    }
}