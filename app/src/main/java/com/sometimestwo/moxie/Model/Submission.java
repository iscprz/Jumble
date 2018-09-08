package com.sometimestwo.moxie.Model;

/*
* Represents a subreddit post
*/

import java.io.Serializable;

public class Submission implements Serializable{
    private String title;
    private String author;
    private String date_updated;
    private String postURL;
    private String thumbnailURL;
    private String id;
    private String extension;
    private boolean isGif = false;
    private boolean isNsfw = false;



    public Submission(){

    }

    public Submission(String title, String author, String date_updated, String postURL, String thumbnailURL, String id) {
        this.title = title;
        this.author = author;
        this.date_updated = date_updated;
        this.postURL = postURL;
        this.thumbnailURL = thumbnailURL;
        this.id = id;

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate_updated() {
        return date_updated;
    }

    public void setDate_updated(String date_updated) {
        this.date_updated = date_updated;
    }

    public String getPostURL() {
        return postURL;
    }

    public void setPostURL(String postURL) {
        this.postURL = postURL;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }
    
    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public boolean isGif() {
        return isGif;
    }

    public void setIsGif(boolean gif) {
        isGif = gif;
    }

    public boolean isNsfw() {
        return isNsfw;
    }

    public void setNsfw(boolean nsfw) {
        isNsfw = nsfw;
    }
}
