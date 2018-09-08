package com.sometimestwo.moxie;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;
import android.arch.paging.ItemKeyedDataSource;

import net.dean.jraw.models.Submission;

public class SubmissionsDataSourceFactory extends DataSource.Factory {
    //creating the mutable live data
    private MutableLiveData<ItemKeyedDataSource<String, Submission>> postLiveDataSource = new MutableLiveData<>();

    @Override
    public DataSource<String, Submission> create() {
        //getting our data source object
        SubmissionsDataSource submissionsDataSource = new SubmissionsDataSource();

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
