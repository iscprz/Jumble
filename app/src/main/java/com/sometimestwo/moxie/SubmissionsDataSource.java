package com.sometimestwo.moxie;

import android.arch.paging.ItemKeyedDataSource;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.sometimestwo.moxie.Model.SubmissionObj;
import com.sometimestwo.moxie.Model.MoxieInfoObj;
import com.sometimestwo.moxie.Utils.Constants;
import com.sometimestwo.moxie.Utils.Utils;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.pagination.DefaultPaginator;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class SubmissionsDataSource extends ItemKeyedDataSource<String, SubmissionObj> {

    private final String TAG = SubmissionsDataSource.class.getSimpleName();

    // For paging through Reddit submission Listings
    DefaultPaginator<Submission> mPaginator;
    MoxieInfoObj mMoxieInfoObj;
    private boolean mIs404 = false;
    private boolean mEndOfSubreddit = false;
    //TODO figure this out
    String defaultSubreddit = "gifs";

    public SubmissionsDataSource(MoxieInfoObj moxieInfoObj) {
        this.mMoxieInfoObj = moxieInfoObj;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params,
                            @NonNull final LoadInitialCallback<SubmissionObj> callback) {
        // gotta make sure we're authenticated before making calls to reddit api
        new Utils.RedditHeartbeatTask(new RedditHeartbeatListener() {
            @Override
            public void redditUserAuthenticated() {
                doLoadInitial(params, callback);
            }
        }).execute();
    }

    private void doLoadInitial(@NonNull LoadInitialParams<String> params,
                               @NonNull final LoadInitialCallback<SubmissionObj> callback) {
        SubredditSort sortBy = App.getMoxieInfoObj().getmSortBy();
        TimePeriod timePeriod = App.getMoxieInfoObj().getmTimePeriod();

        mPaginator = getSubredditSortBuilder()
                .limit(Constants.QUERY_PAGE_SIZE)
                .sorting(sortBy == null ? SubredditSort.HOT : sortBy)
                .timePeriod(timePeriod == null ? TimePeriod.DAY : timePeriod)
                .build();

        new FetchInitialSubmissionsTask(callback).execute();

    }

    private DefaultPaginator.Builder<Submission, SubredditSort> getSubredditSortBuilder() {
        RedditClient redditClient = App.getAccountHelper().getReddit();
        String subredditRequested = null;

        // Get the requested subreddit(s), if any
        if (!mMoxieInfoObj.getmSubredditStack().isEmpty()) {
            subredditRequested = mMoxieInfoObj.getmSubredditStack().peek();
        }
        // We have a subreddit request - does not matter if logged in or not
        if (subredditRequested != null) {
            return redditClient.subreddit(subredditRequested).posts();
        }
        // Logged in with no subreddit request - display user's front page
        else if (!redditClient.getAuthMethod().isUserless()) {
            return redditClient.frontPage();
        }
        // USERLESS and no subreddit request - display default
        else {
            return redditClient.subreddit(defaultSubreddit).posts();
        }
    }

    // Shouldnt be needing this for now since we only ever append to our feed
    @Override
    public void loadBefore(@NonNull final LoadParams<String> params, @NonNull final LoadCallback<SubmissionObj> callback) {
    }

    // Loads the next page
    @Override
    public void loadAfter(@NonNull final LoadParams<String> params, @NonNull final LoadCallback<SubmissionObj> callback) {
        if (!mIs404 && !mEndOfSubreddit) {
            // make sure we're authenticated
            new Utils.RedditHeartbeatTask(new RedditHeartbeatListener() {
                @Override
                public void redditUserAuthenticated() {
                    SubredditSort sortBy = App.getMoxieInfoObj().getmSortBy();
                    TimePeriod timePeriod = App.getMoxieInfoObj().getmTimePeriod();

                    // Listing<Submission> current = mPaginator.getCurrent();
                    mPaginator = getSubredditSortBuilder()
                            .limit(Constants.QUERY_PAGE_SIZE)
                            .sorting(sortBy == null ? SubredditSort.HOT : sortBy)
                            .timePeriod(timePeriod == null ? TimePeriod.DAY : timePeriod)
                            .build();
                    new FetchSubmissionsTask(callback).execute();
                }
            }).execute();

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
                if (submissions.size() < Constants.QUERY_PAGE_SIZE) {
                    mEndOfSubreddit = true;
                }
                submissionObjs = mapSubmissions(submissions);
            } catch (Exception e) {
                // network issue
                if (e instanceof UnknownHostException) {
                    mIs404 = true;
                    submissionObjs = new ArrayList<SubmissionObj>();
                    submissionObjs.add(new SubmissionObj(true));
                }
                //java.net.UnknownHostException: Unable to resolve host "www.reddit.com": No address associated with hostname
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
                    || submission.isNsfw() && mMoxieInfoObj.isHideNSFW()) {
                continue;
            }
            SubmissionObj s = new SubmissionObj();
            s.setSubredditEmpty(false);
            s.setAuthor(submission.getAuthor());
            s.setUrl(submission.getUrl());
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
            s.setEmbeddedMedia(submission.getEmbeddedMedia());
            s.setSaved(submission.isSaved());
            s.setPreviewUrl(submission.getPreview() == null ?
                    null : submission.getPreview().getImages().get(0).getSource().getUrl());

            // Thumbnail can be many different things. Default to submission's URL if no thumbnail.
            // Note: We're ignoring spoiler, nsfw, and potentially any subreddit specific rules
            // which disallow thumbnails(?)
            if (!submission.hasThumbnail()
                    || submission.getThumbnail() == null
                    || "image".equalsIgnoreCase(submission.getThumbnail())
                    || "nsfw".equalsIgnoreCase(submission.getThumbnail())
                    || "spoiler".equalsIgnoreCase(submission.getThumbnail())
                    || "default".equalsIgnoreCase(submission.getThumbnail())
                    || submission.getThumbnail().length() < 1) {
                s.setThumbnail(submission.getUrl());
            } else {
                s.setThumbnail(submission.getThumbnail());
            }

            // domain
            if (submission.getDomain().contains("imgur")) {
                s.setDomain(Constants.SubmissionDomain.IMGUR);
            } else if (submission.getDomain().contains("v.redd.it")) {
                s.setDomain(Constants.SubmissionDomain.VREDDIT);
            } else if (submission.getDomain().contains("i.redd.it")) {
                s.setDomain(Constants.SubmissionDomain.IREDDIT);
            } else if (submission.getDomain().contains("gfycat")) {
                s.setDomain(Constants.SubmissionDomain.GFYCAT);
            } else if (submission.getDomain().contains("youtube")
                    || submission.getDomain().contains("youtu.be")) {
                s.setDomain(Constants.SubmissionDomain.YOUTUBE);
            } else {
                s.setDomain(Constants.SubmissionDomain.OTHER);
            }

            // v.redd.it Videos/GIFS that are crossposted will not have EmbeddedMedia
            // and instead have some sort of crosspost field that JRAW does handle.
            // Since these aren't too common, let's skip support for them for now.
            if (s.getDomain() == Constants.SubmissionDomain.VREDDIT && s.getEmbeddedMedia() == null) {
                continue;
            }
            // add shortened title for displaying purposes if needed
            if (submission.getTitle().length() > Constants.MAX_TITLE_LENGTH) {
                s.setCompactTitle(submission.getTitle().substring(0,
                        Constants.MAX_TITLE_LENGTH) + "...");
            }

           /* // Thumbnail will have value "nsfw" for Guest requests.
            // Thumnail will have "spoiler" in spoiler requests.
            // We're pretty much ignoring them for now.
            if("nsfw".equalsIgnoreCase(submission.getThumbnail())
                    || "spoiler".equalsIgnoreCase(submission.getThumbnail())){
                // Videos
                if(Utils.getSubmissionType(submission.getUrl()) == )
                // Images
            }*/


            res.add(s);
        }
        return res;
    }
}
