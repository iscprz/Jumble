package com.sometimestwo.moxie;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.ItemKeyedDataSource;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;


import com.sometimestwo.moxie.Model.SubmissionObj;
import com.sometimestwo.moxie.Utils.Constants;

public class SubmissionsViewModel extends ViewModel {
    //creating livedata for PagedList  and PagedKeyedDataSource
    LiveData<PagedList<SubmissionObj>> postsPagedList;
    LiveData<ItemKeyedDataSource<String, SubmissionObj>> liveDataSource;
    SubmissionsDataSourceFactory submissionsDataSourceFactory;

    //constructor
    public SubmissionsViewModel() {
        //getting our data source factory
         submissionsDataSourceFactory = new SubmissionsDataSourceFactory();

        //getting the live data source from data source factory
        liveDataSource = submissionsDataSourceFactory.getItemLiveDataSource();

        //Getting PagedList config
        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(Constants.QUERY_PAGE_SIZE).build();

        //Building the paged list
        postsPagedList = (new LivePagedListBuilder(submissionsDataSourceFactory, pagedListConfig)).build();
    }

    /* For refreshing*/
    public void invalidate(){
        submissionsDataSourceFactory.invalidate();
    }
}
