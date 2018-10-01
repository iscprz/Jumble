package com.sometimestwo.moxie;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
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

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
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
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubeThumbnailLoader;
import com.google.android.youtube.player.YouTubeThumbnailView;
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
    //private FrameLayout mZoomieImageViewContainer;
    private ZoomieView mZoomieImageView;

    /* Video view for VREDDIT videos*/
    private FrameLayout mVideoviewContainer;
    private VideoView mVideoView;

    private ProgressBar mProgressBar;
    private TextView mFailedLoadText;

    /* Exoplayer */
    private FrameLayout mExoplayerContainer;
    private BandwidthMeter bandwidthMeter;
    private DefaultTrackSelector trackSelector;
    private SimpleExoPlayer player;
    private DataSource.Factory mediaDataSourceFactory;
    private int currentWindow;
    private long playbackPosition;
    private Timeline.Window window;
    private PlayerView mExoplayer;

    /* Youtube */
    private RelativeLayout mYoutubeIconsOverlay;
    private ImageView mPlayButton;
    private YouTubeThumbnailView mYouTubeThumbnail;

    public static FragmentFullDisplay newInstance() {
        return new FragmentFullDisplay();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        unpackArgs();

        // Read relevant permission settings
        SharedPreferences prefs = getContext().getSharedPreferences(Constants.KEY_GET_PREFS_SETTINGS, Context.MODE_PRIVATE);

        mPrefsAllowNSFW = prefs.getBoolean(Constants.SETTINGS_HIDE_NSFW, false);
        mAllowCloseOnClick = prefs.getBoolean(Constants.SETTINGS_ALLOW_BIGDISPLAY_CLOSE_CLICK, false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
       // mZoomieImageViewContainer = (FrameLayout) v.findViewById(R.id.full_displayer_big_image_container);
        mZoomieImageView = (ZoomieView) v.findViewById(R.id.full_displayer_image_zoomie_view);

        /* Video view*/
        mVideoviewContainer = (FrameLayout) v.findViewById(R.id.full_displayer_videoview_container);
        mVideoView = (VideoView) v.findViewById(R.id.full_displayer_videoview);

        /* Loading progress bar */
        mProgressBar = (ProgressBar) v.findViewById(R.id.full_displayer_progress);

        /* Failure to load text */
        mFailedLoadText = (TextView) v.findViewById(R.id.full_displayer_failed_load_text);

        /* Exo player*/
        mExoplayerContainer = (FrameLayout) v.findViewById(R.id.full_displayer_exoplayer_container);
        mExoplayer = (PlayerView) v.findViewById(R.id.full_displayer_exoplayer);
        bandwidthMeter = new DefaultBandwidthMeter();
        mediaDataSourceFactory = new DefaultDataSourceFactory(getContext(),
                Util.getUserAgent(getContext(), "Moxie"),
                (TransferListener<? super DataSource>) bandwidthMeter);
        window = new Timeline.Window();

        /* Youtube */
        mYoutubeIconsOverlay = (RelativeLayout) v.findViewById(R.id.full_displayer_youtube_thumbnail_icons_overlay);
        mYoutubeIconsOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent youTubeIntent = new Intent(getContext(),YouTubePlayerClass.class);
                Bundle args = new Bundle();
                args.putSerializable(Constants.ARGS_SUBMISSION_OBJ,mCurrSubmission);
                youTubeIntent.putExtras(args);
                startActivity(youTubeIntent);
            }
        });
       // mPlayButton = (ImageView) v.findViewById(R.id.full_displayer_play_button);
        mYouTubeThumbnail = (YouTubeThumbnailView) v.findViewById(R.id.full_displayer_youtube_thumbnail);

        setupFullViewer();

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
        mSubredditTextView.setText("/r/" + mCurrSubmission.getSubreddit());


        /* Set up image/gif view*/
        // Prioritize using the cleaned URL. Some post URLS point to indirect images:
        // Indirect url example: imgur.com/AktjAWe
        // Cleaned url example: imgur.com/AktjAWe.jpg
        String imageUrl = (mCurrSubmission.getCleanedUrl() != null)
                ? mCurrSubmission.getCleanedUrl() : mCurrSubmission.getUrl();

        if(mCurrSubmission.getSubmissionType() == Constants.SubmissionType.IMAGE){
            focusView(mZoomieImageView,null);
            mProgressBar.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(imageUrl)
                    .listener(new GlideProgressListener(mProgressBar))
                    .into(mZoomieImageView);
            //TODO: handle invalid URL
        }else if (mCurrSubmission.getSubmissionType() == Constants.SubmissionType.GIF
                || mCurrSubmission.getSubmissionType() == Constants.SubmissionType.VIDEO){

            if(mCurrSubmission.getDomain() == Constants.SubmissionDomain.YOUTUBE){
                //youtube has it's own progress bar
                mProgressBar.setVisibility(View.GONE);
                focusView(mYouTubeThumbnail,null);
                mYouTubeThumbnail.setTag(Utils.getYouTubeID(mCurrSubmission.getUrl()));
                mYouTubeThumbnail.initialize(Constants.YOUTUBE_API_KEY, new YouTubeThumbnailView.OnInitializedListener() {
                    @Override
                    public void onInitializationSuccess(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader youTubeThumbnailLoader) {
                        youTubeThumbnailLoader.setVideo(Utils.getYouTubeID(mCurrSubmission.getUrl()));
                        youTubeThumbnailLoader.setOnThumbnailLoadedListener(new YouTubeThumbnailLoader.OnThumbnailLoadedListener() {
                            @Override
                            public void onThumbnailLoaded(YouTubeThumbnailView youTubeThumbnailView, String s) {
                                mYoutubeIconsOverlay.setVisibility(View.VISIBLE);
                                youTubeThumbnailLoader.release();
                            }

                            @Override
                            public void onThumbnailError(YouTubeThumbnailView youTubeThumbnailView, YouTubeThumbnailLoader.ErrorReason errorReason) {
                                Log.e("YOUTUBE_THUMBNAIL", "Could not load Youtube thumbnail for url: " + mCurrSubmission.getUrl());
                                youTubeThumbnailLoader.release();
                            }
                        });
                    }

                    @Override
                    public void onInitializationFailure(YouTubeThumbnailView youTubeThumbnailView, YouTubeInitializationResult youTubeInitializationResult) {

                    }
                });
            }
            // VREDDIT submissions require a video view
            else if(mCurrSubmission.getDomain() == Constants.SubmissionDomain.VREDDIT){
                focusView(mVideoView,mVideoviewContainer);
                String url = mCurrSubmission.getEmbeddedMedia().getRedditVideo().getFallbackUrl();
                try {
                    new Utils.FetchVRedditGifTask(getContext(),url, this).execute();
                } catch (Exception e) {
                   // Log.e("V.REDD.IT FAIL at url: " , url);
                    //LogUtil.e(e, "Error v.redd.it url: " + url);
                }
            }
            // IREDDIT requires Imageview to play GIF
            else if(mCurrSubmission.getDomain() == Constants.SubmissionDomain.IREDDIT){
                focusView(mZoomieImageView,null);

                //TODO Progress bar
                mProgressBar.setVisibility(View.GONE);
                Glide.with(this)
                        .asGif()
                        .load(imageUrl)
                        /*.listener(new GlideProgressListener(mProgressBar))*/
                        .into(mZoomieImageView);
            }
            // Every other domain for GIF
            else{
                //exo player has its own progress bar
                mProgressBar.setVisibility(View.GONE);
                focusView(mExoplayer,mExoplayerContainer);
                initializeExoPlayer(imageUrl);
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

    /* Focuses up to two views. Pass null if only need 1 view focused*/
    private void focusView(View focused1, View focused2){
        mZoomieImageView.setVisibility(focused1 == mZoomieImageView || focused2 == mZoomieImageView ? View.VISIBLE : View.GONE);
        mExoplayer.setVisibility(focused1 == mExoplayer || focused2 == mExoplayer ? View.VISIBLE : View.GONE);
        mExoplayerContainer.setVisibility(focused1 == mExoplayerContainer || focused2 == mExoplayerContainer ? View.VISIBLE : View.GONE);
        mVideoView.setVisibility(focused1 == mVideoView || focused2 == mVideoView ? View.VISIBLE : View.GONE);
        mVideoviewContainer.setVisibility(focused1 == mVideoviewContainer || focused2 == mVideoviewContainer ? View.VISIBLE : View.GONE);
        mYouTubeThumbnail.setVisibility(focused1 == mYouTubeThumbnail || focused2 == mYouTubeThumbnail ? View.VISIBLE : View.GONE);
        mYoutubeIconsOverlay.setVisibility(focused1 == mYoutubeIconsOverlay || focused2 == mYoutubeIconsOverlay ? View.VISIBLE : View.GONE);
        mFailedLoadText.setVisibility(focused1 == mFailedLoadText || focused2 == mFailedLoadText ? View.VISIBLE : View.GONE);
        //mPlayButton.setVisibility( focused1 == mPlayButton || focused2 == mPlayButton ? View.VISIBLE : View.GONE);
    }
    private void openSubmissionViewer() {
        Intent submissionViewerIntent = new Intent(getContext(), ActivitySubmissionViewer.class);
        submissionViewerIntent.putExtra(Constants.EXTRA_SUBMISSION_OBJ, mCurrSubmission);
        startActivity(submissionViewerIntent);
        closeFullDisplay();
    }


    private void initializeExoPlayer(String url) {

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
        public void onPlayerError(ExoPlaybackException error) {
            focusView(mFailedLoadText,null);
            super.onPlayerError(error);
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_IDLE:       // The player does not have any media to play yet.
                    //mProgressBar.setVisibility(View.VISIBLE);
                    break;
                case Player.STATE_BUFFERING:  // The player is buffering (loading the content)
                    //mProgressBar.setVisibility(View.VISIBLE);
                    break;
                case Player.STATE_READY:      // The player is able to immediately play
                    mProgressBar.setVisibility(View.GONE);
                    break;
                case Player.STATE_ENDED:      // The player has finished playing the media
                    mProgressBar.setVisibility(View.GONE);
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
    public void onVRedditMuxTaskCompleted(Uri uriToLoad) {
        mVideoView.setVideoURI(uriToLoad);
        // loop
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // TODO: This setVisibility might be causing breaking issues.
                mProgressBar.setVisibility(View.GONE);
                mp.start();
                mp.setLooping(true);
            }
        });
    }
}
