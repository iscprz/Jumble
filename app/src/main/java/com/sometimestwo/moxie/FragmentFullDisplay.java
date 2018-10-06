package com.sometimestwo.moxie;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
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
import com.sometimestwo.moxie.Model.CommentItem;
import com.sometimestwo.moxie.Model.CommentObj;
import com.sometimestwo.moxie.Model.MoreChildItem;
import com.sometimestwo.moxie.Model.SubmissionObj;
import com.sometimestwo.moxie.Utils.Constants;
import com.sometimestwo.moxie.Utils.DownloadBinder;
import com.sometimestwo.moxie.Utils.DownloadService;
import com.sometimestwo.moxie.Utils.Utils;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.VoteDirection;
import net.dean.jraw.tree.CommentNode;
import net.dean.jraw.tree.RootCommentNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.BIND_AUTO_CREATE;

public class FragmentFullDisplay extends Fragment implements OnVRedditTaskCompletedListener {
    public static String TAG = FragmentFullDisplay.class.getSimpleName();

    private SubmissionObj mCurrSubmission;
    private boolean mIsUserless;
    private boolean mPrefsAllowNSFW;
    private boolean mAllowCloseOnClick;
    private BroadcastReceiver mDLCompleteReceiver;
    private OnCommentsEventListener mCommentsEventListener;

    /* Titles */
    TextView mTitleTextView;
    TextView mAuthorTextView;
    TextView mSubredditTextView;
    LinearLayout mHeaderContainer;

    /* Snackbar */
    private ImageView mButtonComments;
    private TextView mCommentCountTextView;
    private ImageView mButtonUpvote;
    private TextView mUpvoteCountTextView;
    private ImageView mButtonDownvote;
    private ImageView mButtonSave;
    private ImageView mButtonOverflow;
    private LinearLayout mSnackbarContainer;
    private VoteDirection mVoteDirection;
    private boolean mIsSaved;
    private int mUpvoteCount;

    // Comments
    boolean mCommentsOpen = false;
    boolean mCommentsInitialized = false;
    LinearLayout mCommentsContainer;
    ProgressBar mCommentsProgressBar;
    ImageView mCommentsButtonClose;
    RelativeLayout mCommentsDummyTopView;
    FrameLayout mCommentsScrollViewContainer;
    RecyclerView mCommentsRecyclerView;
    LinearLayoutManager mCommentsRecyclerLayoutManager;
    public ArrayList<CommentObj> comments;
    public HashMap<String, String> commentOPs = new HashMap<>();

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

    /* Download utils*/
    private DownloadBinder downloadBinder = null;

    public interface OnCommentsEventListener {
        public void isCommentsOpen(boolean commentsOpen);
    }

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
        View v = inflater.inflate(R.layout.fragment_full_displayer, container, false);

        mIsUserless = App.getAccountHelper().getReddit().getAuthMethod().isUserless();

        /* Header */
        mHeaderContainer = (LinearLayout) v.findViewById(R.id.big_display_title_container);
        mTitleTextView = (TextView) v.findViewById(R.id.big_display_title);
        mAuthorTextView = (TextView) v.findViewById(R.id.full_display_author_text);
        mSubredditTextView = (TextView) v.findViewById(R.id.big_display_subreddit);

        /* Snackbar */
        mButtonComments = (ImageView) v.findViewById(R.id.full_display_snack_bar_comments);
        mCommentCountTextView = (TextView) v.findViewById(R.id.full_display_comments_counter);
        mButtonUpvote = (ImageView) v.findViewById(R.id.full_display_snack_bar_upvote);
        mUpvoteCountTextView = (TextView) v.findViewById(R.id.full_display_upvote_counter);
        mButtonDownvote = (ImageView) v.findViewById(R.id.full_display_snack_bar_downvote);
        mButtonSave = (ImageView) v.findViewById(R.id.full_display_snack_bar_save);
        mButtonOverflow = (ImageView) v.findViewById(R.id.full_display_snack_bar_overflow);
        mSnackbarContainer = (LinearLayout) v.findViewById(R.id.full_display_snack_bar_container);

        /* Comments */
        mCommentsContainer = (LinearLayout) v.findViewById(R.id.full_displayer_comments_container);
        mCommentsDummyTopView = (RelativeLayout) v.findViewById(R.id.full_displayer_comments_dummy_top);
        mCommentsScrollViewContainer = (FrameLayout) v.findViewById(R.id.full_displayer_comments_scrollview_container);
        mCommentsRecyclerView = (RecyclerView) v.findViewById(R.id.full_displayer_comments_recycler);
        mCommentsProgressBar = (ProgressBar) v.findViewById(R.id.full_displayer_comments_progress_bar);
        mCommentsButtonClose = (ImageView) v.findViewById(R.id.comments_button_close);

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
                if (!mCommentsOpen) {
                    Intent youTubeIntent = new Intent(getContext(), YouTubePlayerClass.class);
                    Bundle args = new Bundle();
                    args.putSerializable(Constants.ARGS_SUBMISSION_OBJ, mCurrSubmission);
                    youTubeIntent.putExtras(args);
                    startActivity(youTubeIntent);
                } else {
                    closeComments();
                }
            }
        });
        // mPlayButton = (ImageView) v.findViewById(R.id.full_displayer_play_button);
        mYouTubeThumbnail = (YouTubeThumbnailView) v.findViewById(R.id.full_displayer_youtube_thumbnail);

        setupHeader();
        setupMedia();
        setupSnackBar();
        startAndBindDownloadService();

        mExoplayerContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mCommentsOpen) {
                    if (mAllowCloseOnClick) {
                        closeFullDisplay();
                    }
                } else {
                    closeComments();
                }
            }
        });
        mVideoviewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mCommentsOpen) {
                    if (mAllowCloseOnClick) {
                        closeFullDisplay();
                    }
                } else {
                    closeComments();
                }
            }
        });

        // Each time we enter the full display we need to make sure
        // our hosting activity knows comments section isnt opened yet.
        // This is for handling back button press when comments are open
        mCommentsEventListener.isCommentsOpen(false);

        // Button that minimizes comments section
        mCommentsButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //redundant check
                if (mCommentsOpen) {
                    closeComments();
                }
            }
        });
        mCommentsDummyTopView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //redundant check
                if (mCommentsOpen) {
                    closeComments();
                }
            }
        });

        // detects swipes when our comment section is active
        final CommentsGestureDetector gestureListener = new CommentsGestureDetector();
        final GestureDetector commentsGestureDetector
                = new GestureDetector(getActivity(), gestureListener);

        mCommentsDummyTopView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return commentsGestureDetector.onTouchEvent(motionEvent);
            }
        });

        mSnackbarContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return commentsGestureDetector.onTouchEvent(motionEvent);
            }
        });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCommentsEventListener = (OnCommentsEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement listener inferfaces!");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Notify calling fragment that we're closing the full display viewer
        // Don't need to pass anything back. Pass an empty intent for now
        Intent resultIntent = new Intent();
        getTargetFragment().onActivityResult(
                Constants.REQUESTCODE_GOTO_BIG_DISPLAY,
                Activity.RESULT_OK,
                resultIntent);
        releaseExoPlayer();

        // update any changes we made to submission
        mCurrSubmission.setSaved(mIsSaved);
        mCurrSubmission.setVote(mVoteDirection);
        mCurrSubmission.setScore(mUpvoteCount);

        if (mDLCompleteReceiver != null) {
            getContext().unregisterReceiver(mDLCompleteReceiver);
        }

    }

    private void unpackArgs() {
        try {
            // Submission to be viewed
            mCurrSubmission = (SubmissionObj) this.getArguments().get(Constants.ARGS_SUBMISSION_OBJ);
        } catch (Exception e) {
            Log.e("FRAG_FULL_DISPLAY", "Error unpacking args: ");
            e.printStackTrace();
        }
    }

    private void setupHeader() {
        // Title
        mTitleTextView.setText(mCurrSubmission.getCompactTitle() != null
                ? mCurrSubmission.getCompactTitle() : mCurrSubmission.getTitle());
        // Author
        mAuthorTextView.setText(mCurrSubmission.getAuthor());
        // Subreddit
        mSubredditTextView.setText("/r/" + mCurrSubmission.getSubreddit());

        // Container
        mHeaderContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mCommentsOpen) {
                    mHeaderContainer.setAlpha(mHeaderContainer.getAlpha() == 0f ? 1f : 0f);
                } else {
                    closeComments();
                }
            }
        });
    }

    private void setupMedia() {
        /* Set up image/gif view*/
        // Prioritize using the cleaned URL. Some post URLS point to indirect images:
        // Indirect url example: imgur.com/AktjAWe
        // Cleaned url example: imgur.com/AktjAWe.jpg
        String imageUrl = (mCurrSubmission.getCleanedUrl() != null)
                ? mCurrSubmission.getCleanedUrl() : mCurrSubmission.getUrl();

        if (mCurrSubmission.getSubmissionType() == Constants.SubmissionType.IMAGE) {
            // imgur links need to be checked for indirect links such as imgur.com/AktjAWe
            if (mCurrSubmission.getDomain() == Constants.SubmissionDomain.IMGUR
                    && !Arrays.asList(Constants.VALID_IMAGE_EXTENSION)
                    .contains(Utils.getFileExtensionFromUrl(mCurrSubmission.getUrl()))) {
                mProgressBar.setVisibility(View.VISIBLE);
                // fixes indirect imgur url and uses Glide to load image on success
                Utils.fixIndirectImgurUrl(mCurrSubmission, Utils.getImgurHash(mCurrSubmission.getUrl()),
                        new OnTaskCompletedListener() {
                            @Override
                            public void downloadSuccess() {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        focusView(mZoomieImageView, null);
                                        mProgressBar.setVisibility(View.GONE);
                                        Glide.with(FragmentFullDisplay.this)
                                                .load(mCurrSubmission.getCleanedUrl())
                                                /*.listener(new GlideProgressListener(mProgressBar))*/
                                                .into(mZoomieImageView);
                                    }
                                });
                            }

                            @Override
                            public void downloadFailure() {
                                super.downloadFailure();
                            }
                        });
            } else {
                focusView(mZoomieImageView, null);
                mProgressBar.setVisibility(View.GONE);
                Glide.with(FragmentFullDisplay.this)
                        .load(imageUrl)
                        .listener(new GlideProgressListener(mProgressBar))
                        .into(mZoomieImageView);
            }

            //TODO: handle invalid URL
        } else if (mCurrSubmission.getSubmissionType() == Constants.SubmissionType.GIF
                || mCurrSubmission.getSubmissionType() == Constants.SubmissionType.VIDEO) {
            mProgressBar.setVisibility(View.VISIBLE);

            // gif might have a .gifv (imgur) extension
            // ...need to fetch corresponding .mp4
            if (mCurrSubmission.getDomain() == Constants.SubmissionDomain.IMGUR
                    && Utils.getFileExtensionFromUrl(mCurrSubmission.getUrl())
                    .equalsIgnoreCase("gifv")) {
                Utils.getMp4LinkImgur(mCurrSubmission,
                        Utils.getImgurHash(mCurrSubmission.getUrl()),
                        new OnTaskCompletedListener() {
                            @Override
                            public void downloadSuccess() {
                                super.downloadSuccess();
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //exo player has its own progress bar
                                        mProgressBar.setVisibility(View.GONE);
                                        focusView(mExoplayer, mExoplayerContainer);
                                        initializeExoPlayer(mCurrSubmission.getCleanedUrl());
                                    }
                                });
                            }

                            @Override
                            public void downloadFailure() {
                                super.downloadFailure();
                            }
                        });
            } else if (mCurrSubmission.getDomain() == Constants.SubmissionDomain.YOUTUBE) {
                //youtube has it's own progress bar
                mProgressBar.setVisibility(View.GONE);
                focusView(mYouTubeThumbnail, null);
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
            else if (mCurrSubmission.getDomain() == Constants.SubmissionDomain.VREDDIT) {
                focusView(mVideoView, mVideoviewContainer);
                String url = mCurrSubmission.getEmbeddedMedia().getRedditVideo().getFallbackUrl();
                try {
                    new Utils.FetchVRedditGifTask(getContext(), url, this).execute();
                } catch (Exception e) {
                    // Log.e("V.REDD.IT FAIL at url: " , url);
                    //LogUtil.e(e, "Error v.redd.it url: " + url);
                }
            }
            // IREDDIT requires Imageview to play GIF (not gifv)file.
            // Also play anything else that may be a .gif file here
            else if (mCurrSubmission.getDomain() == Constants.SubmissionDomain.IREDDIT
                    || Utils.getFileExtensionFromUrl(imageUrl).equalsIgnoreCase("gif")) {
                focusView(mZoomieImageView, null);

                //TODO Progress bar
                mProgressBar.setVisibility(View.GONE);
                Glide.with(this)
                        .asGif()
                        .load(imageUrl)
                        /*.listener(new GlideProgressListener(mProgressBar))*/
                        .into(mZoomieImageView);
            }
            // Every other domain for GIF
            else {
                //exo player has its own progress bar
                mProgressBar.setVisibility(View.GONE);
                focusView(mExoplayer, mExoplayerContainer);
                initializeExoPlayer(imageUrl);
            }

        }
        // Submission is of unknown type (i.e. /r/todayilearned submissions)
        else {
            focusView(mZoomieImageView, null);
            mProgressBar.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(Constants.URI_404)
                    .listener(new GlideProgressListener(mProgressBar))
                    .into(mZoomieImageView);
        }
    }

    private void setupSnackBar() {
        mVoteDirection = mCurrSubmission.getVote();
        mIsSaved = mCurrSubmission.isSaved();
        mUpvoteCount = mCurrSubmission.getScore();
        Drawable yellowStar = getResources().getDrawable(R.drawable.ic_yellow_star_filled);
        Drawable whiteStar = getResources().getDrawable(R.drawable.ic_white_star_unfilled);
        Drawable upVoteWhite = getResources().getDrawable(R.drawable.ic_white_upvote_medium);
        Drawable downVoteWhite = getResources().getDrawable(R.drawable.ic_white_downvote_medium);
        Drawable upVoteOrange = getResources().getDrawable(R.drawable.ic_upvote_orange);
        Drawable downVoteBlue = getResources().getDrawable(R.drawable.ic_downvote_blue);
        int colorUpvoteOrange = getResources().getColor(R.color.upvote_orange);
        int colorWhite = getResources().getColor(R.color.colorWhite);

        mCommentCountTextView.setText(mCurrSubmission.getCommentCount().toString());
        mUpvoteCountTextView.setTextColor(mCurrSubmission.getVote() == VoteDirection.UP ? colorUpvoteOrange : colorWhite);
        mUpvoteCountTextView.setText(String.valueOf(mCurrSubmission.getScore()));
        mButtonUpvote.setBackground(mVoteDirection == VoteDirection.UP ? upVoteOrange : upVoteWhite);
        mButtonDownvote.setBackground(mVoteDirection == VoteDirection.DOWN ? downVoteBlue : downVoteWhite);
        mButtonSave.setBackground(mIsSaved ? yellowStar : whiteStar);

        // button to visit comments
        mButtonComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openComments();
            }
        });
        mCommentCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mButtonComments.callOnClick();
            }
        });

        mButtonUpvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsUserless) {
                    Toast.makeText(getContext(),
                            getResources().getString(R.string.toast_login_required),
                            Toast.LENGTH_SHORT).show();
                } else {
                    mVoteDirection = (mVoteDirection != VoteDirection.UP) ? VoteDirection.UP : VoteDirection.NONE;
                    mButtonUpvote.setBackground(mVoteDirection == VoteDirection.UP ? upVoteOrange : upVoteWhite);
                    mButtonDownvote.setBackground(mVoteDirection == VoteDirection.DOWN ? downVoteBlue : downVoteWhite);

                    mUpvoteCountTextView.setTextColor(mVoteDirection == VoteDirection.UP ? colorUpvoteOrange : colorWhite);
                    mUpvoteCountTextView.setText(mVoteDirection == VoteDirection.UP ? String.valueOf(++mUpvoteCount) : String.valueOf(--mUpvoteCount));
                    new Utils.VoteSubmissionTask(mCurrSubmission, FragmentFullDisplay.this, mVoteDirection).execute();
                }
            }
        });
        mUpvoteCountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mButtonUpvote.callOnClick();
            }
        });

        mButtonDownvote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsUserless) {
                    Toast.makeText(getContext(),
                            getResources().getString(R.string.toast_login_required),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // vote direction was up, change the text from orange to white
                    if (mVoteDirection == VoteDirection.UP) {
                        mUpvoteCountTextView.setTextColor(colorWhite);
                    }
                    mUpvoteCountTextView.setText(mVoteDirection == VoteDirection.UP
                            ? String.valueOf(--mUpvoteCount) : String.valueOf(mUpvoteCount));
                    mVoteDirection = (mVoteDirection != VoteDirection.DOWN) ? VoteDirection.DOWN : VoteDirection.NONE;
                    mButtonDownvote.setBackground(mVoteDirection == VoteDirection.DOWN ? downVoteBlue : downVoteWhite);
                    mButtonUpvote.setBackground(mVoteDirection == VoteDirection.UP ? upVoteOrange : upVoteWhite);
                    new Utils.VoteSubmissionTask(mCurrSubmission, FragmentFullDisplay.this, mVoteDirection).execute();
                }
            }
        });
        // Save button
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsUserless) {
                    Toast.makeText(getContext(),
                            getResources().getString(R.string.toast_login_required),
                            Toast.LENGTH_SHORT).show();
                } else {
                    mIsSaved = !mIsSaved;
                    mButtonSave.setBackground(mIsSaved ? yellowStar : whiteStar);
                    new Utils.SaveSubmissionTask(mCurrSubmission, FragmentFullDisplay.this).execute();
                }
            }
        });

        // Overflow button
        mButtonOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context wrapper = new ContextThemeWrapper(getContext(), R.style.PopupTheme);
                PopupMenu overflowPopup = new PopupMenu(wrapper, view);
                Menu m = overflowPopup.getMenu();
                MenuInflater inflater = overflowPopup.getMenuInflater();
                inflater.inflate(R.menu.menu_full_display_overflow, overflowPopup.getMenu());
                m.findItem(R.id.menu_full_display_overflow_goto_subreddit)
                        .setTitle("Go to /r/" + mCurrSubmission.getSubreddit());
                // only display Download option if submission has downloadable media
                m.findItem(R.id.menu_full_display_overflow_download)
                        .setVisible(mCurrSubmission.isDownloadableMedia());

                overflowPopup.show();
                overflowPopup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.menu_full_display_overflow_goto_subreddit:
                                App.getMoxieInfoObj().getmSubredditStack().push(mCurrSubmission.getSubreddit());
                                Intent visitSubredditIntent = new Intent(getContext(), ActivitySubredditViewer.class);
                                visitSubredditIntent.putExtra(Constants.EXTRA_GOTO_SUBREDDIT, mCurrSubmission.getSubreddit());
                                startActivityForResult(visitSubredditIntent, Constants.REQUESTCODE_GOTO_SUBREDDIT_VIEWER);
                                return true;
                            case R.id.menu_full_display_overflow_download:
                                // the callback from permissions request will call the
                                // actual download method (see onRequestPermissionsResult)
                                Utils.hasDownloadPermissions(FragmentFullDisplay.this);
                                return true;
                            case R.id.menu_full_display_overflow_share:
                                return true;
                        }
                        return false;
                    }
                });
            }
        });
    }


    public void openComments() {
        if(!mCommentsInitialized){
            initComments();
        }
        mCommentsContainer.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,
                0,
                mCommentsContainer.getHeight(),
                0);
        animate.setDuration(Constants.COMMENTS_OPEN_SWIPE_DURATION);
        animate.setFillAfter(true);
        mCommentsContainer.startAnimation(animate);
        mCommentsOpen = true;
        mCommentsEventListener.isCommentsOpen(true);
    }

    public void closeComments() {
        TranslateAnimation animate = new TranslateAnimation(
                0,
                0,
                0,
                mCommentsContainer.getHeight());
        animate.setDuration(Constants.COMMENTS_CLOSE_SWIPE_DURATION);
        //animate.setFillAfter(true);
        mCommentsContainer.startAnimation(animate);
        mCommentsContainer.setVisibility(View.GONE);
        mCommentsOpen = false;
        mCommentsEventListener.isCommentsOpen(false);
        //mCommentsButtonClose.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case Constants.PERMISSIONS_DOWNLOAD_MEDIA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downloadMedia();
                } else {
                    Toast.makeText(getContext(),
                            getResources().getString(R.string.toast_download_permissions_required),
                            Toast.LENGTH_LONG).show();
                    //Utils.hasDownloadPermissions(this);
                }
        }
    }

    private void initComments() {
        mCommentsRecyclerLayoutManager = new LinearLayoutManager(getContext());
        mCommentsRecyclerView.setLayoutManager(mCommentsRecyclerLayoutManager);
        new FetchCommentsTask().execute();
    }


    // Comments stuff
    private void setupCommentsAdapter() {
        CommentsAdapter adapter = new CommentsAdapter();
        mCommentsRecyclerView.setAdapter(adapter);
    }

    private class CommentsAdapter extends RecyclerView.Adapter<CommentViewHolder> {
        public CommentsAdapter() {
        }

        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.recycler_item_comment, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentViewHolder holder, int rootPosition) {
            CommentNode viewholderComment = comments.get(rootPosition).comment;
            holder.commentsTextViewAuthor.setText(comments.get(rootPosition).comment.getSubject().getAuthor()/* + "::: " + position*/);
            holder.commentsTextViewBody.setText(comments.get(rootPosition).comment.getSubject().getBody());

            //children
           /* List replies = comments.get(rootPosition).comment.getReplies();


            CommentNode curr = viewholderComment;

            for(int i = 0; i < Constants.COMMENT_LOAD_CHILD_LIMIT; i++){
                if(curr.getReplies().size() > 0){
                    curr.getReplies().get(0);
                }
            }*/
        }


        @Override
        public int getItemCount() {
            return comments.size();
        }
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView commentsTextViewAuthor;
        TextView commentsTextViewBody;

        public CommentViewHolder(View itemView) {
            super(itemView);
            commentsTextViewAuthor = (TextView) itemView.findViewById(R.id.comment_item_author);
            commentsTextViewBody = (TextView) itemView.findViewById(R.id.comment_item_body);
        }

        @Override
        public void onClick(View view) {
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void startAndBindDownloadService() {
        Intent downloadIntent = new Intent(getActivity(), DownloadService.class);
        getActivity().startService(downloadIntent);
        getActivity().getApplicationContext().bindService(downloadIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    // we've deemed the media downloadable at this point (Download button
    //  is only available if submissionObj has isDownloadableMedia == true)
    private void downloadMedia() {
        String mediaUrl;

        //Vreddit submissions might be(should be) cached already
        if (mCurrSubmission.getDomain() == Constants.SubmissionDomain.VREDDIT) {
            mediaUrl = mCurrSubmission.getEmbeddedMedia().getRedditVideo().getFallbackUrl();
            downloadBinder.startDownloadVreddit(mediaUrl, mCurrSubmission.getPrettyFilename());
        } else {
            if (mCurrSubmission.getMp4Url() != null)
                mediaUrl = mCurrSubmission.getMp4Url();
            else if (mCurrSubmission.getCleanedUrl() != null)
                mediaUrl = mCurrSubmission.getCleanedUrl();
            else
                mediaUrl = mCurrSubmission.getUrl();

            Toast.makeText(getContext(), getResources()
                    .getString(R.string.toast_download_pre), Toast.LENGTH_SHORT).show();
            downloadBinder.startDownload(mediaUrl,
                    mCurrSubmission.getPrettyFilename(),
                    0);
        }
    }

    /* Focuses up to two views. Pass null if only need 1 view focused*/
    private void focusView(View focused1, View focused2) {
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

    // Pop this fragment off the stack, effectively closing the submission viewer.
    private void closeFullDisplay() {
        //releaseExoPlayer();
        try {
            getActivity().getSupportFragmentManager().popBackStack();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            focusView(mFailedLoadText, null);
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

    private class FetchCommentsTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            RedditClient redditClient = App.getAccountHelper().getReddit();
            String id = mCurrSubmission.getId();
            RootCommentNode baseComments = redditClient.submission(id).comments();


            comments = new ArrayList<>();
            Map<Integer, MoreChildItem> waiting = new HashMap<>();
            commentOPs = new HashMap<>();
            String currentOP = "";


            List<CommentNode<Comment>> rootComments =
                    baseComments.walkTree().iterator().next().getReplies();

            for (CommentNode<Comment> root : rootComments) {
                // maps comment ID to author
                commentOPs.put(root.getSubject().getId(), root.getSubject().getAuthor());
                CommentObj commentObj = new CommentItem(root);
                comments.add(commentObj);


                if (root.hasMoreChildren()) {
                    waiting.put(root.getDepth(), new MoreChildItem(root, root.getMoreChildren()));
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setupCommentsAdapter();
            Log.e("NUM_ROOT_COMMENTS: ", String.valueOf(comments.size()));
            mCommentsProgressBar.setVisibility(View.GONE);
            mCommentsInitialized = true;
        }
    }

    // Used when detecting swipe gestures in comments section
    private class CommentsGestureDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            // don't handle simple down click events
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1,
                               MotionEvent e2,
                               float velocityX,
                               float velocityY) {
            // Minimal x and y axis swipe distance.
            int MIN_SWIPE_DISTANCE_X = 100;
            int MIN_SWIPE_DISTANCE_Y = 5;

            // Maximal x and y axis swipe distance.
            int MAX_SWIPE_DISTANCE_X = 1000;
            int MAX_SWIPE_DISTANCE_Y = 1000;
            try {
                // Get swipe delta value in x axis.
                float deltaX = e1.getX() - e2.getX();

                // Get swipe delta value in y axis.
                float deltaY = e1.getY() - e2.getY();

                // Get absolute value.
                float deltaXAbs = Math.abs(deltaX);
                float deltaYAbs = Math.abs(deltaY);

                if ((deltaYAbs >= MIN_SWIPE_DISTANCE_Y) && (deltaYAbs <= MAX_SWIPE_DISTANCE_Y)) {
                    if (deltaY > 0) {
                        if(!mCommentsOpen){
                            openComments();
                        }
                        Log.e("SWIPE_TEST", "SWIPED UP!");
                    } else {
                        if(mCommentsOpen){
                            closeComments();
                        }
                    }
                }
            } catch (Exception e) {
                // nothing
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

}
