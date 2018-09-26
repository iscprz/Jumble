package com.sometimestwo.moxie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.facebook.stetho.common.LogUtil;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.github.piasy.biv.view.BigImageView;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;
import com.sometimestwo.moxie.Model.SubmissionObj;
import com.sometimestwo.moxie.Utils.Constants;
import com.sometimestwo.moxie.Utils.Utils;

public class FragmentFullDisplay extends Fragment implements OnTaskCompletedListener {

    private SubmissionObj mCurrSubmission;
    private boolean mPrefsAllowNSFW;
    private boolean mAllowCloseOnClick;

    /* Titles */
    TextView mTitleTextView;
    TextView mSubredditTextView;
    RelativeLayout mTitleContainer;

    /* Snackbar */
    private ImageView mButtonComments;
    private ImageView mButtonUpvote;
    private ImageView mButtonDownvote;
    private ImageView mButtonOverflow;
    private LinearLayout mSnackbarContainer;

    /* Big zoomie view*/
    private FrameLayout mBigImageViewContainer;
    private BigImageView mBigImageView;

    /* Video view for VREDDIT videos*/
    private FrameLayout mVideoviewContainer;
    private VideoView mVideoView;

    private ProgressBar mProgressBar;

    /* Exoplayer */
    private FrameLayout mExoplayerContainer;
    private BandwidthMeter bandwidthMeter;
    private DefaultTrackSelector trackSelector;
    private SimpleExoPlayer player;
    private ProgressBar progressBar;
    private DataSource.Factory mediaDataSourceFactory;
    private int currentWindow;
    private long playbackPosition;
    private Timeline.Window window;
    //private FrameLayout mExoplayerContainerLarge;
    private PlayerView mExoplayer;


    public static FragmentFullDisplay newInstance() {
        return new FragmentFullDisplay();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        unpackArgs();

        // Read relevant permission settings
        SharedPreferences prefs = getContext().getSharedPreferences(Constants.KEY_GET_PREFS_SETTINGS, Context.MODE_PRIVATE);

        mPrefsAllowNSFW = prefs.getBoolean(Constants.SETTINGS_ALLOW_NSFW, false);
        mAllowCloseOnClick = prefs.getBoolean(Constants.SETTINGS_ALLOW_BIGDISPLAY_CLOSE_CLICK, false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        BigImageViewer.initialize(GlideImageLoader.with(getContext()));
        View v = inflater.inflate(R.layout.fragment_full_media_display, container, false);


        /* Titles */
        mTitleTextView = (TextView) v.findViewById(R.id.big_display_title);
        mSubredditTextView = (TextView) v.findViewById(R.id.big_display_subreddit);

        /* Snackbar */
        mButtonComments = (ImageView) v.findViewById(R.id.big_display_snack_bar_comments);
        mButtonUpvote = (ImageView) v.findViewById(R.id.big_display_snack_bar_upvote);
        mButtonDownvote = (ImageView) v.findViewById(R.id.big_display_snack_bar_downvote);
        mButtonOverflow = (ImageView) v.findViewById(R.id.big_display_snack_bar_overflow);
        mSnackbarContainer = (LinearLayout) v.findViewById(R.id.big_display_snack_bar_container);

        /* Main zoomie image view*/
        mBigImageViewContainer = (FrameLayout) v.findViewById(R.id.full_displayer_big_image_container);
        mBigImageView = (BigImageView) v.findViewById(R.id.big_image_viewer);

        /* Video view*/
        mVideoviewContainer = (FrameLayout) v.findViewById(R.id.full_displayer_videoview_container);
        mVideoView = (VideoView) v.findViewById(R.id.full_displayer_videoview);

        /* Loading progress bar */
        //mProgressBar = (ProgressBar) v.findViewById(R.id.full_displayer_progress);

        /* Exo player*/
        mExoplayerContainer = (FrameLayout) v.findViewById(R.id.full_displayer_exoplayer_container);
        mExoplayer = (PlayerView) v.findViewById(R.id.full_displayer_exoplayer);
        bandwidthMeter = new DefaultBandwidthMeter();
        mediaDataSourceFactory = new DefaultDataSourceFactory(getContext(),
                Util.getUserAgent(getContext(), "Moxie"),
                (TransferListener<? super DataSource>) bandwidthMeter);
        window = new Timeline.Window();


        setupFullViewer();

        /* Exit on tap if settings option is enabled*/
        mBigImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mAllowCloseOnClick) {
                    closeFullDisplay();
                }
            }
        });
        mExoplayerContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAllowCloseOnClick){
                    closeFullDisplay();
                }
            }
        });
        mVideoviewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAllowCloseOnClick){
                    closeFullDisplay();
                }
            }
        });
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        // Notify calling fragment that we're closing the submission viewer
        // Don't need to pass anything back. Pass an empty intent for now
        Intent resultIntent = new Intent();
        getTargetFragment().onActivityResult(FragmentHome.KEY_INTENT_GOTO_BIG_DISPLAY, Activity.RESULT_OK, resultIntent);
        releaseExoPlayer();
        super.onDestroy();
    }

    private void unpackArgs() {
        try {
            // Submission to be viewed
            mCurrSubmission = (SubmissionObj) this.getArguments().get(Constants.ARGS_SUBMISSION_OBJ);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Pop this fragment off the stack, effectively closing the submission viewer.
    private void closeFullDisplay() {
        //releaseExoPlayer();
        try {
            getActivity().getSupportFragmentManager().popBackStack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setupFullViewer() {
        /* title*/
        mTitleTextView.setText(mCurrSubmission.getCompactTitle() != null
                ? mCurrSubmission.getCompactTitle() : mCurrSubmission.getTitle());
        mSubredditTextView.setText(mCurrSubmission.getSubreddit());


        /* Set up image/gif view*/
        // Prioritize using the cleaned URL. Some post URLS point to indirect images:
        // Indirect url example: imgur.com/AktjAWe
        // Cleaned url example: imgur.com/AktjAWe.jpg
        String imageUrl = (mCurrSubmission.getCleanedUrl() != null)
                ? mCurrSubmission.getCleanedUrl() : mCurrSubmission.getUrl();

        if(mCurrSubmission.getSubmissionType() == Constants.SubmissionType.IMAGE){
            mExoplayerContainer.setVisibility(View.GONE);
            mVideoviewContainer.setVisibility(View.GONE);
            mBigImageViewContainer.setVisibility(View.VISIBLE);
            mBigImageView.showImage(Uri.parse(mCurrSubmission.getUrl()));

            //mBigImageView.setVisibility(View.VISIBLE);
            //mExoplayer.setVisibility(View.GONE);
            //TODO: handle invalid URL
        }else if (mCurrSubmission.getSubmissionType() == Constants.SubmissionType.GIF
                || mCurrSubmission.getSubmissionType() == Constants.SubmissionType.VIDEO){
           // mBigImageView.setVisibility(View.GONE);
                mBigImageViewContainer.setVisibility(View.GONE);
            // VREDDIT submissions require a video view
            if(mCurrSubmission.getDomain() == Utils.SubmissionDomain.VREDDIT){
                mVideoviewContainer.setVisibility(View.VISIBLE);
                mExoplayerContainer.setVisibility(View.GONE);
                String url = mCurrSubmission.getEmbeddedMedia().getRedditVideo().getFallbackUrl();
                try {
                    new Utils.FetchVRedditGifTask(getContext(),url, this).execute();
                } catch (Exception e) {
                    LogUtil.e(e, "Error v.redd.it url: " + url);
                }
                //mExoplayer.setVisibility(View.GONE);
            }
            else{
                mExoplayer.setVisibility(View.VISIBLE);
                mVideoviewContainer.setVisibility(View.GONE);
               // mVideoView.setVisibility(View.GONE);
                initializePlayer(imageUrl);
            }

        }

        // button to visit comments
        mButtonComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSubmissionViewer();
            }
        });
    }


    private void openSubmissionViewer() {
        Intent submissionViewerIntent = new Intent(getContext(), ActivitySubmissionViewer.class);
        submissionViewerIntent.putExtra(Constants.EXTRA_SUBMISSION_OBJ, mCurrSubmission);
        startActivity(submissionViewerIntent);
        closeFullDisplay();
    }


    private void initializePlayer(String url) {

        mExoplayer.requestFocus();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);

        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);

        mExoplayer.setPlayer(player);

        player.addListener(new FragmentFullDisplay.PlayerEventListener());
        player.setPlayWhenReady(true);
/*        MediaSource mediaSource = new HlsMediaSource(Uri.parse("https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"),
                mediaDataSourceFactory, mainHandler, null);*/


        MediaSource mediaSource = new ExtractorMediaSource.Factory(mediaDataSourceFactory)
                .createMediaSource(Uri.parse(url));

        boolean haveStartPosition = currentWindow != C.INDEX_UNSET;
        if (haveStartPosition) {
            player.seekTo(currentWindow, playbackPosition);
        }

        // repeat mode: 0 = off, 1 = loop single video, 2 = loop playlist
        player.setRepeatMode(1);
        player.prepare(mediaSource, !haveStartPosition, false);

    }
    private class PlayerEventListener extends Player.DefaultEventListener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_IDLE:       // The player does not have any media to play yet.
                    //progressBar.setVisibility(View.VISIBLE);
                    break;
                case Player.STATE_BUFFERING:  // The player is buffering (loading the content)
                    //   progressBar.setVisibility(View.VISIBLE);
                    break;
                case Player.STATE_READY:      // The player is able to immediately play
                    // progressBar.setVisibility(View.GONE);
                    break;
                case Player.STATE_ENDED:      // The player has finished playing the media
                    //  progressBar.setVisibility(View.GONE);
                    break;
            }
        }
    }
   private void releaseExoPlayer() {
        if (player != null) {
            player.release();
        }
    }

    @Override
    public void onTaskCompleted(Uri uriToLoad) {
        mVideoView.setVideoURI(uriToLoad);
        mVideoView.start();
    }
}
