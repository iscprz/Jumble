package com.sometimestwo.moxie;

import android.annotation.SuppressLint;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.paging.PagedList;
import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
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
import com.sometimestwo.moxie.Imgur.client.ImgurClient;
import com.sometimestwo.moxie.Imgur.response.images.ImgurSubmission;
import com.sometimestwo.moxie.Imgur.response.images.SubmissionRoot;
import com.sometimestwo.moxie.Model.SubmissionObj;
import com.sometimestwo.moxie.Utils.Constants;
import com.sometimestwo.moxie.Utils.Helpers;

import net.dean.jraw.RedditClient;

import java.util.Arrays;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

public class FragmentHome extends Fragment {
    public final static String TAG = Constants.TAG_FRAG_HOME;
    private final static String ARGS_NUM_DISPLAY_COLS = "ARGS_NUM_DISPLAY_COLS";
    private final static int INTENT_LOG_IN = 1;

    private SwipeRefreshLayout mRefreshLayout;
    private MultiClickRecyclerView mRecyclerMain;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private int mNumDisplayColumns;
    private String mCurrSubreddit;
    private RedditClient mRedditClient;
    private boolean isImageViewPressed = false;
    // temporarily stores mp4 link to corresponding GIFV
    private String mp4Url;

    // settings prefs
    SharedPreferences prefs;
    private boolean mIsLoggedIn = false;
    private boolean mPrefsAllowNSFW = false;
    private boolean mAllowImagePreview = false;
    // private boolean mAllowGifPreview = false;
    private Constants.HoverPreviewSize mPreviewSize;

    // hover preview small
    private RelativeLayout mHoverPreviewContainerSmall;
    private TextView mHoverPreviewTitleSmall;
    private ImageView mHoverPreviewSmall;
    // private PopupWindow mPopupWindow;
    // private View mPopupView;

    // hover view large
    private RelativeLayout mHoverPreviewContainerLarge;
    private TextView mHoverPreviewTitleLarge;
    private ImageView mHoverPreviewLarge;

    //exo player stuff
    private BandwidthMeter bandwidthMeter;
    private PlayerView mExoplayerLarge;
    private DefaultTrackSelector trackSelector;
    private SimpleExoPlayer player;
    private ProgressBar progressBar;
    private DataSource.Factory mediaDataSourceFactory;
    private int currentWindow;
    private long playbackPosition;
    private Timeline.Window window;

    // event listeners
    private HomeEventListener mHomeEventListener;

    public interface HomeEventListener {
        public void openMediaViewer(SubmissionObj submission);

        public void openSettings();

        public void refreshFeed(String fragmentTag);
    }

    public static FragmentHome newInstance() {
        return new FragmentHome();
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unpackArgs();
        //  mRedditClient = App.getAccountHelper().isAuthenticated() ? App.getAccountHelper().getReddit() : App.getAccountHelper().switchToUserless();
        setHasOptionsMenu(true);
        prefs = getContext().getSharedPreferences(Constants.KEY_GETPREFS_SETTINGS, Context.MODE_PRIVATE);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        //mPopupView = inflater.inflate(R.layout.layout_popup_preview_small,null);
        //mParentView = v.findViewById(R.id.main_content);

        /* Hamburger menu*/
        setupHamburgerMenu(v);

        /* Toolbar setup*/
        mToolbar = (Toolbar) v.findViewById(R.id.toolbar_main);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        /* Refresh layout setup*/
        mRefreshLayout = v.findViewById(R.id.recycler_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
                SubmissionsViewModel submissionsViewModel = ViewModelProviders.of(FragmentHome.this).get(SubmissionsViewModel.class);
                submissionsViewModel.invalidate();
                mHomeEventListener.refreshFeed(TAG);
            }
        });

        /* Recycler view setup*/
        mRecyclerMain = (MultiClickRecyclerView) v.findViewById(R.id.recycler_zoomie_view);
        mRecyclerMain.setLayoutManager(new GridLayoutManager(getContext(), mNumDisplayColumns));
        mRecyclerMain.setHasFixedSize(true);
        //  disabler = new RecyclerViewDisabler();

        /* RecyclerView adapter stuff */
        final RecyclerAdapter adapter = new RecyclerAdapter(getContext(), Glide.with(this));

        /* Viewmodel fetching and data updating */
        SubmissionsViewModel submissionsViewModel = ViewModelProviders.of(this).get(SubmissionsViewModel.class);
        submissionsViewModel.postsPagedList.observe(getActivity(), new Observer<PagedList<SubmissionObj>>() {
            @Override
            public void onChanged(@Nullable PagedList<SubmissionObj> items) {
                // submitting changes to adapter, if any
                adapter.submitList(items);
            }
        });

        mRecyclerMain.setAdapter(adapter);

        /* hover view small*/
        mHoverPreviewContainerSmall = (RelativeLayout) v.findViewById(R.id.hover_view_container_small);
        mHoverPreviewTitleSmall = (TextView) v.findViewById(R.id.hover_view_title_small);
        mHoverPreviewSmall = (ImageView) v.findViewById(R.id.hover_view_small);

        /* hover view large*/
        mHoverPreviewContainerLarge = (RelativeLayout) v.findViewById(R.id.hover_view_container_large);
        mHoverPreviewTitleLarge = (TextView) v.findViewById(R.id.hover_view_title_large);
        mHoverPreviewLarge = (ImageView) v.findViewById(R.id.hover_view_large);

        /* Exo player */
        mExoplayerLarge = (PlayerView) v.findViewById(R.id.exoplayer_large);
        bandwidthMeter = new DefaultBandwidthMeter();
        mediaDataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "mediaPlayerSample"), (TransferListener<? super DataSource>) bandwidthMeter);
        window = new Timeline.Window();
        //ivHideControllerButton = findViewById(R.id.exo_controller);
       // progressBar = findViewById(R.id.progress_bar);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mHomeEventListener = (HomeEventListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement listener inferfaces!");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        //toolbar setup
        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null) {
            //toolbar.setTitle(getResources().getString(R.string.toolbar_title_albums));
            toolbar.setDisplayHomeAsUpEnabled(true);
            toolbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        // Check if user settings have been altered.
        // i.e. User went to settings, opted in to NSFW posts then navigated back.
        validatePreferences();
        //loggedIn = ???
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_LOG_IN) {
            if (resultCode == RESULT_OK) {

            }
        }
    }

    private void unpackArgs() {
        // SharedPreferencesTokenStore tokenStore = App.getTokenStore();
        try {
            mNumDisplayColumns = (Integer) this.getArguments().get(ARGS_NUM_DISPLAY_COLS);
            //  mCurrSubreddit = mRedditClient.getmRedditDataRequestObj().getmSubreddit();
        } catch (NullPointerException npe) {
            throw new NullPointerException("Null ptr exception trying to unpack arguments in " + TAG);
        }
        // default to 3 if at 0. //TODO: revisit default here
        if (mNumDisplayColumns < 1) {
            mNumDisplayColumns = Constants.DEFAULT_NUM_DISPLAY_COLS;
        }
        // default to /r/pics as failsafe if nothing was passed to us
        if (mCurrSubreddit == null || "".equals(mCurrSubreddit)) {
            mCurrSubreddit = Constants.DEFAULT_SUBREDDIT;
        }
    }

    /* Hamburger menu*/
    private void setupHamburgerMenu(View v) {
        mDrawerLayout = v.findViewById(R.id.drawer_layout);

        NavigationView navigationView = v.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        //menuItem.setChecked(true);
                        handleNavItemSelection(menuItem);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
        mDrawerLayout.addDrawerListener(
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        // Respond when the drawer's position changes
                        Log.e(TAG, "Drawer slide!");
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Respond when the drawer is opened
                        Log.e(TAG, "Drawer opened!");

                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Respond when the drawer is closed
                        Log.e(TAG, "Drawer closed!");
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        // Respond when the drawer motion state changes
                        Log.e(TAG, "Drawer state changed!");
                    }
                }
        );
    }

    /* Handles left navigation menu item selections*/
    private void handleNavItemSelection(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
           /* case R.id.nav_log_in:
                Intent loginIntent = new Intent(getContext(), ActivityLogin.class);
                //unlockSessionIntent.putExtra("REQUEST_UNLOCK_SESSION", true);
                startActivityForResult(loginIntent,INTENT_LOG_IN);
                return;*/
            case R.id.nav_menu_goto_subreddit:
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Enter subreddit:");

                final EditText input = new EditText(getContext());
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        App.getCurrSubredditObj().setSubreddit(input.getText().toString());
                        SubmissionsViewModel submissionsViewModel = ViewModelProviders.of(FragmentHome.this).get(SubmissionsViewModel.class);
                        submissionsViewModel.invalidate();
                        mHomeEventListener.refreshFeed(TAG);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
                return;

            case R.id.nav_settings:
                mHomeEventListener.openSettings();
                return;
            default:
                Log.e(TAG, "Nav item selection not found! Entered default case!");
        }
    }

    private void saveRecyclerViewState() {

    }

    private void restoreRecyclerViewState() {

    }

    private void validatePreferences() {
        mPrefsAllowNSFW = prefs.getString(Constants.KEY_ALLOW_NSFW, Constants.SETTINGS_NO).equalsIgnoreCase(Constants.SETTINGS_YES);
        mAllowImagePreview = prefs.getString(Constants.KEY_ALLOW_HOVER_PREVIEW, Constants.SETTINGS_NO).equalsIgnoreCase(Constants.SETTINGS_YES);
        mPreviewSize = prefs.getString(Constants.KEY_PREVIEW_SIZE, Constants.SETTINGS_PREVIEW_SIZE_SMALL)
                .equalsIgnoreCase(Constants.SETTINGS_PREVIEW_SIZE_SMALL)
                ? Constants.HoverPreviewSize.SMALL : Constants.HoverPreviewSize.LARGE;
    }

    private static DiffUtil.ItemCallback<SubmissionObj> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<SubmissionObj>() {
                @Override
                public boolean areItemsTheSame(SubmissionObj oldItem, SubmissionObj newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(SubmissionObj oldItem, SubmissionObj newItem) {
                    return oldItem.equals(newItem);
                }
            };


    public class RecyclerAdapter extends PagedListAdapter<SubmissionObj, RecyclerAdapter.ItemViewHolder> {
        private Context mContext;
        private final RequestManager GlideApp;


        RecyclerAdapter(Context mContext, RequestManager glideApp) {
            super(DIFF_CALLBACK);
            this.mContext = mContext;
            this.GlideApp = glideApp;
        }

        @NonNull
        @Override
        public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.recycler_item_thumbnail, parent, false);
            return new ItemViewHolder(view);
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public void onBindViewHolder(@NonNull final ItemViewHolder holder, int position) {
            SubmissionObj item = getItem(position);
            //ignore any items that do not have thumbnail do display
            if (item != null && !item.isSelfPost()) {
                // Imgur
                if (item.getDomain().contains("imgur")) {
                    // try setting the submission type here. Imgur links will have .jpg/.gifv
                    // appended to them if linked directly. Don't worry about indirect links here.
                    item.setSubmissionType(Helpers.getSubmissionType(item.getUrl()));

                    // Check if submission type is null. This will happen if the item's URL is
                    // to a non-direct image/gif link such as https://imgur.com/qTadRtq
                    if (item.getSubmissionType() == null) {
                        String imgurHash = Helpers.getImgurHash(item.getUrl());
                        // replaces item's postURL with direct link to image/gif(mp4)
                        fixIndirectImgurUrl(item, imgurHash);
                    }

                    // image
                    if (item.getSubmissionType() == Constants.SubmissionType.IMAGE) {
                        GlideApp.load(item.getThumbnail())
                                .apply(new RequestOptions()
                                        .centerCrop()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                                .into(holder.thumbnailImageView);
                    }

                    // gif
                    else if (item.getSubmissionType() == Constants.SubmissionType.GIF) {
                        // Assume we're given .gifv link. Need to fetch .mp4 link from Imgur API
                        String imgurHash = Helpers.getImgurHash(item.getUrl());
                        getMp4LinkImgur(item,imgurHash);
                        //String hashTest = "4RxPsWI";
                       /* GlideApp
                                .load(thumbnailUrl)
                                .apply(new RequestOptions()
                                        .centerCrop()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                                .into(holder.thumbnailImageView);*/
                    }
                }
                //v.redd.it
                if ("v.redd.it".equalsIgnoreCase(item.getDomain())) {
                    Log.e("VIDEO_DOMAIN_FOUND", " Found v.redd.it link. Not working yet.");
                }

                // i.redd.it
                if (item.getDomain().contains("i.redd.it")) {

                    GlideApp
                            .load(item.getThumbnail())
                            .apply(new RequestOptions()
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                            .into(holder.thumbnailImageView);
                }
                //youtube
                if (item.getDomain().contains("youtube")) {
                    Log.e("VIDEO_DOMAIN_FOUND", " Found youtube link. Not working yet.");

                }
                //???
                else {
                    Log.e("DOMAIN_NOT_FOUND", "Domain not recognized: " + item.getDomain() + ". Position: " + position);

                    //TODO: For now, if we don't recognize the submission's domain we will assume it
                    //      has a thumbnail to display.
                    GlideApp
                            .load(item.getThumbnail())
                            .apply(new RequestOptions()
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                            .into(holder.thumbnailImageView);
                }

                /* open submission viewer when image clicked*/
                holder.thumbnailImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.onClick(holder.thumbnailImageView);
                    }
                });

                /* Long press hover previewer */
                if (mAllowImagePreview) {
                    holder.thumbnailImageView.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View pView) {
                            // prevent recyclerview from handling touch events, otherwise bad things happen
                            mRecyclerMain.setHandleTouchEvents(false);
                            isImageViewPressed = true;
                            if (mPreviewSize == Constants.HoverPreviewSize.SMALL) {
                                //popupWindow = new PopupWindow(customView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                                if(item.getSubmissionType() == Constants.SubmissionType.IMAGE){
                                    GlideApp
                                            .load(item.getUrl())
                                            .apply(new RequestOptions()
                                                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                                            .into(mHoverPreviewSmall);
                                    mHoverPreviewTitleSmall.setText(item.getTitle());
                                    mHoverPreviewContainerSmall.setVisibility(View.VISIBLE);
                                }
                                else if(item.getSubmissionType() == Constants.SubmissionType.GIF){

                                }


                                //qqq
                                // mPopupWindow = new PopupWindow(getActivity());

                                //mPopupWindow = new PopupWindow(mPopupView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                                // mPopupWindow.setContentView(mHoverPreviewContainerSmall);
                                //mPopupWindow.showAtLocation(mParentView, Gravity.CENTER,0,0);

                            } else if (mPreviewSize == Constants.HoverPreviewSize.LARGE) {
                                if(item.getSubmissionType() == Constants.SubmissionType.IMAGE) {
                                    GlideApp
                                            .load(item.getUrl())
                                            .apply(new RequestOptions()
                                                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                                            .into(mHoverPreviewLarge);
                                    mHoverPreviewTitleLarge.setText(item.getTitle());
                                    mHoverPreviewContainerLarge.setVisibility(View.VISIBLE);
                                }
                                else if(item.getSubmissionType() == Constants.SubmissionType.GIF) {
                                    initializePlayer(item.getUrl());
                                    mHoverPreviewTitleLarge.setText(item.getTitle());
                                    mHoverPreviewContainerLarge.setVisibility(View.VISIBLE);
                                }

                            }
                            return true;
                        }
                    });

                    holder.thumbnailImageView.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View pView, MotionEvent pEvent) {
                            pView.onTouchEvent(pEvent);
                            if (pEvent.getAction() == MotionEvent.ACTION_UP) {
                                // hide hoverView on click release
                                if (isImageViewPressed) {
                                    // done with hoverview, allow recyclerview to handle touch events
                                    mRecyclerMain.setHandleTouchEvents(true);
                                    isImageViewPressed = false;
                                    if (mPreviewSize == Constants.HoverPreviewSize.SMALL) {
                                        // mHoverPreviewTitleSmall.setText("");
                                        mHoverPreviewContainerSmall.setVisibility(View.GONE);
                                    } else if (mPreviewSize == Constants.HoverPreviewSize.LARGE) {
                                        // mHoverPreviewTitleSmall.setText("");
                                        mHoverPreviewContainerLarge.setVisibility(View.GONE);
                                    }
                                }
                            }
                            return false;
                        }
                    });

                }
            }


        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //TextView textView;
            ImageView thumbnailImageView;

            public ItemViewHolder(View itemView) {
                super(itemView);
                //textView = itemView.findViewById(R.id.textViewName);
                thumbnailImageView = (ImageView) itemView.findViewById(R.id.thumbnail);
            }

            @Override
            public void onClick(View view) {
                SubmissionObj submission = getItem(getLayoutPosition());
                mHomeEventListener.openMediaViewer(submission);
            }
        }
    }


    /*
        Takes indirect Imgur url such as https://imgur.com/7Ogk88I, fetches direct link from
        Imgur API, and sets item's URL to direct link.
        We also are setting the item's submission type here. Might need to do this elsewhere.
     */
    public void fixIndirectImgurUrl(SubmissionObj item, String imgurHash) {
        ImgurClient imgurClient = new ImgurClient();

        imgurClient.getImageService()
                .getImageByHash(imgurHash)
                .subscribeOn(Schedulers.io())
                .subscribe(new io.reactivex.Observer<SubmissionRoot>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(SubmissionRoot submissionRoot) {
                        ImgurSubmission imgurSubmissionData = submissionRoot.getImgurSubmissionData();
                        // ImgurSubmission can be one of two things:
                        // 1. Image. Will not contain any mp4 data
                        // 2. Gif. Will contain mp4 link

                        // image
                        if (imgurSubmissionData.getMp4() == null /*|| "".equalsIgnoreCase(imgurSubmissionData.getMp4())*/) {
                            item.setUrl(imgurSubmissionData.getLink());
                            item.setSubmissionType(Constants.SubmissionType.IMAGE);
                        }
                        // gif
                        else {
                            item.setUrl(imgurSubmissionData.getMp4());
                            item.setSubmissionType(Constants.SubmissionType.GIF);

                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    /*
        [IMGUR SPECIFIC FUNCTION]
        Given a imgur link ending with .gif/gifv such as https://i.imgur.com/4RxPsWI.gifv,
        retrieve corresponding .mp4 link: https://i.imgur.com/4RxPsWI.mp4 and set item's
        URL to new .mp4 link.
     */
    private void getMp4LinkImgur(SubmissionObj item, String imgurHash){
        ImgurClient imgurClient = new ImgurClient();

        imgurClient.getImageService()
                .getImageByHash(imgurHash)
                .subscribeOn(Schedulers.io())
                .subscribe(new io.reactivex.Observer<SubmissionRoot>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(SubmissionRoot submissionRoot) {
                        ImgurSubmission imgurSubmissionData = submissionRoot.getImgurSubmissionData();
                        item.setUrl(imgurSubmissionData.getMp4());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }


    private void initializePlayer(String url) {

        mExoplayerLarge.requestFocus();

        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);

        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);

        mExoplayerLarge.setPlayer(player);

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

        player.prepare(mediaSource, !haveStartPosition, false);

     /*   ivHideControllerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerView.hideController();
            }
        });*/
    }

    private class PlayerEventListener extends Player.DefaultEventListener{

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

}
