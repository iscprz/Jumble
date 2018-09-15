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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
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

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.app.Activity.RESULT_OK;

public class FragmentHome extends Fragment {
    public final static String TAG = Constants.TAG_FRAG_HOME;
    public final static int KEY_INTENT_GOTO_SUBMISSIONVIEWER = 1;
    private final static int KEY_LOG_IN = 2;

    private RequestManager GlideApp;
    private SwipeRefreshLayout mRefreshLayout;
    private MultiClickRecyclerView mRecyclerMain;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mNumDisplayColumns;
    private String mCurrSubreddit;
    private boolean isImageViewPressed = false;
    private int mActivePointerId = -1;
    private boolean isViewingSubmission = false;
    private GestureDetector mGestureDetector;
    private ProgressBar mProgressBar;

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
    private ImageView mHoverImagePreviewSmall;
    // private PopupWindow mPopupWindow;
    // private View mPopupView;

    // hover view large
    private RelativeLayout mHoverPreviewContainerLarge;
    private FrameLayout mHoverPreviewMediaContainerLarge;
    private TextView mHoverPreviewTitleLarge;
    private ImageView mHoverImagePreviewLarge;


    //exo player stuff
    private BandwidthMeter bandwidthMeter;
    private DefaultTrackSelector trackSelector;
    private SimpleExoPlayer player;
    private ProgressBar progressBar;
    private DataSource.Factory mediaDataSourceFactory;
    private int currentWindow;
    private long playbackPosition;
    private Timeline.Window window;
    //private FrameLayout mExoplayerContainerLarge;
    private PlayerView mExoplayerLarge;

    // event listeners
    private HomeEventListener mHomeEventListener;

    public interface HomeEventListener {
        public void openSettings();

        public void refreshFeed(String fragmentTag);

        public void isHome(boolean isHome);
    }

    public static FragmentHome newInstance() {
        return new FragmentHome();
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        unpackArgs();
        GlideApp = Glide.with(this);
        //  mRedditClient = App.getAccountHelper().isAuthenticated() ? App.getAccountHelper().getReddit() : App.getAccountHelper().switchToUserless();
        setHasOptionsMenu(true);
        prefs = getContext().getSharedPreferences(Constants.KEY_GETPREFS_SETTINGS, Context.MODE_PRIVATE);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        //mPopupView = inflater.inflate(R.layout.layout_popup_preview_small,null);
        //mParentView = v.findViewById(R.id.main_content);

        /* Initialize any preference/settings variables */
        validatePreferences();

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
        final RecyclerAdapter adapter = new RecyclerAdapter(getContext());

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

        /* for detecting click types when needed */
        mGestureDetector = new GestureDetector(getContext(), new SingleTapConfirm());

        /* Progress bar for loading images/gifs/videos*/
        mProgressBar = (ProgressBar) v.findViewById(R.id.hover_view_large_image_media_progress);

        /* hover view small*/
        mHoverPreviewContainerSmall = (RelativeLayout) v.findViewById(R.id.hover_view_container_small);
        mHoverPreviewTitleSmall = (TextView) v.findViewById(R.id.hover_view_title_small);
        mHoverImagePreviewSmall = (ImageView) v.findViewById(R.id.hover_imageview_small);

        /* hover view large*/
        mHoverPreviewContainerLarge = (RelativeLayout) v.findViewById(R.id.hover_view_container_large);
        mHoverPreviewMediaContainerLarge = (FrameLayout) v.findViewById(R.id.hover_view_large_image_container);
        mHoverPreviewTitleLarge = (TextView) v.findViewById(R.id.hover_view_title_large);
        mHoverImagePreviewLarge = (ImageView) v.findViewById(R.id.hover_imageview_large);

        /* Exo player */
        //mExoplayerContainerLarge = (FrameLayout) v.findViewById(R.id.container_exoplayer_large);
        mExoplayerLarge = (PlayerView) v.findViewById(R.id.exoplayer_large);

        bandwidthMeter = new DefaultBandwidthMeter();
        mediaDataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "Moxie"), (TransferListener<? super DataSource>) bandwidthMeter);
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
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        //toolbar setup
        setupToolbar();

        // Check if user settings have been altered.
        // i.e. User went to settings, opted in to NSFW posts then navigated back.
        validatePreferences();
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseExoPlayer();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mToolbar.setAlpha(0);

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseExoPlayer();
    }

    @Override
    public void onDestroyOptionsMenu() {
        super.onDestroyOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // hacky workaround for handling conflict with opening drawer
                // instead of going back when viewing a submission
                if (!isViewingSubmission) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                } else {
                    goBack();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /* Returned from SubmissionViewer*/
        if (requestCode == KEY_INTENT_GOTO_SUBMISSIONVIEWER) {
            if (resultCode == RESULT_OK) {
                isViewingSubmission = false;
                // Lets the activity know we're back home so it can handle onBackPress()
                mHomeEventListener.isHome(true);
                mToolbar.setAlpha(1);
            }
        }

        if (requestCode == KEY_LOG_IN) {
            if (resultCode == RESULT_OK) {

            }
        }
    }

    private void setupToolbar() {
        if(isAdded()){
            mToolbar.setVisibility(View.VISIBLE);
            mToolbar.setAlpha(1);
            mToolbar.setTitle(getResources().getString(R.string.subreddit_prefix) + mCurrSubreddit);

            // set hamburger menu icon
            ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if(toolbar != null){
                toolbar.setDisplayHomeAsUpEnabled(true);
                toolbar.setHomeAsUpIndicator(R.drawable.ic_menu);
            }
        }


    }

    private void unpackArgs() {
        // SharedPreferencesTokenStore tokenStore = App.getTokenStore();
        Bundle arguments = this.getArguments();
        try {
            if (arguments != null) {
                //mNumDisplayColumns = (Integer) arguments.get(ARGS_NUM_DISPLAY_COLS);
            }
            //  mCurrSubreddit = mRedditClient.getmRedditDataRequestObj().getmSubreddit();
        } catch (NullPointerException npe) {
            throw new NullPointerException("Null ptr exception trying to unpack arguments in " + TAG);
        }

        // default to /r/pics as failsafe if nothing was passed to us
        mCurrSubreddit = App.getCurrSubredditObj().getSubreddit();
        if (mCurrSubreddit == null || "".equals(mCurrSubreddit)) {
            mCurrSubreddit = Constants.DEFAULT_SUBREDDIT;
        }
    }

    /* Hamburger menu*/
    private void setupHamburgerMenu(View v) {
        mDrawerLayout = v.findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                /*  R.drawable.ic_menu,  nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                setupToolbar();
                //getActivity().getSupportActionBar().setTitle(mTitle);
                //supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                setupToolbar();
                //supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);


        NavigationView navigationView = (NavigationView) v.findViewById(R.id.nav_view);
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
    }

    /* Handles left navigation menu item selections*/
    private void handleNavItemSelection(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
           /* case R.id.nav_log_in:
                Intent loginIntent = new Intent(getContext(), ActivityLogin.class);
                //unlockSessionIntent.putExtra("REQUEST_UNLOCK_SESSION", true);
                startActivityForResult(loginIntent,KEY_LOG_IN);
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
                        String requestedSubreddit = input.getText().toString();
                        App.getCurrSubredditObj().setSubreddit(requestedSubreddit);

                        Intent visitSubredditIntent = new Intent(getContext(), ActivitySubredditViewer.class);
                        visitSubredditIntent.putExtra(Constants.EXTRA_GOTO_SUBREDDIT, requestedSubreddit);
                        startActivity(visitSubredditIntent);
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

    /*
        Hover preview set up. Hides any views that aren't needed and unhides the ones we need.
     */
    private void setupPreviewer(SubmissionObj item) {
        mProgressBar.setVisibility(View.VISIBLE);
        if (mPreviewSize == Constants.HoverPreviewSize.SMALL) {
            mHoverPreviewContainerSmall.setVisibility(View.VISIBLE);
            mHoverPreviewContainerLarge.setVisibility(View.GONE);
            //popupWindow = new PopupWindow(customView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            if (item.getSubmissionType() == Constants.SubmissionType.IMAGE) {
                //mHoverPreviewTitleSmall.setText(item.getTitle());
                mHoverImagePreviewSmall.setVisibility(View.VISIBLE);
            }
            if(item.getSubmissionType() == Constants.SubmissionType.GIF){ // and video?

            }
        } else if (mPreviewSize == Constants.HoverPreviewSize.LARGE) {
            mHoverPreviewContainerLarge.setVisibility(View.VISIBLE);
            mHoverPreviewContainerSmall.setVisibility(View.GONE);
            // fade the toolbar while we're in large previewer
            mToolbar.setAlpha(.1f);
            mHoverPreviewMediaContainerLarge.setVisibility(View.VISIBLE);
            if (item.getSubmissionType() == Constants.SubmissionType.IMAGE) {
                mHoverImagePreviewLarge.setVisibility(View.VISIBLE);
                mExoplayerLarge.setVisibility(View.GONE);
            }
            if(item.getSubmissionType() == Constants.SubmissionType.GIF){ // and video?
                initializePlayer(item.getUrl());
                mExoplayerLarge.setVisibility(View.VISIBLE);
                mHoverImagePreviewLarge.setVisibility(View.GONE);

            }
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
        mNumDisplayColumns = 3;//prefs.getInt(Constants.SETTINGS_NUM_DISPLAY_COLS);
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


        RecyclerAdapter(Context mContext) {
            super(DIFF_CALLBACK);
            this.mContext = mContext;
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
            // Waiting for API response
            String thumbnail = Constants.THUMBNAIL_NOT_FOUND;
            item.setSubmissionType(Helpers.getSubmissionType(item.getUrl()));

            if (item != null && !item.isSelfPost()) {
                // Imgur
                if (item.getDomain().contains("imgur")) {
                    // try setting the submission type here. Imgur links will have .jpg/.gifv
                    // appended to them if linked directly. Don't worry about indirect links here.

                    // Check if submission type is null. This will happen if the item's URL is
                    // to a non-direct image/gif link such as https://imgur.com/qTadRtq
                    if (item.getSubmissionType() == null) {
                        String imgurHash = Helpers.getImgurHash(item.getUrl());
                        // Async call to Imgur API
                        fixIndirectImgurUrl(item, imgurHash);
                    }

                    // imgur image
                    if (item.getSubmissionType() == Constants.SubmissionType.IMAGE) {
                    }
                    // imgur gif
                    else if (item.getSubmissionType() == Constants.SubmissionType.GIF) {
                        // Assume we're given .gifv link. Need to fetch .mp4 link from Imgur API
                        String imgurHash = Helpers.getImgurHash(item.getUrl());
                        getMp4LinkImgur(item, imgurHash);
                    }
                    // We assume item will always have a thumbnail in an image format
                    thumbnail = item.getThumbnail();
                }
                //v.redd.it
                else if ("v.redd.it".equalsIgnoreCase(item.getDomain())) {
                    Log.e("VIDEO_DOMAIN_FOUND", " Found v.redd.it link. Not working yet.");
                    thumbnail = item.getThumbnail();
                }

                // i.redd.it
                else if (item.getDomain().contains("i.redd.it")) {
                    thumbnail = item.getThumbnail();
                }
                //gfycat
                else if (item.getDomain().contains("gfycat")) {
                    thumbnail = item.getThumbnail();
                }
                //youtube
                else if (item.getDomain().contains("youtube")) {
                    Log.e("VIDEO_DOMAIN_FOUND", " Found YOUTUBE link. Not working yet.");
                }
                // Domain not recognized - hope submission is linked to a valid media extension
                else {
                    thumbnail = item.getThumbnail();

                   /* if (item.getSubmissionType() == Constants.SubmissionType.IMAGE) {
                        thumbnail = item.getUrl();
                    }

                    //TODO: Test what happens when we encounter weird domain linked to gif/video
                    else if (item.getSubmissionType() == Constants.SubmissionType.GIF
                            || item.getSubmissionType() == Constants.SubmissionType.VIDEO) {
                        thumbnail = item.getUrl();
                    }*/
                    Log.e("DOMAIN_NOT_FOUND", "Domain not recognized: " + item.getDomain() + ". Position: " + position);
                }

                //TODO: handle Constants.THUMBNAIL_NOT_FOUND

                // Finally load thumbnail into recyclerview
                //loadThumbNailIntoRV(thumbnail,holder);
                GlideApp.load(thumbnail)
                        .apply(new RequestOptions().centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(holder.thumbnailImageView);


                /* open submission viewer when image clicked*/
                holder.thumbnailImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        holder.onClick(holder.thumbnailImageView);
                    }
                });

                /* Long press hover previewer */
                holder.thumbnailImageView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View pView) {
                        // do nothing if previewing has been disabled through settings
                        if (!mAllowImagePreview) {
                            return true;
                        }
                        // prevent recyclerview from handling touch events, otherwise bad things happen
                        mRecyclerMain.setHandleTouchEvents(false);
                        isImageViewPressed = true;

                        if (mPreviewSize == Constants.HoverPreviewSize.SMALL) {
                            mHoverPreviewTitleSmall.setText(item.getTitle());
                            //popupWindow = new PopupWindow(customView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                            if (item.getSubmissionType() == Constants.SubmissionType.IMAGE) {
                                setupPreviewer(item);
                                GlideApp
                                        .load(item.getUrl())
                                        .apply(new RequestOptions()
                                                .diskCacheStrategy(DiskCacheStrategy.ALL))
                                        .into(mHoverImagePreviewSmall);
                            } else if (item.getSubmissionType() == Constants.SubmissionType.GIF) {

                            }


                            //qqq
                            // mPopupWindow = new PopupWindow(getActivity());

                            //mPopupWindow = new PopupWindow(mPopupView, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                            // mPopupWindow.setContentView(mHoverPreviewContainerSmall);
                            //mPopupWindow.showAtLocation(mParentView, Gravity.CENTER,0,0);

                        } else if (mPreviewSize == Constants.HoverPreviewSize.LARGE) {
                            mHoverPreviewTitleLarge.setText(item.getTitle());
                            setupPreviewer(item);
                            if (item.getSubmissionType() == Constants.SubmissionType.IMAGE) {
                                GlideApp.load(item.getUrl())
                                        .listener(new ProgressBarRequestListener(mProgressBar))
                                        /*.apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))*/
                                        .into(mHoverImagePreviewLarge);
                                // make sure the gif/video player isn't showing
                            } else if (item.getSubmissionType() == Constants.SubmissionType.GIF) {
                                //TODO: Settings option to disable exoplayer controller?
                                // set up exoplayer to play gif
                                initializePlayer(item.getUrl());
                            }
                        }
                        return true;
                    }
                });

                holder.thumbnailImageView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View pView, MotionEvent pEvent) {
                        boolean singleTap = mGestureDetector.onTouchEvent(pEvent);
                        // do nothing if previewing has been disabled through settings
                        if (!mAllowImagePreview && !singleTap) {
                            return true;
                        }
                        // save ID of the first pointer(touch) ID
                        mActivePointerId = pEvent.getPointerId(0);

                        // find current touch ID
                        final int action = pEvent.getAction();
                        int currPointerId = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

                        // don't care about touches that aren't the first touch
                        if (currPointerId != mActivePointerId) {
                            return true;
                        }
                        // only care about doing stuff that relates to first finger touch
                        if (pEvent.getAction() == MotionEvent.ACTION_UP) {
                            // hide hoverView on click release
                            if (isImageViewPressed) {
                                // done with hover view, allow recyclerview to handle touch events
                                mRecyclerMain.setHandleTouchEvents(true);
                                isImageViewPressed = false;
                                if (mPreviewSize == Constants.HoverPreviewSize.SMALL) {
                                    mHoverPreviewContainerSmall.setVisibility(View.GONE);

                                } else if (mPreviewSize == Constants.HoverPreviewSize.LARGE) {
                                    mHoverPreviewContainerLarge.setVisibility(View.GONE);
                                    //mExoplayerContainerLarge.setVisibility(View.GONE);
                                    // restore the toolbar
                                    mToolbar.setAlpha(1);
                                }
                                //releaseExoPlayer();
                            }
                        }
                        return false;
                    }
                });
            }
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //TextView textView;
            ImageView thumbnailImageView;

            public ItemViewHolder(View itemView) {
                super(itemView);
                thumbnailImageView = (ImageView) itemView.findViewById(R.id.thumbnail);
            }

            @Override
            public void onClick(View view) {
                SubmissionObj submission = getItem(getLayoutPosition());

                // prevent any weird double clicks from opening two submission viewers
                if (!isViewingSubmission) {
                    openSubmissionViewer(submission);
                    isViewingSubmission = true;
                    // Let our activity know we're no longer home. This prevents this fragment's
                    // activity from handling onBackPress() which would exit the app
                    mHomeEventListener.isHome(false);
                }
            }
        }
    }

    private void openSubmissionViewer(SubmissionObj submission) {
        if(isAdded()) {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            // Fragment mediaDisplayerFragment = (FragmentSubmissionViewer) fm.findFragmentByTag(Constants.TAG_FRAG_MEDIA_DISPLAY);
            FragmentTransaction ft = fm.beginTransaction();
            Fragment mediaDisplayerFragment = FragmentSubmissionViewer.newInstance();
            Bundle args = new Bundle();
            args.putSerializable(Constants.EXTRA_SUBMISSION_OBJ, submission);
            mediaDisplayerFragment.setArguments(args);

            mediaDisplayerFragment.setTargetFragment(FragmentHome.this, KEY_INTENT_GOTO_SUBMISSIONVIEWER);

            //ft.replace(R.id.fragment_container_home, mediaDisplayerFragment,Constants.TAG_FRAG_MEDIA_DISPLAY);
            int parentContainerId = ((ViewGroup)getView().getParent()).getId();
            ft.add(parentContainerId, mediaDisplayerFragment/*, Constants.TAG_FRAG_MEDIA_DISPLAY*/);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    /* Solution to closing the submission viewer instead of opening left drawerlayout*/
    private void goBack() {
        try {
            FragmentManager fm = getActivity().getSupportFragmentManager();
            fm.popBackStack();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     *   Hacky workaround for detecting single clicks. This was added to fix the issue where
     *   submissions would not open when clicked if "preview on long touch" option was disabled.
     *   (onTouchListener() was being called before onClick() every time.)
     */
    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            /*it needs to return true if we don't want
            to ignore rest of the gestures*/
            return true;
        }
    }

    /*
        [IMGUR SPECIFIC FUNCTION]
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
                        //item.setLoadingData(false);
                    }
                });
    }

    /*
        [IMGUR SPECIFIC FUNCTION]
        Given a imgur link ending with .gif/gifv such as https://i.imgur.com/4RxPsWI.gifv,
        retrieve corresponding .mp4 link: https://i.imgur.com/4RxPsWI.mp4 and set item's
        URL to new .mp4 link.
     */
    private void getMp4LinkImgur(SubmissionObj item, String imgurHash) {
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



    /* Exoplayer */

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

    public void releaseExoPlayer() {
        if (player != null) {
            player.release();
        }
    }
}
