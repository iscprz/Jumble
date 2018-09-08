package com.sometimestwo.moxie;

import android.arch.paging.ItemKeyedDataSource;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sometimestwo.moxie.Utils.Constants;

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
    DefaultPaginator<Submission> paginator;

    public SubmissionsDataSource() {

    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params, @NonNull final LoadInitialCallback<Submission> callback) {
        App.getAccountHelper().switchToUserless();
        RedditClient redditClient = App.getAccountHelper().getReddit();

        paginator = redditClient
                .subreddit("pics")
                .posts()
                .limit(Constants.QUERY_PAGE_SIZE) // 50 posts per page
                .sorting(SubredditSort.HOT) // top posts
                .timePeriod(TimePeriod.DAY) // of all time
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
                submissions = paginator.next();
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
                submissions = paginator.next();
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
