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
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.pagination.DefaultPaginator;

import java.util.ArrayList;
import java.util.List;

public class SubmissionsDataSource extends ItemKeyedDataSource<String, SubmissionObj> {

    private final String TAG = SubmissionsDataSource.class.getSimpleName();

    // For paging through Reddit submission Listings
    DefaultPaginator<Submission> mPaginator;
    MoxieInfoObj mMoxieInfoObj;
    private boolean mIs404 = false;
    private boolean mEndOfSubreddit = false;

    public SubmissionsDataSource(MoxieInfoObj moxieInfoObj) {
        this.mMoxieInfoObj = moxieInfoObj;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params, @NonNull final LoadInitialCallback<SubmissionObj> callback) {
        RedditClient redditClient = App.getAccountHelper().getReddit();
        DefaultPaginator.Builder<Submission, SubredditSort> submissionSubredditSortBuilder;
        String subredditRequested = null;
        SubredditSort sortBy = App.getMoxieInfoObj().getmSortBy();
        TimePeriod timePeriod = App.getMoxieInfoObj().getmTimePeriod();

        //TODO figure this out
        String defaultSubreddit = "pics";

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
                        .limit(Constants.QUERY_PAGE_SIZE)
                        .sorting(sortBy == null ? SubredditSort.HOT : sortBy)
                        .timePeriod(timePeriod == null ? TimePeriod.DAY : timePeriod)
                        .build();

        new FetchInitialSubmissionsTask(callback).execute();
    }

    // Shouldnt be needing this for now since we only ever append to our feed
    @Override
    public void loadBefore(@NonNull final LoadParams<String> params, @NonNull final LoadCallback<SubmissionObj> callback) {
    }

    // Loads the next page
    @Override
    public void loadAfter(@NonNull final LoadParams<String> params, @NonNull final LoadCallback<SubmissionObj> callback) {
        if (!mIs404 && !mEndOfSubreddit) {
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
                /* If we've retrieved an amount of pages less than our page size limit,
                *  it's because the subreddit(s) in question are out of submissions to return.
                *
                *  There exists a condition that will break this: subreddit has exactly
                *  Constants.QUERY_PAGE_SIZE amount of submissions, mEndOfSubreddit never gets
                *  set to true, we try loading next page and we find nothing. Ignore this for now.
                * */
                if(submissions.size() < Constants.QUERY_PAGE_SIZE){
                    mEndOfSubreddit = true;
                }
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
        Non-initial submissions loading. Used to page through submissions in a subreddit after
        being initialized. This asyncronous function is what gets called as the recyclerview
        is scrolled. Uses callback to return result if success.
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
