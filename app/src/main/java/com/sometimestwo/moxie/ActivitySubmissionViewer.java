package com.sometimestwo.moxie;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
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

public class ActivitySubmissionViewer extends AppCompatActivity {
    private RequestManager GlideApp;
    private SubmissionObj mCurrSubmission;
    private Toolbar mToolbar;
    private TextView mSubmissionTitle;
    private ImageView mImageView;
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


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.drawer_layout);
        setContentView(R.layout.submission_viewer);
        GlideApp = Glide.with(this);

        /* Title(s)*/
        mSubmissionTitle = (TextView) findViewById(R.id.submission_viewer_title);

        /* The actual view in which the image is displayed*/
        mImageView = (ImageView) findViewById(R.id.submission_media_view);

        /* Exo player*/
        mExoplayer = (PlayerView) findViewById(R.id.submission_viewer_exoplayer);
        bandwidthMeter = new DefaultBandwidthMeter();
        mediaDataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "Moxie"), (TransferListener<? super DataSource>) bandwidthMeter);
        window = new Timeline.Window();

        /* Loading progress bar */
        mProgressBar = (ProgressBar) findViewById(R.id.submission_viewer_media_progress);

        /* Post info : Vote count, comment count, author */
        mSubmissionInfo = (LinearLayout) findViewById(R.id.submission_viewer_submission_info);
        mTextViewAuthor = (TextView) findViewById(R.id.submission_viewer_author);
        mTextViewVoteCount = (TextView) findViewById(R.id.submission_viewer_vote_count);
        mCommentCount = (TextView) findViewById(R.id.submission_viewer_comment_count);

        /* Upvote/downvote/save/overflow*/
        mVotebar = (LinearLayout) findViewById(R.id.submission_viewer_vote_bar);
        mButtonUpvote = (ImageView) findViewById(R.id.submission_viewer_commit_upvote);
        mButtonDownvote = (ImageView) findViewById(R.id.submission_viewer_commit_downvote);
        mButtonSave = (ImageView) findViewById(R.id.submission_viewer_commit_save);
        mButtonOverflow = (ImageView) findViewById(R.id.submission_viewer_votebar_overflow);

        unpackExtras();
        setupToolbar();
        setupMedia();
        setupSubmissionInfoBar();
        setupVotebar();

        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSimpleMediaDisplay();
            }
            // closeMediaPlayer();
        });
    }


    private void unpackExtras() {
        mCurrSubmission = (SubmissionObj) getIntent().getExtras().get(Constants.EXTRA_SUBMISSION_OBJ);
    }

    private void setupToolbar(){
        mToolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(mToolbar);

        ActionBar toolbar = getSupportActionBar();
        toolbar.show();
        toolbar.setDisplayHomeAsUpEnabled(true);
        toolbar.setHomeAsUpIndicator(R.drawable.ic_white_back_arrow);
        toolbar.setTitle("/r/" + mCurrSubmission.getSubreddit());
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

    private void openSimpleMediaDisplay(){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment simpleViewerFragment = FragmentSimpleImageDisplay.newInstance();

        Bundle args = new Bundle();
        args.putSerializable(Constants.ARGS_SUBMISSION_OBJ, mCurrSubmission);
        simpleViewerFragment.setArguments(args);


       // int parentContainerId = ((ViewGroup) getView().getParent()).getId();
        ft.add(R.id.submission_viewer_simple_display_container,
                simpleViewerFragment,
                Constants.TAG_FRAG_SIMPLE_DISPLAYER);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void initializePlayer(String url) {

        mExoplayer.requestFocus();

        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);

        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        mExoplayer.setPlayer(player);

        player.addListener(new ActivitySubmissionViewer.PlayerEventListener());
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


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_submission_view_header,menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch(id){
            case android.R.id.home:
                FragmentSimpleImageDisplay simpleImageDisplayFragment
                        = (FragmentSimpleImageDisplay) getSupportFragmentManager()
                        .findFragmentByTag(Constants.TAG_FRAG_SIMPLE_DISPLAYER);
                if(simpleImageDisplayFragment != null && simpleImageDisplayFragment.isVisible()){
                    getSupportFragmentManager().popBackStack();
                }
                else{
                    finish();
                }
                return true;
            case R.id.menu_submission_view_comments_sortby:
                return true;
        }


        return super.onOptionsItemSelected(item);
    }
}