package com.sometimestwo.moxie;

import android.os.Bundle;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.sometimestwo.moxie.Model.SubmissionObj;
import com.sometimestwo.moxie.Utils.Constants;
import com.sometimestwo.moxie.Utils.Utils;

public class YouTubePlayerClass extends YouTubeBaseActivity implements YouTubePlayer.OnInitializedListener {

    private YouTubePlayerView mPlayerView;
    private YouTubePlayer mYouTubePlayer;
    private SubmissionObj mCurrSubmission;
    private String mYouTubeID;

    public YouTubePlayerClass() {
        super();
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
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


}


