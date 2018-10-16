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
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.ablanco.zoomy.DoubleTapListener;
import com.ablanco.zoomy.LongPressListener;
import com.ablanco.zoomy.TapListener;
import com.ablanco.zoomy.Zoomy;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
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
import com.sometimestwo.moxie.EventListeners.OnRedditTaskListener;
import com.sometimestwo.moxie.EventListeners.OnTaskCompletedListener;
import com.sometimestwo.moxie.EventListeners.OnVRedditTaskCompletedListener;
import com.sometimestwo.moxie.Model.CommentObj;
import com.sometimestwo.moxie.Model.ExpandableCommentGroup;
import com.sometimestwo.moxie.Model.GfyItem;
import com.sometimestwo.moxie.Model.GfycatWrapper;
import com.sometimestwo.moxie.Model.SubmissionObj;
import com.sometimestwo.moxie.Utils.Constants;
import com.sometimestwo.moxie.Utils.DownloadBinder;
import com.sometimestwo.moxie.Utils.DownloadService;
import com.sometimestwo.moxie.Utils.Utils;
import com.xwray.groupie.GroupAdapter;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Comment;
import net.dean.jraw.models.VoteDirection;
import net.dean.jraw.tree.CommentNode;
import net.dean.jraw.tree.RootCommentNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.BIND_AUTO_CREATE;

public class FragmentFullDisplay extends Fragment implements OnVRedditTaskCompletedListener {
    public static String TAG = FragmentFullDisplay.class.getSimpleName();

    private RequestManager GlideApp = App.getGlideApp();
    private SubmissionObj mCurrSubmission;
    private boolean mIsUserless;
    private boolean mPrefsAllowNSFW;
    private boolean mAllowCloseOnClick;
    private BroadcastReceiver mDLCompleteReceiver;
    private OnCommentsEventListener mCommentsEventListener;

    /* Titles */
    TextView mTitleTextView;
    TextView mSubmissionInfoTextView;
    ImageView mGoldStar;
    TextView mGoldTextView;
    RelativeLayout mHeaderContainer;

    /* Snackbar */
    private ImageView mButtonComments;
    private TextView mCommentCountTextView;
    private ImageView mButtonUpvote;
    private TextView mUpvoteCountTextView;
    private FrameLayout mDownvoteContainer;
    private ImageView mButtonDownvote;
    FrameLayout mSaveButtonContainer;
    private ImageView mButtonSave;
    private ImageView mButtonOverflow;
    private LinearLayout mSnackbarContainer;
    private VoteDirection mVoteDirection;
    private boolean mIsSaved;
    private int mUpvoteCount;

    /* Comments */
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

    /* Zoomable image view*/
    PhotoView mZoomView;

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

    /* Async tasks which might need cancelling */
    AsyncTask<Void, Void, Void> FetchCommentsTask;
    AsyncTask<Void,Void,Boolean> ReauthUserTask;


    public interface OnCommentsEventListener {
        public void isCommentsOpen(boolean commentsOpen);
    }

    public static FragmentFullDisplay newInstance() {
        return new FragmentFullDisplay();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setRetainInstance(true);
        unpackArgs();

        // Read relevant permission settings
        SharedPreferences prefs = getContext().getSharedPreferences(Constants.KEY_SHARED_PREFS, Context.MODE_PRIVATE);

        mPrefsAllowNSFW = prefs.getBoolean(Constants.PREFS_HIDE_NSFW, false);
        mAllowCloseOnClick = prefs.getBoolean(Constants.PREFS_ALLOW_CLOSE_CLICK, true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_full_displayer, container, false);

        mIsUserless = Utils.isUserlessSafe();

        /* Header */
        mHeaderContainer = (RelativeLayout) v.findViewById(R.id.big_display_title_container);
        mTitleTextView = (TextView) v.findViewById(R.id.full_display_title);
        mSubmissionInfoTextView = (TextView) v.findViewById(R.id.full_display_submission_info);
        mGoldStar = (ImageView) v.findViewById(R.id.full_display_gold_star);
        mGoldTextView = (TextView) v.findViewById(R.id.full_display_gold_text);

        /* Snackbar */
        mButtonComments = (ImageView) v.findViewById(R.id.full_display_snack_bar_comments);
        mCommentCountTextView = (TextView) v.findViewById(R.id.full_display_comments_counter);
        mButtonUpvote = (ImageView) v.findViewById(R.id.full_display_snack_bar_upvote);
        mUpvoteCountTextView = (TextView) v.findViewById(R.id.full_display_upvote_counter);
        mDownvoteContainer = (FrameLayout) v.findViewById(R.id.full_display_downvote_container);
        mButtonDownvote = (ImageView) v.findViewById(R.id.full_display_snack_bar_downvote);
        mSaveButtonContainer = (FrameLayout) v.findViewById(R.id.full_display_save_container);
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
        mZoomView = (PhotoView) v.findViewById(R.id.full_displayer_zoomie_view);

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
                    Intent youTubeIntent = new Intent(getContext(), ActivityYouTubePlayer.class);
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
        setupComments();
        startAndBindDownloadService();

        /* Click listeners that listen for click-to-close functionality*/
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

        // stop any async tasks that might still be running
        cancelRunning();
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

    private void cancelRunning() {
        if(FetchCommentsTask != null) FetchCommentsTask.cancel(true);
        if(ReauthUserTask != null) ReauthUserTask.cancel(true);

    }

    private void setupHeader() {
        // Title
        mTitleTextView.setText(mCurrSubmission.getTitle());

        // Submission info
        mSubmissionInfoTextView.setText(submissionInfoTextBuilder());
        if (mCurrSubmission.getGilded() > 0) {
            mGoldStar.setVisibility(View.VISIBLE);
            mGoldTextView.setText(mCurrSubmission.getGilded() > 1 ? "x" + mCurrSubmission.getGilded() : "");
        }

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

    private String submissionInfoTextBuilder() {
        StringBuilder sb = new StringBuilder();
        sb.append("/r/").append(mCurrSubmission.getSubreddit())
                .append("  \u2022  ")
                .append(mCurrSubmission.getAuthor())
                .append("  \u2022  ")
                .append(Utils.getStringTimestamp(mCurrSubmission.getDateCreated().getTime()));

        // need another separator if gilded
        if (mCurrSubmission.getGilded() > 0)
            sb.append("  \u2022  ");

        return sb.toString();
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
                    .contains(Utils.getFileExtensionFromUrl(imageUrl))) {
                mProgressBar.setVisibility(View.VISIBLE);
                // fixes indirect imgur url and uses Glide to load image on success
                Utils.fixIndirectImgurUrl(mCurrSubmission, Utils.getImgurHash(imageUrl),
                        new OnTaskCompletedListener() {
                            @Override
                            public void downloadSuccess() {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mProgressBar.setVisibility(View.GONE);
                                            focusView(mZoomView, null);
                                            Zoomy.Builder builder = new Zoomy.Builder(getActivity())
                                                    .target(mZoomView)
                                                    .interpolator(new OvershootInterpolator());
                                            builder.register();
                                            GlideApp.load(mCurrSubmission.getCleanedUrl())
                                                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                                                    .into(mZoomView);
                                        }
                                    });
                                } else {
                                    Log.e(TAG,
                                            "getActivity() null when trying to fixIndirectImgurUrl for URL "
                                                    + mCurrSubmission.getUrl());
                                }
                            }

                            @Override
                            public void downloadFailure() {
                                super.downloadFailure();
                            }
                        });
            } else {
                mProgressBar.setVisibility(View.VISIBLE);
                focusView(mZoomView, null);
                Zoomy.Builder builder = new Zoomy.Builder(getActivity())
                        .target(mZoomView)
                        .interpolator(new OvershootInterpolator())
                        .tapListener(new TapListener() {
                            @Override
                            public void onTap(View v) {
                                if (mAllowCloseOnClick) {
                                    closeFullDisplay();
                                }
                            }
                        })
                        .longPressListener(new LongPressListener() {
                            @Override
                            public void onLongPress(View v) {

                            }
                        }).doubleTapListener(new DoubleTapListener() {
                            @Override
                            public void onDoubleTap(View v) {
                            }
                        });
                builder.register();
                GlideApp.load(imageUrl)
                        .listener(new GlideProgressListener(mProgressBar))
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(mZoomView);
            }

            //TODO: handle invalid URL
        } else if (mCurrSubmission.getSubmissionType() == Constants.SubmissionType.GIF
                || mCurrSubmission.getSubmissionType() == Constants.SubmissionType.VIDEO) {
            mProgressBar.setVisibility(View.VISIBLE);

            // gif might have a .gifv (imgur) extension
            // ...need to fetch corresponding .mp4
            if (mCurrSubmission.getDomain() == Constants.SubmissionDomain.IMGUR
                    && Utils.getFileExtensionFromUrl(imageUrl)
                    .equalsIgnoreCase("gifv")) {
                Utils.getMp4LinkImgur(mCurrSubmission,
                        Utils.getImgurHash(imageUrl),
                        new OnTaskCompletedListener() {
                            @Override
                            public void downloadSuccess() {
                                super.downloadSuccess();
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            //exo player has its own progress bar
                                            mProgressBar.setVisibility(View.GONE);
                                            focusView(mExoplayer, mExoplayerContainer);
                                            initializeExoPlayer(mCurrSubmission.getCleanedUrl());
                                        }
                                    });
                                } else {
                                    Log.e(TAG,
                                            "getActivity null when trying to getMp4LinkImgur for url "
                                                    + mCurrSubmission.getUrl());
                                }
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
            } else if (mCurrSubmission.getDomain() == Constants.SubmissionDomain.GFYCAT
                    && mCurrSubmission.getCleanedUrl() == null) {
                // We're given a URL in this format: //https://gfycat.com/SpitefulGoldenAracari
                // extract gfycat ID (looks like:SpitefulGoldenAracari)
                String gfycatHash = Utils.getGfycatHash(mCurrSubmission.getUrl());
                // get Gfycat .mp4 "clean url"
                Call<GfycatWrapper> gfycatObj = Utils.getGyfCatObjToEnqueue(gfycatHash, mCurrSubmission);
                gfycatObj.enqueue(new Callback<GfycatWrapper>() {
                    @Override
                    public void onResponse(Call<GfycatWrapper> call, Response<GfycatWrapper> response) {
                        //Log.d(TAG, "onResponse: feed: " + response.body().toString());
                        Log.d("GFYCAT_RESPONSE",
                                "getGyfCatObjToEnqueue onResponse: Server Response: " + response.toString());

                        GfyItem gfyItem = new GfyItem();
                        try {
                            gfyItem = response.body().getGfyItem();
                        } catch (Exception e) {
                            Log.e("GFYCAT_RESPONSE_ERROR",
                                    "Failed in attempt to retrieve gfycat object for hash "
                                            + gfycatHash + ". "
                                            + e.getMessage());
                            call.cancel();
                        }
                        if (gfyItem != null) {
                            mCurrSubmission.setCleanedUrl(gfyItem.getMobileUrl() != null ? gfyItem.getMobileUrl() : gfyItem.getMp4Url());
                            mCurrSubmission.setMp4Url(gfyItem.getMp4Url());

                            // Display gfycat
                            mProgressBar.setVisibility(View.GONE);
                            focusView(mExoplayer, mExoplayerContainer);
                            initializeExoPlayer(mCurrSubmission.getCleanedUrl());
                        }
                    }

                    @Override
                    public void onFailure(Call<GfycatWrapper> call, Throwable t) {
                        call.cancel();
                        Log.e("GETGFYCAT_ERROR",
                                "getGyfCatObjToEnqueue onFailure: Unable to retrieve Gfycat: " + t.getMessage());
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
                focusView(mZoomView, null);
                mProgressBar.setVisibility(View.VISIBLE);
                Zoomy.Builder builder = new Zoomy.Builder(getActivity())
                        .target(mZoomView)
                        .interpolator(new OvershootInterpolator())
                        .tapListener(new TapListener() {
                            @Override
                            public void onTap(View v) {
                                if (mAllowCloseOnClick) {
                                    closeFullDisplay();
                                }
                            }
                        })
                        .longPressListener(new LongPressListener() {
                            @Override
                            public void onLongPress(View v) {
                            }
                        }).doubleTapListener(new DoubleTapListener() {
                            @Override
                            public void onDoubleTap(View v) {
                            }
                        });

                builder.register();
                //TODO Progress bar
                GlideApp.asGif()
                        .load(imageUrl)
                        .listener(new RequestListener<GifDrawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e,
                                                        Object model,
                                                        Target<GifDrawable> target,
                                                        boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GifDrawable resource,
                                                           Object model,
                                                           Target<GifDrawable> target,
                                                           com.bumptech.glide.load.DataSource dataSource,
                                                           boolean isFirstResource) {
                                mProgressBar.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(mZoomView);
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
            focusView(mZoomView, null);
            mProgressBar.setVisibility(View.VISIBLE);

            Zoomy.Builder builder = new Zoomy.Builder(getActivity())
                    .target(mZoomView)
                    .interpolator(new OvershootInterpolator())
                    .tapListener(new TapListener() {
                        @Override
                        public void onTap(View v) {
                            if (mAllowCloseOnClick) {
                                closeFullDisplay();
                            }
                        }
                    })
                    .longPressListener(new LongPressListener() {
                        @Override
                        public void onLongPress(View v) {
                        }
                    }).doubleTapListener(new DoubleTapListener() {
                        @Override
                        public void onDoubleTap(View v) {
                        }
                    });

            builder.register();
            GlideApp.load(Constants.URI_404)
                    .listener(new GlideProgressListener(mProgressBar))
                    .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(mZoomView);
        }
    }

    private void setupSnackBar() {
        mVoteDirection = mCurrSubmission.getVote();
        mIsSaved = mCurrSubmission.isSaved();
        mUpvoteCount = mCurrSubmission.getScore();
        Drawable yellowStar = getResources().getDrawable(R.drawable.ic_yellow_star_filled_2);
        Drawable whiteStar = getResources().getDrawable(R.drawable.ic_white_star_unfilled_2);
        Drawable upVoteWhite = getResources().getDrawable(R.drawable.ic_white_upvote_medium);
        Drawable downVoteWhite = getResources().getDrawable(R.drawable.ic_white_downvote_medium);
        Drawable upVoteOrange = getResources().getDrawable(R.drawable.ic_upvote_orange);
        Drawable downVoteBlue = getResources().getDrawable(R.drawable.ic_downvote_blue);
        int colorUpvoteOrange = getResources().getColor(R.color.upvote_orange);
        int colorWhite = getResources().getColor(R.color.colorWhite);

        mCommentCountTextView.setText(Utils.truncateCount(mCurrSubmission.getCommentCount()));
        mUpvoteCountTextView.setTextColor(mCurrSubmission.getVote() == VoteDirection.UP ? colorUpvoteOrange : colorWhite);
        mUpvoteCountTextView.setText(Utils.truncateCount(mCurrSubmission.getScore()));
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
                    mUpvoteCountTextView.setText(mVoteDirection == VoteDirection.UP ? Utils.truncateCount(++mUpvoteCount) : Utils.truncateCount(--mUpvoteCount));
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

        mDownvoteContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mButtonDownvote.callOnClick();
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
                            ? Utils.truncateCount(--mUpvoteCount) : Utils.truncateCount(mUpvoteCount));
                    mVoteDirection = (mVoteDirection != VoteDirection.DOWN) ? VoteDirection.DOWN : VoteDirection.NONE;
                    mButtonDownvote.setBackground(mVoteDirection == VoteDirection.DOWN ? downVoteBlue : downVoteWhite);
                    mButtonUpvote.setBackground(mVoteDirection == VoteDirection.UP ? upVoteOrange : upVoteWhite);
                    new Utils.VoteSubmissionTask(mCurrSubmission, FragmentFullDisplay.this, mVoteDirection).execute();
                }
            }
        });
        // Save button
        mSaveButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mButtonSave.callOnClick();
            }
        });
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


                // Display the submission's URL inside the option "go to {URL}..."
                String truncatedURL = mCurrSubmission.getUrl().substring(0, Constants.MAX_LENGTH_URL_DISPLAY);
                String gotoString = getResources().getString(R.string.goto_web_url, truncatedURL);
                m.findItem(R.id.menu_full_display_overflow_open_web).setTitle(gotoString);

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
                            case R.id.menu_full_display_overflow_open_web:
                                Intent intent = new Intent(
                                        FragmentFullDisplay.this.getActivity(),
                                        ActivityWebView.class);
                                intent.putExtra(Constants.EXTRA_GOTO_WEB_URL, mCurrSubmission.getUrl());
                                startActivity(intent);
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

    // Does not actually LOAD comments. Only initializes views and click listeners
    private void setupComments() {
        // Each time we enter the full display we need to make sure
        // our hosting activity knows comments section isn't open yet.
        // This is for handling back button press when comments are open
        mCommentsEventListener.isCommentsOpen(false);

        // Button that closes the entire comments section (an alternative to swiping downwards)
        mCommentsButtonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //redundant check
                if (mCommentsOpen) {
                    closeComments();
                }
            }
        });

        // Dummy area that is used for capturing events to close comment section
        mCommentsDummyTopView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //redundant check
                if (mCommentsOpen) {
                    closeComments();
                }
            }
        });

        // Detects swipes when our comment section is open
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
    }

    public void openComments() {
        if (!mCommentsInitialized) {
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
        if(App.getAccountHelper().isAuthenticated()){
            FetchCommentsTask = new FetchCommentsTask().execute();
        }
        else{
            ReauthUserTask = new Utils.RedditReauthTask(new OnRedditTaskListener() {
                @Override
                public void onSuccess() {
                    FetchCommentsTask = new FetchCommentsTask().execute();
                }

                @Override
                public void onFailure(String exceptionMessage) {
                    //todo
                }
            }).execute();
        }
    }


    // Comments stuff
    private void setupCommentsAdapter() {
        GroupAdapter groupAdapter = new GroupAdapter();
        mCommentsRecyclerView.setAdapter(groupAdapter);
        mCommentsRecyclerLayoutManager = new LinearLayoutManager(getContext());
        mCommentsRecyclerView.setLayoutManager(mCommentsRecyclerLayoutManager);

        // add all root comments
        for (CommentObj c : comments) {
            ExpandableCommentGroup expandableCommentGroup =
                    new ExpandableCommentGroup(
                            c,
                            true,
                            mCurrSubmission);
            groupAdapter.add(expandableCommentGroup);
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
        if (getActivity() != null) {
            Intent downloadIntent = new Intent(getActivity(), DownloadService.class);
            getActivity().startService(downloadIntent);
            getActivity().getApplicationContext().bindService(downloadIntent, serviceConnection, BIND_AUTO_CREATE);
        } else {
            Log.e(TAG, "getActivity() null on attempting to startAndBindDownloadService()");
        }
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
        mExoplayer.setVisibility(focused1 == mExoplayer || focused2 == mExoplayer ? View.VISIBLE : View.GONE);
        mExoplayerContainer.setVisibility(focused1 == mExoplayerContainer || focused2 == mExoplayerContainer ? View.VISIBLE : View.GONE);
        mVideoView.setVisibility(focused1 == mVideoView || focused2 == mVideoView ? View.VISIBLE : View.GONE);
        mVideoviewContainer.setVisibility(focused1 == mVideoviewContainer || focused2 == mVideoviewContainer ? View.VISIBLE : View.GONE);
        mYouTubeThumbnail.setVisibility(focused1 == mYouTubeThumbnail || focused2 == mYouTubeThumbnail ? View.VISIBLE : View.GONE);
        mYoutubeIconsOverlay.setVisibility(focused1 == mYoutubeIconsOverlay || focused2 == mYoutubeIconsOverlay ? View.VISIBLE : View.GONE);
        mFailedLoadText.setVisibility(focused1 == mFailedLoadText || focused2 == mFailedLoadText ? View.VISIBLE : View.GONE);
        mZoomView.setVisibility(focused1 == mZoomView || focused2 == mZoomView ? View.VISIBLE : View.GONE);
    }

    // Pop this fragment off the stack, effectively closing the submission viewer.
    private void closeFullDisplay() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
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
            List<CommentNode<Comment>> rootComments =
                    baseComments.walkTree().iterator().next().getReplies();

            for (CommentNode<Comment> rootComment : rootComments) {
                comments.add(new CommentObj(rootComment, mCurrSubmission));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            setupCommentsAdapter();
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
                        if (!mCommentsOpen) {
                            openComments();
                        }
                        Log.e("SWIPE_TEST", "SWIPED UP!");
                    } else {
                        if (mCommentsOpen) {
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
