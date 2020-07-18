package com.codechez.blog.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codechez.blog.Model.BlogPost;
import com.codechez.blog.R;

import java.util.List;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder> {

    public List<BlogPost> blogPostList;

    public BlogRecyclerAdapter(List<BlogPost> blogPostList){
        this.blogPostList = blogPostList;
    }

    @NonNull
    @Override
    public BlogRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.posts_layout, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BlogRecyclerAdapter.ViewHolder holder, int position) {
        String content = blogPostList.get(position).getPost_content();
        holder.post_content_view.setText(content);
    }

    @Override
    public int getItemCount() {
        return blogPostList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView post_content_view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            post_content_view = itemView.findViewById(R.id.posts_content);
        }

    }
}
