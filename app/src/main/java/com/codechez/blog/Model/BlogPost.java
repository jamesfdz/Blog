package com.codechez.blog.Model;


import com.codechez.blog.BlogPostId;
import com.google.firebase.Timestamp;

import java.util.Date;

public class BlogPost extends com.codechez.blog.BlogPostId {

    public String image_url, post_content, thumb, user_id;

    public Date timestamp;

    public BlogPost(){}

    public BlogPost(String image_url, String post_content, String thumb, String user_id, Date timestamp) {
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

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
