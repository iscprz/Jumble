package com.sometimestwo.moxie;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;
import android.arch.paging.ItemKeyedDataSource;

import com.sometimestwo.moxie.Model.SubredditInfoObj;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Submission;

public class SubmissionsDataSourceFactory extends DataSource.Factory {
    //creating the mutable live data
    private MutableLiveData<ItemKeyedDataSource<String, Submission>> postLiveDataSource = new MutableLiveData<>();
    RedditClient redditClient;
    SubredditInfoObj mSubredditInfoObj;

    SubmissionsDataSourceFactory(SubredditInfoObj subredditInfoObj){
        this.mSubredditInfoObj = subredditInfoObj;
    }

    @Override
    public DataSource<String, Submission> create() {
        //getting our data source object
        SubmissionsDataSource submissionsDataSource = new SubmissionsDataSource(mSubredditInfoObj);

        //posting the datasource to get the values
        postLiveDataSource.postValue(submissionsDataSource);

        //returning the datasource
        return submissionsDataSource;
    }


    //getter for itemlivedatasource
    public MutableLiveData<ItemKeyedDataSource<String, Submission>> getItemLiveDataSource() {
        return postLiveDataSource;
    }

    /* For refreshing*/
    public void invalidate(){
        postLiveDataSource.getValue().invalidate();
    }
}
