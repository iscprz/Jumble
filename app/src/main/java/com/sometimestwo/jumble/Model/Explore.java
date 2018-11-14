package com.sometimestwo.jumble.Model;

import java.util.List;

/*
    An Explore Object represents one of the options in the Explore menu. It is reponsible for
    holding details about the Explore drawables, links, subreddit info, etc.
 */
public class Explore {
    int bgDrawableId;
    List<String> subredditList;

    public Explore(int bgDrawableId, List<String> subredditList) {
        this.bgDrawableId = bgDrawableId;
        this.subredditList = subredditList;
    }

    public int getBgDrawableId() {
        return bgDrawableId;
    }

    public void setBgDrawableId(int bgDrawableId) {
        this.bgDrawableId = bgDrawableId;
    }

    public List<String> getSubredditList() {
        return subredditList;
    }

    public void setSubredditList(List<String> subredditList) {
        this.subredditList = subredditList;
    }
}
