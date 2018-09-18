package com.sometimestwo.moxie;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;
import android.arch.paging.ItemKeyedDataSource;

import com.sometimestwo.moxie.Model.SubmissionObj;
import com.sometimestwo.moxie.Model.MoxieInfoObj;

import net.dean.jraw.RedditClient;

public class SubmissionsDataSourceFactory extends DataSource.Factory {
    //creating the mutable live data
    private MutableLiveData<ItemKeyedDataSource<String, SubmissionObj>> postLiveDataSource = new MutableLiveData<>();
    RedditClient redditClient;
    MoxieInfoObj mMoxieInfoObj;

    SubmissionsDataSourceFactory(MoxieInfoObj moxieInfoObj){
        this.mMoxieInfoObj = moxieInfoObj;
    }

    @Override
    public DataSource<String, SubmissionObj> create() {
        //getting our data source object
        SubmissionsDataSource submissionsDataSource = new SubmissionsDataSource(mMoxieInfoObj);

        //posting the datasource to get the values
        postLiveDataSource.postValue(submissionsDataSource);

        //returning the datasource
        return submissionsDataSource;
    }


    //getter for itemlivedatasource
    public MutableLiveData<ItemKeyedDataSource<String, SubmissionObj>> getItemLiveDataSource() {
        return postLiveDataSource;
    }

    /* For refreshing*/
    public void invalidate(){
        if(postLiveDataSource.getValue() != null){
            postLiveDataSource.getValue().invalidate();
        }
    }
}
