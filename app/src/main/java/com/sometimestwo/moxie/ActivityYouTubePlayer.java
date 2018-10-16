package com.sometimestwo.moxie;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.sometimestwo.moxie.Model.SubmissionObj;
import com.sometimestwo.moxie.Utils.Constants;
import com.sometimestwo.moxie.Utils.Utils;

public class ActivityYouTubePlayer extends YouTubeBaseActivity
        implements YouTubePlayer.OnInitializedListener, YouTubePlayer.OnFullscreenListener {

    private YouTubePlayerView mPlayerView;
    private YouTubePlayer mYouTubePlayer;
    private SubmissionObj mCurrSubmission;
    private String mYouTubeID;

    @SuppressLint("InlinedApi")
    private static final int PORTRAIT_ORIENTATION =  ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;

    @SuppressLint("InlinedApi")
    private static final int LANDSCAPE_ORIENTATION =  ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;

    private YouTubePlayer mPlayer = null;
    private boolean mAutoRotation = false;

    public ActivityYouTubePlayer() {
        super();
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        mAutoRotation = Settings.System.getInt(getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0) == 1;

        setContentView(R.layout.layout_youtube_player);
        mPlayerView = (YouTubePlayerView) findViewById(R.id.youtube_player_view);
        mPlayerView.getRootView().setBackgroundColor(getResources().getColor(R.color.colorBgBlackTintDarker));
        if (getIntent().getExtras() != null) {
            mCurrSubmission = (SubmissionObj) getIntent().getExtras().getSerializable(Constants.ARGS_SUBMISSION_OBJ);
        }

        // Get YouTube ID
        setVideoId(Utils.getYouTubeID(mCurrSubmission.getUrl()));
        initialize();
    }

    // stolen from https://stackoverflow.com/questions/21332765/android-youtube-player-has-been-released
    public void setVideoId(final String videoId) {
        if (videoId != null && !videoId.equals(this.mYouTubeID)) {
            this.mYouTubeID = videoId;
            if (mYouTubePlayer != null) {
                try {
                    mYouTubePlayer.loadVideo(videoId);
                } catch (IllegalStateException e) {
                    initialize();
                }
            }
        }
    }

    public void initialize() {
        mPlayerView.initialize(Constants.YOUTUBE_API_KEY, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mYouTubePlayer != null) {
            mYouTubePlayer.release();
        }
        super.onDestroy();
    }

    @Override
    public void onInitializationFailure(Provider arg0,
                                        YouTubeInitializationResult error) {
        // TODO Auto-generated method stub
        Toast.makeText(this, "Failed to load YouTube Video. " + error.toString(), Toast.LENGTH_LONG).show();

    }

    @Override
    public void onInitializationSuccess(Provider arg0, YouTubePlayer player,
                                        boolean arg2) {
        this.mYouTubePlayer = player;
        // TODO Auto-generated method stub
        player.loadVideo(mYouTubeID);
        player.setOnFullscreenListener(this);

        if (mAutoRotation) {
            player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION
                    | YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI
                    | YouTubePlayer.FULLSCREEN_FLAG_ALWAYS_FULLSCREEN_IN_LANDSCAPE
                    | YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        } else {
            player.addFullscreenControlFlag(YouTubePlayer.FULLSCREEN_FLAG_CONTROL_ORIENTATION
                    | YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI
                    | YouTubePlayer.FULLSCREEN_FLAG_CUSTOM_LAYOUT);
        }

        player.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
            @Override
            public void onPlaying() {

            }

            @Override
            public void onPaused() {
            }

            @Override
            public void onStopped() {
            }

            @Override
            public void onBuffering(boolean b) {

            }

            @Override
            public void onSeekTo(int i) {

            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (mPlayer != null)
                mPlayer.setFullscreen(true);
        }
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            if (mPlayer != null)
                mPlayer.setFullscreen(false);
        }
    }

    @Override
    public void onFullscreen(boolean fullsize) {
        if (fullsize) {
            setRequestedOrientation(LANDSCAPE_ORIENTATION);
        } else {
            setRequestedOrientation(PORTRAIT_ORIENTATION);
        }
    }


}


