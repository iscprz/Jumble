package com.sometimestwo.moxie;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
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

public class FragmentSimpleImageDisplay extends Fragment implements OnTaskCompletedListener {

    private SubmissionObj mCurrSubmission;
    private boolean mPrefsAllowNSFW;
    private boolean mAllowCloseOnClick;


    private ImageView mButtonShare;
    private ImageView mButtonCopyURL;
    private ImageView mButtonDownload;

    // will hold any image/gif/video to handle click-to-close
    private FrameLayout mMediaContainer;

    /* Big zoomie view*/
    private ZoomieView mZoomieImageView;

    /* Exo player*/
    private FrameLayout mExoplayerContainerLarge;
    private PlayerView mSimpleDisplayExoplayer;
    private BandwidthMeter bandwidthMeter;
    private DefaultTrackSelector trackSelector;
    private SimpleExoPlayer player;
    private ProgressBar progressBar;
    private DataSource.Factory mediaDataSourceFactory;
    private int currentWindow;
    private long playbackPosition;
    private Timeline.Window window;

    OnCloseClickEventListener closeClickEventListener;
    /* Video view for VREDDIT videos*/
    private VideoView mSimpleDisplayVideoView;

    public static FragmentSimpleImageDisplay newInstance() {
        return new FragmentSimpleImageDisplay();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unpackArgs();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_simple_display, container, false);

        /* Contains any image/gif/video - this is to handle click-to-close events*/
        mMediaContainer = (FrameLayout) v.findViewById(R.id.simple_display_media_container);

        /* Exo player*/
        mSimpleDisplayExoplayer = (PlayerView) v.findViewById(R.id.simple_display_exoplayer);
        bandwidthMeter = new DefaultBandwidthMeter();
        mediaDataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "Moxie"), (TransferListener<? super DataSource>) bandwidthMeter);
        window = new Timeline.Window();

        /* Video view for VREDDIT */
        mSimpleDisplayVideoView = (VideoView) v.findViewById(R.id.simple_display_video_view);
        mSimpleDisplayVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        /* Main zoomie image view*/
        mZoomieImageView = (ZoomieView) v.findViewById(R.id.simple_display_image_viewer);
        /* Bottom buttons */
        mButtonCopyURL = (ImageView) v.findViewById(R.id.simple_display_button_copy_url);
        mButtonShare = (ImageView) v.findViewById(R.id.simple_display_button_share);
        mButtonDownload = (ImageView) v.findViewById(R.id.simple_display_button_download);


        setupMedia();

        /* Bottom toolbar actions */
        mButtonCopyURL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int sdk = android.os.Build.VERSION.SDK_INT;
                if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(mCurrSubmission.getUrl());
                } else {
                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip = android.content.ClipData.newPlainText(mCurrSubmission.getTitle(), mCurrSubmission.getUrl());
                    clipboard.setPrimaryClip(clip);
                }

            }
        });

        /* Exit on tap if settings option is enabled*/
        mMediaContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                closeClickEventListener.onCloseClickDetected();
            }
        });

        return v;
    }

    @Override
    public void onAttach(Context context) {
        try {
            closeClickEventListener = (OnCloseClickEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement listener inferfaces!");
        }
        super.onAttach(context);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
        startExoPlayer();
    }

    @Override
    public void onDestroy() {
        mSimpleDisplayVideoView.stopPlayback();
        releaseExoPlayer();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (player != null) {
            player.stop();
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();
    }

    @Override
    public void onPause() {
        super.onPause();
        pauseExoPlayer();
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
    private void closeSimpleImageViewer() {
        //releaseExoPlayer();
        try {
            getActivity().getSupportFragmentManager().popBackStack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setupMedia() {
        String imageUrl = (mCurrSubmission.getCleanedUrl() != null)
                ? mCurrSubmission.getCleanedUrl() : mCurrSubmission.getUrl();

        if (mCurrSubmission.getSubmissionType() == Constants.SubmissionType.IMAGE) {
            mZoomieImageView.setVisibility(View.VISIBLE);
            mSimpleDisplayExoplayer.setVisibility(View.GONE);
            mSimpleDisplayVideoView.setVisibility(View.GONE);

            Glide.with(this)
                    .load(Uri.parse(imageUrl))
                    .into(mZoomieImageView);
        } else {
            mZoomieImageView.setVisibility(View.GONE);
            // VREDDIT needs VideoView
            if (mCurrSubmission.getDomain() == Constants.SubmissionDomain.VREDDIT) {
                mSimpleDisplayVideoView.setVisibility(View.VISIBLE);
                mSimpleDisplayExoplayer.setVisibility(View.GONE);

                String url = mCurrSubmission.getEmbeddedMedia().getRedditVideo().getFallbackUrl();
                try {
                    new Utils.FetchVRedditGifTask(getContext(), url, this).execute();
                } catch (Exception e) {
                }
            }
            // Non VREDDIT GIF/VIDEO
            else {
                mSimpleDisplayExoplayer.setVisibility(View.VISIBLE);
                mSimpleDisplayVideoView.setVisibility(View.GONE);
                mZoomieImageView.setVisibility(View.GONE);
                initializePlayer(imageUrl);
            }

        }

    }

    private void releaseExoPlayer() {
        if (player != null) {
            player.release();
        }
    }

    private void pauseExoPlayer() {
        if (player != null) {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
    }

    private void startExoPlayer() {
        if (player != null) {
            player.setPlayWhenReady(true);
            player.getPlaybackState();
        }
    }

    private void initializePlayer(String url) {

        mSimpleDisplayExoplayer.requestFocus();

        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);

        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);

        mSimpleDisplayExoplayer.setPlayer(player);

        player.addListener(new FragmentSimpleImageDisplay.PlayerEventListener());
        player.setPlayWhenReady(true);
       /* MediaSource mediaSource = new HlsMediaSource(Uri.parse("https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"),
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

    @Override
    public void onVRedditMuxTaskCompleted(Uri uriToLoad) {
        mSimpleDisplayVideoView.setVideoURI(uriToLoad);
        mSimpleDisplayVideoView.start();
    }
}


