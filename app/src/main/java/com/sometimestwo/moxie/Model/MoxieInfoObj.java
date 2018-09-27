package com.sometimestwo.moxie.Model;

import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;

import java.util.Stack;

/*
    Representation of the current state of the user's browsing preferences such as
    currently-being-viewed subreddit, sort by option, and time period (today, this month, all time).
 */
public class MoxieInfoObj {
    Stack<String> mSubredditStack = new Stack<>();
    SubredditSort mSortBy;
    TimePeriod mTimePeriod;
    boolean hideNSFW = true;

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
}
