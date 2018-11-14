package com.sometimestwo.jumble;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;
import android.arch.paging.ItemKeyedDataSource;

import com.sometimestwo.jumble.Model.SubmissionObj;

public class SubmissionsDataSourceFactory extends DataSource.Factory {
    //creating the mutable live data
    private MutableLiveData<ItemKeyedDataSource<String, SubmissionObj>> postLiveDataSource = new MutableLiveData<>();

    SubmissionsDataSourceFactory(){
    }

    @Override
    public DataSource<String, SubmissionObj> create() {
        //getting our data source object
        SubmissionsDataSource submissionsDataSource = new SubmissionsDataSource();

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
