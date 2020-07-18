package com.codechez.blog.Model;


import com.google.firebase.Timestamp;

public class BlogPost {

    public String image_url, post_content, thumb, user_id;

    public Timestamp timestamp;

    public BlogPost(){}

    public BlogPost(String image_url, String post_content, String thumb, String user_id, Timestamp timestamp) {
        this.image_url = image_url;
        this.post_content = post_content;
        this.thumb = thumb;
        this.user_id = user_id;
        this.timestamp = timestamp;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getPost_content() {
        return post_content;
    }

    public void setPost_content(String post_content) {
        this.post_content = post_content;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
