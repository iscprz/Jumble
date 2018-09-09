package com.sometimestwo.moxie;

import android.arch.paging.ItemKeyedDataSource;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sometimestwo.moxie.Model.SubredditInfoObj;
import com.sometimestwo.moxie.Utils.Constants;
import com.sometimestwo.moxie.Utils.Helpers;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.pagination.DefaultPaginator;

import java.util.List;

public class SubmissionsDataSource extends ItemKeyedDataSource<String, Submission> {

    private final String TAG = SubmissionsDataSource.class.getSimpleName();

    // For paging through Reddit submission Listings
    DefaultPaginator<Submission> mPaginator;
    SubredditInfoObj mSubredditInfoObj;

    public SubmissionsDataSource(SubredditInfoObj subredditInfoObj) {
        this.mSubredditInfoObj = subredditInfoObj;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params, @NonNull final LoadInitialCallback<Submission> callback) {
        App.getAccountHelper().switchToUserless();
        RedditClient redditClient = App.getAccountHelper().getReddit();

        mPaginator = redditClient
                .subreddit(mSubredditInfoObj.getSubreddit())
                .posts()
                .limit(Constants.QUERY_PAGE_SIZE) // 50 posts per page
                .sorting(mSubredditInfoObj.getmSortBy()) // top posts
                .timePeriod(mSubredditInfoObj.getmTimePeriod()) // of all time
                .build();
        new FetchInitialSubmissionsTask(callback).execute();
    }

    //shouldnt be needing this for now since we only ever append to our feed
    @Override
    public void loadBefore(@NonNull final LoadParams<String> params, @NonNull final LoadCallback<Submission> callback) {
    }

    //this will load the next page
    @Override
    public void loadAfter(@NonNull final LoadParams<String> params, @NonNull final LoadCallback<Submission> callback) {
        new FetchSubmissionsTask(callback).execute();
    }

    @NonNull
    @Override
    public String getKey(@NonNull Submission item) {
        return item.getId();
    }

    /*
        Initial submissions loading. Uses callback to return result if success
     */
    private class FetchInitialSubmissionsTask extends AsyncTask<Void, Void, List<Submission>> {

        LoadInitialCallback<Submission> callback;

        FetchInitialSubmissionsTask(final LoadInitialCallback<Submission> callback) {
            this.callback = callback;
        }

        @Override
        protected List<Submission> doInBackground(Void... voids) {
            Listing<Submission> submissions = null;
            try {
                submissions = mPaginator.next();
            } catch (Exception e) {
                Log.e(SubmissionsDataSource.class.getSimpleName(),
                        " Failed to request initial submissions from reddit: " + e.getMessage());
            }
            return submissions;
        }

        @Override
        protected void onPostExecute(List<Submission> submissions) {
            super.onPostExecute(submissions);
            callback.onResult(submissions);
        }
    }

    /*
        Non-initial submissions loading. Used to page through submissions in a subreddit after being initialized.
        This asyncronous function is what gets called as the recyclerview is scrolled.
        Uses callback to return result if success
    */
    private class FetchSubmissionsTask extends AsyncTask<Void, Void, List<Submission>> {

        LoadCallback<Submission> callback;

        FetchSubmissionsTask(final LoadCallback<Submission> callback) {
            this.callback = callback;
        }

        @Override
        protected List<Submission> doInBackground(Void... voids) {
            Listing<Submission> submissions = null;
            try {
                // get the next few submissions
                submissions = mPaginator.next();

                // download any videos if user has selected to play GIF files through settings
                if(true/* sharedprefs.playGifs*/){
                    for (Submission s : submissions){
                        if(Helpers.getMediaType(s) == Helpers.MediaType.GIF){

                        }
                    }
                }
            } catch (Exception e) {
                Log.e(SubmissionsDataSource.class.getSimpleName(),
                        " Failed to request non-initial submissions from reddit: " + e.getMessage());
            }
            return submissions;
        }

        @Override
        protected void onPostExecute(List<Submission> submissions) {
            super.onPostExecute(submissions);
            callback.onResult(submissions);
        }
    }
}
