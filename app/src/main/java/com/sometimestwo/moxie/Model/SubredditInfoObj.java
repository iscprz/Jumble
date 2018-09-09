package com.sometimestwo.moxie.Model;

import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;

/*
    Representation of the current state of the user's browsing preferences such as
    currently-being-viewed subreddit, sort by option, and time period (today, this month, all time).
 */
public class SubredditInfoObj {
    String subreddit;
    SubredditSort mSortBy;
    TimePeriod mTimePeriod;

    public SubredditSort getmSortBy() {
        return mSortBy == null ? SubredditSort.HOT : mSortBy;
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

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }
}
