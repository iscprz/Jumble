package com.sometimestwo.moxie;

import android.arch.paging.ItemKeyedDataSource;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sometimestwo.moxie.Model.SubmissionObj;
import com.sometimestwo.moxie.Model.MoxieInfoObj;
import com.sometimestwo.moxie.Utils.Constants;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkException;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.pagination.DefaultPaginator;

import java.util.ArrayList;
import java.util.List;

public class SubmissionsDataSource extends ItemKeyedDataSource<String, SubmissionObj> {

    private final String TAG = SubmissionsDataSource.class.getSimpleName();

    // For paging through Reddit submission Listings
    DefaultPaginator<Submission> mPaginator;
    MoxieInfoObj mMoxieInfoObj;
    private boolean mIs404 = false;

    public SubmissionsDataSource(MoxieInfoObj moxieInfoObj) {
        this.mMoxieInfoObj = moxieInfoObj;
    }

    /*
        App.getAccountHelper().switchToUser(App.getTokenStore().getUsernames().get(0))
        App.getAccountHelper().getReddit().me().getUsername()
     */


    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params, @NonNull final LoadInitialCallback<SubmissionObj> callback) {
        RedditClient redditClient = App.getAccountHelper().getReddit();
        DefaultPaginator.Builder<Submission, SubredditSort> submissionSubredditSortBuilder;
        String defaultSubreddit = "pics";
        String subredditRequested = null;

        if(!mMoxieInfoObj.getmSubredditStack().isEmpty()){
            subredditRequested = mMoxieInfoObj.getmSubredditStack().peek();
        }
        if (subredditRequested != null) {
            submissionSubredditSortBuilder = redditClient.subreddit(subredditRequested).posts();
        }
        // logged in with no request - display user's front page
        else if (!redditClient.getAuthMethod().isUserless()) {
            submissionSubredditSortBuilder = redditClient.frontPage();
        }
        // USERLESS and no subreddit request - display default
        else {
            submissionSubredditSortBuilder = redditClient.subreddit(defaultSubreddit).posts();
        }

        mPaginator =
                submissionSubredditSortBuilder
                        .limit(Constants.QUERY_PAGE_SIZE) // 50 posts per page
                        .sorting(mMoxieInfoObj.getmSortBy()) // top posts
                        .timePeriod(mMoxieInfoObj.getmTimePeriod()) // of all time
                        .build();
        //App.getTokenStore().getUsernames().get(App.getTokenStore().getUsernames().indexOf("ambits"));

        new FetchInitialSubmissionsTask(callback).execute();
    }

    //shouldnt be needing this for now since we only ever append to our feed
    @Override
    public void loadBefore(@NonNull final LoadParams<String> params, @NonNull final LoadCallback<SubmissionObj> callback) {
    }

    //this will load the next page
    @Override
    public void loadAfter(@NonNull final LoadParams<String> params, @NonNull final LoadCallback<SubmissionObj> callback) {
        if (!mIs404) {
            new FetchSubmissionsTask(callback).execute();
        }
    }

    @NonNull
    @Override
    public String getKey(@NonNull SubmissionObj item) {
        return item.getId();
    }

    /*
        Initial submissions loading. Uses callback to return result if success
     */
    private class FetchInitialSubmissionsTask extends AsyncTask<Void, Void, List<SubmissionObj>> {

        LoadInitialCallback<SubmissionObj> callback;

        FetchInitialSubmissionsTask(final LoadInitialCallback<SubmissionObj> callback) {
            this.callback = callback;
        }

        @Override
        protected List<SubmissionObj> doInBackground(Void... voids) {
            Listing<Submission> submissions = null;
            List<SubmissionObj> submissionObjs = null;

            try {
                submissions = mPaginator.next();
                submissionObjs = mapSubmissions(submissions);
            } catch (NetworkException e) {
                Log.e(SubmissionsDataSource.class.getSimpleName(),
                        " Failed to request initial submissions from reddit: " + e.getMessage());
            }
            return submissionObjs;
        }

        @Override
        protected void onPostExecute(List<SubmissionObj> submissions) {
            super.onPostExecute(submissions);
            callback.onResult(submissions);
        }
    }

    /*
        Non-initial submissions loading. Used to page through submissions in a subreddit after being initialized.
        This asyncronous function is what gets called as the recyclerview is scrolled.
        Uses callback to return result if success
    */
    private class FetchSubmissionsTask extends AsyncTask<Void, Void, List<SubmissionObj>> {

        LoadCallback<SubmissionObj> callback;

        FetchSubmissionsTask(final LoadCallback<SubmissionObj> callback) {
            this.callback = callback;
        }

        @Override
        protected List<SubmissionObj> doInBackground(Void... voids) {
            Listing<Submission> submissions = null;
            List<SubmissionObj> submissionObjs = null;
            try {
                // get the next few submissions
                submissions = mPaginator.next();
                submissionObjs = mapSubmissions(submissions);
                // download any videos if user has selected to play GIF files through settings
                if (true/* sharedprefs.playGifs*/) {
                    for (Submission s : submissions) {
                      /*  if(Helpers.getSubmissionType(s) == Helpers.MediaType.GIF){

                        }*/
                    }
                }
            } catch (Exception e) {
                Log.e(SubmissionsDataSource.class.getSimpleName(),
                        " Failed to request non-initial submissions from reddit: " + e.getMessage());
            }
            return submissionObjs;
        }

        @Override
        protected void onPostExecute(List<SubmissionObj> submissions) {
            super.onPostExecute(submissions);
            callback.onResult(submissions);
        }
    }

    private List<SubmissionObj> mapSubmissions(Listing<Submission> submissions) {
        List<SubmissionObj> res = new ArrayList<SubmissionObj>();

        // If the requested subreddit has 0 posts (non-existant)
        if (submissions == null || submissions.size() < 1) {
            res.add(new SubmissionObj(true));
            mIs404 = true;
            return res;
        }
        mIs404 = false;
        for (Submission submission : submissions) {
            // filter some submissions out here
            if (submission.isSelfPost()
                    || submission.isNsfw() && !mMoxieInfoObj.isAllowNSFW()) {
                continue;
            }
            SubmissionObj s = new SubmissionObj();
            s.setSubredditEmpty(false);
            s.setAuthor(submission.getAuthor());
            s.setUrl(submission.getUrl());
            s.setDomain(submission.getDomain());
            s.setId(submission.getId());
            s.setTitle(submission.getTitle());
            s.setCommentCount(submission.getCommentCount());
            s.setDateCreated(submission.getCreated());
            s.setFullName(submission.getFullName());
            s.setGilded(submission.getGilded());
            s.setHasThumbnail(submission.hasThumbnail());
            s.setHidden(submission.isHidden());
            s.setScoreHidden(submission.isScoreHidden());
            s.setHidden(submission.isHidden());
            s.setLinkFlairText(submission.getLinkFlairText());
            s.setPermalink(submission.getPermalink());
            s.setNSFW(submission.isNsfw());
            s.setLocked(submission.isLocked());
            s.setPostHint(submission.getPostHint());
            s.setRemoved(submission.isRemoved());
            s.setSelfPost(submission.isSelfPost());
            s.setSpam(submission.isSpam());
            s.setSpoiler(submission.isSpoiler());
            s.setSelfText(submission.getSelfText());
            s.setSubreddit(submission.getSubreddit());
            s.setSuggestedSort(submission.getSuggestedSort());
            s.setSubredditFullName(submission.getSubredditFullName());
            s.setVote(submission.getVote());
            s.setVisited(submission.isVisited());
            s.setScore(submission.getScore());
            s.setPreviewUrl(submission.getPreview() == null ?
                    null : submission.getPreview().getImages().get(0).getSource().getUrl());
            // thumbnail will be "nsfw" here if user has selected to hide NSFW thumbnails through
            // reddit preferences. If "nsfw", set postURL as thumbnail for now.
            if ("nsfw".equalsIgnoreCase(submission.getThumbnail())) {
                s.setThumbnail(submission.getUrl());
            } else {
                s.setThumbnail(submission.getThumbnail());
            }

            // Some submissions may be a link that does not end with .jpeg, .gif, etc.
            // Example: "https://imgur.com/qTadRtq?r"
            // Needs to be converted to proper extension

            res.add(s);
        }
        return res;
    }
}
