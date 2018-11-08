package com.sometimestwo.moxie.Model;

import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/*
    Representation of the current state of the user's browsing preferences such as
    currently-being-viewed subreddit, sort by option, and time period (today, this month, all time).
 */
public class MoxieInfoObj {
    private Stack<String> mSubredditStack = new Stack<>();
    private SubredditSort mSortBy;
    private TimePeriod mTimePeriod;
    private boolean hideNSFW = true;
    private List<String> defaultSubreddits =
            new ArrayList<>(Arrays.asList(
                    "funny",
                    "gifs",
                    "pics",
                    "earthporn",
                    "aww",
                    "videos",
                    "mildlyinteresting",
                    "wallpapers"));


    public SubredditSort getmSortBy() {
        return mSortBy == null ? SubredditSort.BEST : mSortBy;
    }

    public void setmSortBy(SubredditSort mSortBy) {
        this.mSortBy = mSortBy;
    }

    public TimePeriod getmTimePeriod() {
        return mTimePeriod == null ? TimePeriod.DAY : mTimePeriod;
    }

    public void setmTimePeriod(TimePeriod mTimePeriod) {
        this.mTimePeriod = mTimePeriod;
    }

    public Stack<String> getmSubredditStack() {
        return mSubredditStack;
    }

    public void setmSubredditStack(Stack<String> mSubredditStack) {
        this.mSubredditStack = mSubredditStack;
    }

    public boolean isHideNSFW() {
        return hideNSFW;
    }

    public void setHideNSFW(boolean hideNSFW) {
        this.hideNSFW = hideNSFW;
    }

    public List<String> getDefaultSubreddits() {
        return defaultSubreddits;
    }

    public void setDefaultSubreddits(List<String> defaultSubreddits) {
        this.defaultSubreddits = defaultSubreddits;
    }
}
