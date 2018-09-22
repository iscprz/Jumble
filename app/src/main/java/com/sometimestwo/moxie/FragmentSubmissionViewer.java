package com.sometimestwo.moxie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
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

public class FragmentSubmissionViewer extends Fragment {
    private static final String TAG = Constants.TAG_FRAG_MEDIA_DISPLAY;

    private RequestManager GlideApp;
    private SubmissionObj mCurrSubmission;
    private BigImageView mBigImageView;
    private TextView mSubmissionTitle;
    private ImageView mImageView;
    private LinearLayout mCommentsContainer;
    private View mClicker;
    private GestureDetector mDetector;
    Toolbar mToolbar;
    ProgressBar mProgressBar;

    // Submission info
    LinearLayout mSubmissionInfo;
    TextView mTextViewAuthor;
    TextView mTextViewVoteCount;
    TextView mCommentCount;

    // Vote bar
    LinearLayout mVotebar;
    ImageView mButtonUpvote;
    ImageView mButtonDownvote;
    ImageView mButtonSave;
    ImageView mButtonOverflow;

    // Exo player stuff
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


    private SubmissionDisplayerEventListener mMediaDisplayerEventListener;

    public interface SubmissionDisplayerEventListener { }

    public static FragmentSubmissionViewer newInstance() {
        return new FragmentSubmissionViewer();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlideApp = Glide.with(this);

        unpackArgs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.submission_viewer, container, false);

        /* Toolbar setup*/
        mToolbar = (Toolbar) v.findViewById(R.id.submission_viewer_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        mSubmissionTitle = (TextView) v.findViewById(R.id.submission_viewer_title);

        /* The actual view in which the image is displayed*/
        mImageView = (ImageView) v.findViewById(R.id.submission_media_view);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openBigMediaDisplay();
            }
               // closeMediaPlayer();
        });
        /*mBigImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e(TAG, "Clicked media viewer(not the background)! ");
                mMediaDisplayerEventListener.closeMediaDisplayer();
            }
        });*/

        /* Exo player*/
        mExoplayer = (PlayerView) v.findViewById(R.id.submission_viewer_exoplayer);
        bandwidthMeter = new DefaultBandwidthMeter();
        mediaDataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "Moxie"), (TransferListener<? super DataSource>) bandwidthMeter);
        window = new Timeline.Window();

        /* Loading progress bar */
        mProgressBar = (ProgressBar) v.findViewById(R.id.submission_viewer_media_progress);

        /* Post info : Vote count, comment count, author */
        mSubmissionInfo = (LinearLayout) v.findViewById(R.id.submission_viewer_submission_info);
        mTextViewAuthor = (TextView) v.findViewById(R.id.submission_viewer_author);
        mTextViewVoteCount = (TextView) v.findViewById(R.id.submission_viewer_vote_count);
        mCommentCount = (TextView) v.findViewById(R.id.submission_viewer_comment_count);

        /* Upvote/downvote/save/overflow*/
        mVotebar = (LinearLayout) v.findViewById(R.id.submission_viewer_vote_bar);
        mButtonUpvote = (ImageView) v.findViewById(R.id.submission_viewer_commit_upvote);
        mButtonDownvote = (ImageView) v.findViewById(R.id.submission_viewer_commit_downvote);
        mButtonSave = (ImageView) v.findViewById(R.id.submission_viewer_commit_save);
        mButtonOverflow = (ImageView) v.findViewById(R.id.submission_viewer_votebar_overflow);

        /* Comments */
        mCommentsContainer = (LinearLayout) v.findViewById(R.id.media_viewer_comments_container);

        setupMedia();
        setupSubmissionInfoBar();
        setupVotebar();
      /*  mToolbar = (Toolbar) v.findViewById(R.id.toolbar_top);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);*/
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mMediaDisplayerEventListener = (SubmissionDisplayerEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement listener inferfaces!");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setupToolbar();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releaseExoPlayer();
    }

    @Override
    public void onDestroy() {
        // Notify calling fragment that we're closing the submission viewer
        // Don't need to pass anything back. Pass an empty intent for now
        Intent resultIntent = new Intent();
        getTargetFragment().onActivityResult(FragmentHome.KEY_INTENT_GOTO_SUBMISSIONVIEWER, Activity.RESULT_OK, resultIntent);
        releaseExoPlayer();
        super.onDestroy();
    }

    private void unpackArgs(){
        try{
            // Submission to be viewed
            mCurrSubmission = (SubmissionObj) this.getArguments().get(Constants.EXTRA_SUBMISSION_OBJ);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setupToolbar() {
        //toolbar setup
        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null) {
            //toolbar.setTitle(getResources().getString(R.string.toolbar_title_albums));
            toolbar.setDisplayHomeAsUpEnabled(true);
            toolbar.setHomeAsUpIndicator(R.drawable.ic_white_back_arrow);
            toolbar.setTitle("/r/" + mCurrSubmission.getSubreddit());
        }
        mToolbar.setAlpha(1);
    }



    private void goBack() {
        releaseExoPlayer();
        try {
            FragmentManager fm = getActivity().getSupportFragmentManager();

            fm.popBackStack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releaseExoPlayer() {
        if (player != null) {
            player.release();
        }
    }


    private void setupMedia() {
        mSubmissionTitle.setText(mCurrSubmission.getTitle());

        // Prioritize using the cleaned URL. Some post URLS point to indirect images:
        // Indirect url example: imgur.com/AktjAWe
        // Cleaned url example: imgur.com/AktjAWe.jpg
        String imageUrl = (mCurrSubmission.getCleanedUrl() != null)
                ? mCurrSubmission.getCleanedUrl() : mCurrSubmission.getUrl();

        if(mCurrSubmission.getSubmissionType() == Constants.SubmissionType.IMAGE){
            mImageView.setVisibility(View.VISIBLE);
            mExoplayer.setVisibility(View.GONE);
            //TODO: handle invalid URL
            Glide.with(this)
                    .load(imageUrl)
                    .listener(new ProgressBarRequestListener(mProgressBar))
                    //* .apply(options)*//*
                    .into(mImageView);
        }else if (mCurrSubmission.getSubmissionType() == Constants.SubmissionType.GIF
                || mCurrSubmission.getSubmissionType() == Constants.SubmissionType.VIDEO){
            mImageView.setVisibility(View.GONE);
            mExoplayer.setVisibility(View.VISIBLE);
            initializePlayer(imageUrl);
        }
    }

    private void setupSubmissionInfoBar(){
        mTextViewAuthor.setText(mCurrSubmission.getAuthor());
        mTextViewVoteCount.setText(String.valueOf(mCurrSubmission.getScore()));
        mCommentCount.setText(String.valueOf(mCurrSubmission.getCommentCount()));
    }

    private void setupVotebar(){
        // change icon color depending on of user has voted on this already (only for logged in users)
    }


    /* Exoplayer */
    private void initializePlayer(String url) {

        mExoplayer.requestFocus();

        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);

        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);

        mExoplayer.setPlayer(player);

        player.addListener(new PlayerEventListener());
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

     /*   ivHideControllerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerView.hideController();
            }
        });*/
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

    public void openBigMediaDisplay(){
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

        Fragment bigDisplayFragment = FragmentBigDisplay.newInstance();
        Bundle args = new Bundle();
        args.putSerializable(Constants.ARGS_SUBMISSION_OBJ, mCurrSubmission);
        bigDisplayFragment.setArguments(args);

        int parentContainerId = ((ViewGroup) getView().getParent()).getId();
        ft.add(parentContainerId, bigDisplayFragment/*, Constants.TAG*/);
        ft.addToBackStack(null);
        ft.commit();
    }
}
