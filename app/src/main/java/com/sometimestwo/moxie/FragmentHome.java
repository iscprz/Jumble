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
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
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
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.Util;
import com.makeramen.roundedimageview.RoundedImageView;
import com.sometimestwo.moxie.Model.ExpandableMenuModel;
import com.sometimestwo.moxie.Model.Explore;
import com.sometimestwo.moxie.Model.SubmissionObj;
import com.sometimestwo.moxie.Utils.Constants;
import com.sometimestwo.moxie.Utils.Utils;

import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class FragmentHome extends Fragment {
    public final static String TAG = Constants.TAG_FRAG_HOME;


    private int mScreenWidth;
    private int mScreenHeight;
    private RequestManager GlideApp = App.getGlideApp();
    private SwipeRefreshLayout mRefreshLayout;
    private MultiClickRecyclerView mRecyclerHome;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationViewLeft;
    private NavigationView mNavigationViewRight;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mCurrSubreddit;
    private String mCurrExploreTitle;
    private boolean isImageViewPressed = false;
    private int mActivePointerId = -1;
    private GestureDetector mGestureDetector;
    private ProgressBar mPreviewerProgressBar;
    private ProgressBar mProgressbarMain;
    private boolean mInvalidateDataSource = false;
    private boolean is404 = false;

    // User's preferences. Initialize with default values for safety
    SharedPreferences prefs_settings;
    private boolean mHideNSFW = true;
    private boolean mAllowImagePreview = false;
    private boolean mAllowBigDisplayClickClose = true;
    private boolean mDisplayDomainIcon = false;
    private boolean mHideNSFWThumbs = false;
    private boolean mDisplayFiletypeIcons = false;
    private boolean mDisplayNSFWIcon = false;
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
    private TextView mHoverPreviewSubredditLarge;
    private ImageView mHoverImagePreviewLarge;
    // private GfycatPlayer mHoverPreviewGfycatLarge;

    /* Left navigation view */
    private LinearLayout mNavViewHeader;
    private TextView mNavViewHeaderTitle;
    private ImageView mNavViewDropDown;
    ExpandableListAdapter expandableListAdapter;
    ExpandableListView expandableListView;
    List<ExpandableMenuModel> headerList = new ArrayList<>();
    HashMap<ExpandableMenuModel, List<ExpandableMenuModel>> childList = new HashMap<>();

    /* Right navigation view*/
    private RecyclerView mExploreRecyclerView;
    // maps an explore category to a background image uri
    private Map<String, Explore> mExploreCatagoriesMap;
    // holds a list of the "Explore" catagories in memory
    private List<String> mExploreCatagoriesList;

    // log out button
    private TextView mButtonLogout;

    //exo player stuff
    private BandwidthMeter bandwidthMeter;
    private DefaultTrackSelector trackSelector;
    // Exoplayer is used to play streaming(as opposed to downloaded) .mp4 files.
    // .gifv links will be converted to .mp4 links and played with exoplayer.
    private SimpleExoPlayer player;
    private ProgressBar exoplayerProgressbar;
    private DataSource.Factory mediaDataSourceFactory;
    private int currentWindow;
    private long playbackPosition;
    private Timeline.Window window;
    //private FrameLayout mExoplayerContainerLarge;
    private PlayerView mExoplayerLarge;
    // Videoview is used to play downloaded(as opposed to streaming) .mp4 files.
    // VReddit links will be downloaded and played using VideoView.
    private VideoView mPreviewerVideoViewLarge;
    // event listeners
    private HomeEventListener mHomeEventListener;


    public static FragmentHome newInstance() {
        return new FragmentHome();
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        prefs_settings = getContext().getSharedPreferences(Constants.KEY_GET_PREFS_SETTINGS, Context.MODE_PRIVATE);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Ensure Reddit Client is authenticated before proceeding
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        /* Set screen dimensions for resizing dialogs */
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;

        /* Initialize any preference/settings variables */
        try {
            validatePreferences();
        } catch (Exception e) {
            //TODO: What to do when preferences aren't found? Will this ever happen? (prob not)
            e.printStackTrace();
        }

        unpackArgs();

        /*Drawer layout config */
        setupDrawerLayout(v);

        /* Navigation menu on left*/
        setupLeftNavView(v);

        /* Navigation menu on right*/
        setupRightNavView(v);

        /* Toolbar setup*/
        mToolbar = (Toolbar) v.findViewById(R.id.toolbar_main);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);


        /* Refresh layout setup*/
        mRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.home_refresh_layout);
        mRefreshLayout.setDistanceToTriggerSync(Constants.REFRESH_PULL_TOLERANCE);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FragmentHome.this.refresh(true);
            }
        });

        /* Recycler view setup*/
        mRecyclerHome = (MultiClickRecyclerView) v.findViewById(R.id.recycler_submissions);
        mRecyclerHome.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mRecyclerHome.setHasFixedSize(true);

        /* RecyclerView adapter stuff */
        final SubredditContentRecyclerAdapter adapter = new SubredditContentRecyclerAdapter(getContext());

        /* Viewmodel fetching and data updating */
        if (mInvalidateDataSource) {
            // This is true when we are refreshing a page whether it be from a sipe refresh,
            // a new user log in, or user log out
            invalidateData();
        }
        SubmissionsViewModel submissionsViewModel = ViewModelProviders.of(this).get(SubmissionsViewModel.class);
        submissionsViewModel.postsPagedList.observe(getActivity(), new Observer<PagedList<SubmissionObj>>() {
            @Override
            public void onChanged(@Nullable PagedList<SubmissionObj> items) {
                // submitting changes to adapter, if any
                adapter.submitList(items);
            }
        });

        if (isAdded()) {
            mRecyclerHome.setAdapter(adapter);
        }

        /* for detecting click types when needed */
        mGestureDetector = new GestureDetector(getContext(), new SingleTapConfirm());

        mProgressbarMain = (ProgressBar) v.findViewById(R.id.progress_bar_home);
        /* Progress bar for loading images/gifs/videos*/
        mPreviewerProgressBar = (ProgressBar) v.findViewById(R.id.hover_view_large_image_media_progress);

        /* hover view small*/
        mHoverPreviewContainerSmall = (RelativeLayout) v.findViewById(R.id.hover_view_container_small);
        mHoverPreviewTitleSmall = (TextView) v.findViewById(R.id.hover_view_title_small);
        mHoverImagePreviewSmall = (ImageView) v.findViewById(R.id.hover_imageview_small);

        /* hover view large*/
        mHoverPreviewContainerLarge = (RelativeLayout) v.findViewById(R.id.hover_view_container_large);
        mHoverPreviewMediaContainerLarge = (FrameLayout) v.findViewById(R.id.hover_view_large_image_container);
        //mHoverPreviewTitleLarge = (TextView) v.findViewById(R.id.hover_view_title_large);
        //mHoverPreviewSubredditLarge = (TextView) v.findViewById(R.id.hover_view_textview_subreddit);
        mHoverImagePreviewLarge = (ImageView) v.findViewById(R.id.large_previewer_imageview);

        /* Exo player */
        //mExoplayerContainerLarge = (FrameLayout) v.findViewById(R.id.container_exoplayer_large);
        mExoplayerLarge = (PlayerView) v.findViewById(R.id.large_previewer_exoplayer);
        mPreviewerVideoViewLarge = (VideoView) v.findViewById(R.id.large_previewer_video_view);

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
        try {
            // Check if user settings have been altered.
            // i.e. User went to settings, opted in to NSFW posts then navigated back.
            validatePreferences();
            setupToolbar();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_submissions_sortby).setVisible(true);
        menu.findItem(R.id.menu_explore).setVisible(true);
        menu.findItem(R.id.menu_comments_sortby).setVisible(false);


        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SubredditSort sortBy;
        TimePeriod timePeriod;
        switch (item.getItemId()) {
            case android.R.id.home:
                mHomeEventListener.menuGoBack();
                return true;
            case R.id.menu_explore:
                mDrawerLayout.openDrawer(GravityCompat.END);
                return true;
            /* Sort by*/
            case R.id.menu_submissions_sortby_HOT:
                App.getMoxieInfoObj().setmSortBy(SubredditSort.HOT);
                App.getMoxieInfoObj().setmTimePeriod(null);
                refresh(true);
                return true;
            case R.id.menu_submissions_sortby_NEW:
                App.getMoxieInfoObj().setmSortBy(SubredditSort.NEW);
                App.getMoxieInfoObj().setmTimePeriod(null);
                refresh(true);
                return true;
            case R.id.menu_submissions_sortby_RISING:
                App.getMoxieInfoObj().setmSortBy(SubredditSort.RISING);
                App.getMoxieInfoObj().setmTimePeriod(null);
                refresh(true);
                return true;
            case R.id.menu_submissions_sortby_TOP_hour:
                App.getMoxieInfoObj().setmSortBy(SubredditSort.TOP);
                App.getMoxieInfoObj().setmTimePeriod(TimePeriod.HOUR);
                refresh(true);
                return true;

            case R.id.menu_submissions_sortby_TOP_today:
                App.getMoxieInfoObj().setmSortBy(SubredditSort.TOP);
                App.getMoxieInfoObj().setmTimePeriod(TimePeriod.DAY);
                refresh(true);
                return true;

            case R.id.menu_submissions_sortby_TOP_week:
                App.getMoxieInfoObj().setmSortBy(SubredditSort.TOP);
                App.getMoxieInfoObj().setmTimePeriod(TimePeriod.WEEK);
                refresh(true);
                return true;

            case R.id.menu_submissions_sortby_TOP_month:
                App.getMoxieInfoObj().setmSortBy(SubredditSort.TOP);
                App.getMoxieInfoObj().setmTimePeriod(TimePeriod.MONTH);
                refresh(true);
                return true;

            case R.id.menu_submissions_sortby_TOP_year:
                App.getMoxieInfoObj().setmSortBy(SubredditSort.TOP);
                App.getMoxieInfoObj().setmTimePeriod(TimePeriod.YEAR);
                refresh(true);
                return true;

            case R.id.menu_submissions_sortby_TOP_alltime:
                App.getMoxieInfoObj().setmSortBy(SubredditSort.TOP);
                App.getMoxieInfoObj().setmTimePeriod(TimePeriod.ALL);
                refresh(true);
                return true;
            case R.id.menu_submissions_sortby_CONTROVERSIAL_hour:
                App.getMoxieInfoObj().setmSortBy(SubredditSort.CONTROVERSIAL);
                App.getMoxieInfoObj().setmTimePeriod(TimePeriod.HOUR);
                refresh(true);
                return true;
            case R.id.menu_submissions_sortby_CONTROVERSIAL_today:
                App.getMoxieInfoObj().setmSortBy(SubredditSort.CONTROVERSIAL);
                App.getMoxieInfoObj().setmTimePeriod(TimePeriod.DAY);
                refresh(true);
                return true;
            case R.id.menu_submissions_sortby_CONTROVERSIAL_week:
                App.getMoxieInfoObj().setmSortBy(SubredditSort.CONTROVERSIAL);
                App.getMoxieInfoObj().setmTimePeriod(TimePeriod.WEEK);
                refresh(true);
                return true;
            case R.id.menu_submissions_sortby_CONTROVERSIAL_month:
                App.getMoxieInfoObj().setmSortBy(SubredditSort.CONTROVERSIAL);
                App.getMoxieInfoObj().setmTimePeriod(TimePeriod.MONTH);
                refresh(true);
                return true;
            case R.id.menu_submissions_sortby_CONTROVERSIAL_year:
                App.getMoxieInfoObj().setmSortBy(SubredditSort.CONTROVERSIAL);
                App.getMoxieInfoObj().setmTimePeriod(TimePeriod.YEAR);
                refresh(true);
                return true;
            case R.id.menu_submissions_sortby_CONTROVERSIAL_alltime:
                App.getMoxieInfoObj().setmSortBy(SubredditSort.CONTROVERSIAL);
                App.getMoxieInfoObj().setmTimePeriod(TimePeriod.ALL);
                refresh(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /* Returned from SubmissionViewer*/
        if (requestCode == Constants.REQUESTCODE_GOTO_BIG_DISPLAY) {
            if (resultCode == RESULT_OK) {
                //mToolbar.setAlpha(1);
            }
        } else if (requestCode == Constants.REQUESTCODE_GOTO_SUBREDDIT_VIEWER) {
            if (resultCode == Constants.RESULT_OK_START_OVER) {
                mHomeEventListener.startOver();
            }
        } else if (requestCode == Constants.REQUESTCODE_GOTO_LOG_IN) {
            if (resultCode == RESULT_OK) {
                // User successfully logged in. Update the current user.
                updateCurrentUser(App.getTokenStore().getUsernames().size() - 1);
                // empty the subreddit stack since we're starting over with new user
                while (!App.getMoxieInfoObj().getmSubredditStack().isEmpty()) {
                    App.getMoxieInfoObj().getmSubredditStack().pop();
                }
                refresh(true);
            }
        }
    }

    private void setupToolbar() {
        if (isAdded()) {
            mToolbar.setVisibility(View.VISIBLE);
            mToolbar.setAlpha(1);

            // Displaying Explore category
            if (mCurrExploreTitle != null) {
                mToolbar.setTitle(mCurrExploreTitle);
                //mToolbar.setTitleTextColor(getResources().getColor(R.color.colorAccentBlue));
                mToolbar.setTitleTextAppearance(getContext(), R.style.toolbar_title_text_explore);
            }
            // Displaying a subreddit
            else if (mCurrSubreddit != null) {
                mToolbar.setTitle(getResources().getString(R.string.subreddit_prefix) + mCurrSubreddit);
                mToolbar.setTitleTextAppearance(getContext(), R.style.toolbar_title_text_default);
            }
            // Displaying user's frontpage
            else {
                mToolbar.setTitle(getResources().getString(R.string.frontpage));
                mToolbar.setTitleTextAppearance(getContext(), R.style.toolbar_title_text_default);
            }

            // Subtitle - if sort by is null, getter will default to HOT
            mToolbar.setSubtitle(Utils.makeTextCute(App.getMoxieInfoObj().getmSortBy().toString()));


            // set hamburger menu icon if viewing a subreddit, back arrow if viewing submission
            ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (toolbar != null) {
                toolbar.setDisplayHomeAsUpEnabled(true);
                toolbar.setHomeAsUpIndicator(mCurrSubreddit == null ? R.drawable.ic_menu : R.drawable.ic_white_back_arrow);
            }
        }
    }

    private void unpackArgs() {
        // SharedPreferencesTokenStore tokenStore = App.getTokenStore();
        Bundle arguments = this.getArguments();
        try {
            if (arguments != null) {
                mInvalidateDataSource = (boolean) arguments.getBoolean(Constants.ARGS_INVALIDATE_DATASOURCE);
                mCurrSubreddit = (String) arguments.getString(Constants.ARGS_CURR_SUBREDDIT, null);
                mCurrExploreTitle = (String) arguments.getString(Constants.EXTRA_GOTO_EXPLORE_CATEGORY, null);
            }
            //  mCurrSubreddit = mRedditClient.getmRedditDataRequestObj().getmSubreddit();
        } catch (NullPointerException npe) {
            throw new NullPointerException("Null ptr exception trying to unpack arguments in " + TAG);
        }
    }

    /* Drawerlayout config to handle navigation views */
    private void setupDrawerLayout(View v) {
        mDrawerLayout = v.findViewById(R.id.drawer_layout);
        // Lock the left nav drawer unless we're home. This is to prevent unexpected behavior
        // such as what would happen if we logged in as a new user in the middle of some transaction.
        // FYI: We already have a "startOver()" method that starts us over from home in case we ever
        // want to handle that condition. Let's just avoid it for now.
        if (!(getActivity() instanceof ActivityHome)
                || getActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.START);
        }
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                /*  R.drawable.ic_menu,  nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                //supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                //supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    /* Left drawer/navigation view */
    private void setupLeftNavView(View v) {

        mNavigationViewLeft = (NavigationView) v.findViewById(R.id.nav_view_left);

        /* expandable nav */
        expandableListView = v.findViewById(R.id.expandable_list_left);
        prepareLeftMenuData();
        populateExpandableList();
        // Expand usernames
        expandableListView.expandGroup(0);

        /* Navigation view menu */
        Menu navViewMenu = mNavigationViewLeft.getMenu();

        /* Navigation menu header*/
        View navViewHeader = mNavigationViewLeft.getHeaderView(0);
        setupNavViewHeader(navViewHeader);

        /* Log out button */
        mButtonLogout = (TextView) v.findViewById(R.id.navbar_button_logout);
        // hide logout button if we're in Guest mode
        mButtonLogout.setVisibility(Constants.USERNAME_USERLESS
                .equalsIgnoreCase(App.getAccountHelper().getReddit().getAuthManager().currentUsername())
                ? View.GONE : View.VISIBLE);
        mButtonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmLogout(App.getAccountHelper().getReddit().getAuthManager().currentUsername());
            }
        });
    }

    private void setupRightNavView(View v) {
        mNavigationViewRight = (NavigationView) v.findViewById(R.id.nav_view_right);

        // set up spinner(header)
        Spinner spinner = (Spinner) mNavigationViewRight.getHeaderView(0).findViewById(R.id.navview_right_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.navview_right_dropdown_array, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        // set up all the "Explore" options.
        mExploreRecyclerView = (RecyclerView) v.findViewById(R.id.navview_right_explore_recycler);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        mExploreRecyclerView.setLayoutManager(gridLayoutManager);
        mExploreRecyclerView.setHasFixedSize(true);
        initExploreCatagories();
        if (isAdded()) {
            mExploreRecyclerView.setAdapter(new ExploreGridRecyclerAdapter());
        }
    }

    private class ExploreItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private RoundedImageView mExploreImage;
        private TextView mExploreTitle;

        public ExploreItemHolder(View itemView) {
            super(itemView);
            mExploreImage = (RoundedImageView) itemView.findViewById(R.id.explore_image);
            mExploreTitle = (TextView) itemView.findViewById(R.id.explore_title);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //onAlbumClick(getAdapterPosition());
        }
    }

    private class ExploreGridRecyclerAdapter extends RecyclerView.Adapter<ExploreItemHolder> {
        public ExploreGridRecyclerAdapter() {
        }

        @NonNull
        @Override
        public ExploreItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.explore_grid_item, viewGroup, false);
            return new ExploreItemHolder(view);
        }

        @NonNull
        @Override
        public void onBindViewHolder(@NonNull final ExploreItemHolder exploreItemGridItem, int position) {
            String category = mExploreCatagoriesList.get(position);

            exploreItemGridItem.mExploreTitle.setText(category);
            String bgUri = "android.resource://com.sometimestwo.moxie/"
                    + mExploreCatagoriesMap.get(category).getBgDrawableId();
            GlideApp.load(Uri.parse(bgUri))
                    .apply(new RequestOptions()
                            .centerInside()
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(exploreItemGridItem.mExploreImage);

            exploreItemGridItem.mExploreImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String category = mExploreCatagoriesList.get(position);
                    List<String> subreddits = mExploreCatagoriesMap.get(category).getSubredditList();

                    StringBuilder sb = new StringBuilder();
                    for (String subreddit : subreddits) {
                        sb.append(subreddit).append('+');
                    }

                    String exploreURL = sb.substring(0, sb.length() - 1);
                    App.getMoxieInfoObj().getmSubredditStack().push(exploreURL);

                    Intent visitSubredditIntent = new Intent(getContext(), ActivitySubredditViewer.class);
                    visitSubredditIntent.putExtra(Constants.EXTRA_GOTO_SUBREDDIT, exploreURL);
                    visitSubredditIntent.putExtra(Constants.EXTRA_GOTO_EXPLORE_CATEGORY, category);
                    startActivityForResult(visitSubredditIntent, Constants.REQUESTCODE_GOTO_SUBREDDIT_VIEWER);
                    mDrawerLayout.closeDrawer(mNavigationViewRight);
                }
            });

            exploreItemGridItem.mExploreTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    exploreItemGridItem.mExploreImage.callOnClick();
                }
            });

            //Long click listeners for future use
           /* exploreItemGridItem.mExploreImage.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return false;
                }
            });

            exploreItemGridItem.mExploreTitle.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return false;
                }
            });*/
        }

        @Override
        public int getItemCount() {
            return mExploreCatagoriesList.size();
        }
    }

    private void initExploreCatagories() {
        //store these values in a list in memory for convenience
        mExploreCatagoriesList = Arrays.asList(getResources().getStringArray(R.array.explore_catagories));
        // maps an explore category to an image file that will follow the following naming
        // convention: explore_bg_category, where category is the explore category
        mExploreCatagoriesMap = new HashMap<>();
        mExploreCatagoriesMap.put("Funny", new Explore(R.drawable.explore_bg_funny, Arrays.asList(getResources().getStringArray(R.array.explore_subreddits_funny))));
        mExploreCatagoriesMap.put("Awwwww", new Explore(R.drawable.explore_bg_aww, Arrays.asList(getResources().getStringArray(R.array.explore_subreddits_aww))));
        mExploreCatagoriesMap.put("Travel", new Explore(R.drawable.explore_bg_travel, Arrays.asList(getResources().getStringArray(R.array.explore_subreddits_travel))));
        mExploreCatagoriesMap.put("Meme", new Explore(R.drawable.explore_bg_meme, Arrays.asList(getResources().getStringArray(R.array.explore_subreddits_meme))));
        mExploreCatagoriesMap.put("GIFs", new Explore(R.drawable.explore_bg_gifs, Arrays.asList(getResources().getStringArray(R.array.explore_subreddits_gifs))));
        mExploreCatagoriesMap.put("Food", new Explore(R.drawable.explore_bg_food, Arrays.asList(getResources().getStringArray(R.array.explore_subreddits_food))));
        mExploreCatagoriesMap.put("Motivational", new Explore(R.drawable.explore_bg_motivational, Arrays.asList(getResources().getStringArray(R.array.explore_subreddits_motivational))));
        mExploreCatagoriesMap.put("Woah", new Explore(R.drawable.explore_bg_woah, Arrays.asList(getResources().getStringArray(R.array.explore_subreddits_woah))));
        mExploreCatagoriesMap.put("Design", new Explore(R.drawable.explore_bg_design, Arrays.asList(getResources().getStringArray(R.array.explore_subreddits_design))));
        mExploreCatagoriesMap.put("Art", new Explore(R.drawable.explore_bg_art, Arrays.asList(getResources().getStringArray(R.array.explore_subreddits_art))));
        mExploreCatagoriesMap.put("WTF", new Explore(R.drawable.explore_bg_wtf, Arrays.asList(getResources().getStringArray(R.array.explore_subreddits_wtf))));
        mExploreCatagoriesMap.put("NSFW", new Explore(R.drawable.explore_bg_nsfw, Arrays.asList(getResources().getStringArray(R.array.explore_subreddits_nsfw))));
        mExploreCatagoriesMap.put("Adventure", new Explore(R.drawable.explore_bg_adventure, Arrays.asList(getResources().getStringArray(R.array.explore_subreddits_adventure))));
        mExploreCatagoriesMap.put("Nature", new Explore(R.drawable.explore_bg_nature, Arrays.asList(getResources().getStringArray(R.array.explore_subreddits_nature))));
    }

    private void prepareLeftMenuData() {
        List<ExpandableMenuModel> childModelsList = new ArrayList<>();

        ExpandableMenuModel expandableMenuModel = new ExpandableMenuModel(
                getResources().getString(R.string.menu_accounts),
                true,
                true);
        headerList.add(expandableMenuModel);

        // Always offer Guest option
        ExpandableMenuModel childModel
                = new ExpandableMenuModel(
                Constants.USERNAME_USERLESS_PRETTY,
                false,
                false);
        childModelsList.add(childModel);

        // Fill account names
        for (String username : App.getTokenStore().getUsernames()) {
            // ignore Userless account
            if (!Constants.USERNAME_USERLESS.equalsIgnoreCase(username)) {
                childModel = new ExpandableMenuModel(username, false, false);
                childModelsList.add(childModel);
            }
        }

        // add option to add new account
        childModel = new ExpandableMenuModel(
                getResources().getString(R.string.menu_add_account),
                false,
                false);
        childModelsList.add(childModel);
        childList.put(expandableMenuModel, childModelsList);

        // add option to go to subreddit
        expandableMenuModel = new ExpandableMenuModel(
                getResources().getString(R.string.menu_goto_subreddit),
                true,
                false);
        headerList.add(expandableMenuModel);
        childList.put(expandableMenuModel, null);

        // add option to open settings
        expandableMenuModel = new ExpandableMenuModel(
                getResources().getString(R.string.menu_settings),
                true,
                false);
        headerList.add(expandableMenuModel);
        childList.put(expandableMenuModel, null);
    }

    private void populateExpandableList() {

        expandableListAdapter = new ExpandableListAdapter(getContext(), headerList, childList);
        expandableListView.setAdapter(expandableListAdapter);

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                if (headerList.get(groupPosition).isGroup) {
                    if (!headerList.get(groupPosition).hasChildren) {
                        String clickedItemTitle = headerList.get(groupPosition).menuName;

                        // go to subreddit
                        if (getResources().getString(R.string.menu_goto_subreddit)
                                .equalsIgnoreCase(clickedItemTitle)) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.TransparentDialog);
                            builder.setTitle("Enter subreddit:");

                            EditText input = new EditText(getContext());
                            input.setInputType(InputType.TYPE_CLASS_TEXT);
                            input.setTextColor(getResources().getColor(R.color.colorWhite));
                            builder.setView(input);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String requestedSubreddit = input.getText().toString();
                                    //TODO: This might be better placed as one of the first
                                    // things ActivitySubredditViewer does
                                    App.getMoxieInfoObj().getmSubredditStack().push(requestedSubreddit);

                                    Intent visitSubredditIntent = new Intent(getContext(), ActivitySubredditViewer.class);
                                    visitSubredditIntent.putExtra(Constants.EXTRA_GOTO_SUBREDDIT, requestedSubreddit);
                                    startActivityForResult(visitSubredditIntent, Constants.REQUESTCODE_GOTO_SUBREDDIT_VIEWER);
                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            AlertDialog alertDialog = builder.create();

                            // button color setup
                            alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface dialogInterface) {
                                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                            .setTextColor(getResources().getColor(R.color.colorWhite));
                                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                                            .setTextColor(getResources().getColor(R.color.colorWhite));
                                }
                            });
                            alertDialog.show();
                            alertDialog.getWindow().setLayout((6 * mScreenWidth) / 7, (4 * mScreenHeight) / 18);

                            mDrawerLayout.closeDrawer(mNavigationViewLeft);
                        } else if (getResources().getString(R.string.menu_settings)
                                .equalsIgnoreCase(clickedItemTitle)) {
                            mHomeEventListener.openSettings();
                        }
                    }
                }
                return false;
            }
        });

        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (childList.get(headerList.get(groupPosition)) != null) {
                    ExpandableMenuModel model = childList.get(headerList.get(groupPosition)).get(childPosition);
                    String clickedMenuItemName = model.menuName;
                    // A username in Accounts tab was clicked
                    if (App.getTokenStore().getUsernames().contains(clickedMenuItemName)
                            || Constants.USERNAME_USERLESS_PRETTY.equalsIgnoreCase(clickedMenuItemName)) {
                        // user has selected to switch to userless mode
                        if (Constants.USERNAME_USERLESS_PRETTY.equalsIgnoreCase(clickedMenuItemName)) {
                            new FetchUserlessAccountTask().execute();
                        }
                        // user selected a non-guest switch to
                        else {
                            App.getAccountHelper().switchToUser(clickedMenuItemName);
                            switchOrLogoutCleanup(clickedMenuItemName);
                        }
                    }
                    // clicked "Add account" to add a new acc
                    else if (getResources().getString(R.string.menu_add_account).equalsIgnoreCase(clickedMenuItemName)) {
                        Intent loginIntent = new Intent(getContext(), ActivityNewUserLogin.class);
                        //unlockSessionIntent.putExtra("REQUEST_UNLOCK_SESSION", true);
                        startActivityForResult(loginIntent, Constants.REQUESTCODE_GOTO_LOG_IN);
                    }
                }
                return false;
            }
        });
        expandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long id) {
                if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
                    int groupPosition = ExpandableListView.getPackedPositionGroup(id);
                    int childPosition = ExpandableListView.getPackedPositionChild(id);

                    ExpandableMenuModel model = childList.get(headerList.get(groupPosition)).get(childPosition);
                    String longClickedItem = model.menuName;

                    // only care about handling long clicks for user names
                    if (App.getTokenStore().getUsernames().contains(longClickedItem)) {
                        confirmLogout(longClickedItem);
                    }

                    return true;
                }
                return false;
            }
        });
    }

    private void setupNavViewHeader(View navViewHeader) {
        mNavViewHeaderTitle = (TextView) navViewHeader.findViewById(R.id.navview_header_text_title);
        if (Constants.USERNAME_USERLESS
                .equalsIgnoreCase(App.getAccountHelper().getReddit().getAuthManager().currentUsername())) {
            mNavViewHeaderTitle.setText(Constants.USERNAME_USERLESS_PRETTY);
        } else {
            mNavViewHeaderTitle.setText(App.getAccountHelper().getReddit().getAuthManager().currentUsername());
        }
    }

    /*
        Hover preview set up. Hides any views that aren't needed and unhides
        the ones we need according to which viewer has been selected in settings.
     */
    private void setupPreviewer(SubmissionObj item) {
        /* Small previewer on hold for now*/
        /*if (mPreviewSize == Constants.HoverPreviewSize.SMALL) {
            mHoverPreviewContainerSmall.setVisibility(View.VISIBLE);
            mHoverPreviewContainerLarge.setVisibility(View.GONE);
            //popupWindow = new PopupWindow(customView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            if (item.getSubmissionType() == Constants.SubmissionType.IMAGE) {
                //mHoverPreviewTitleSmall.setText(item.getTitle());
                mHoverImagePreviewSmall.setVisibility(View.VISIBLE);
            }
            if (item.getSubmissionType() == Constants.SubmissionType.GIF) { // and video?

            }
        } else */
        if (mPreviewSize == Constants.HoverPreviewSize.LARGE) {
            if (mHoverPreviewContainerLarge.getParent() != null) {
                ((ViewGroup) mHoverPreviewContainerLarge.getParent()).removeView(mHoverPreviewContainerLarge);
            }
            // forcing the view to display over the entire screen
            ViewGroup vg = (ViewGroup) (getActivity().getWindow().getDecorView().getRootView());
            vg.addView(mHoverPreviewContainerLarge);

            mHoverPreviewContainerLarge.setVisibility(View.VISIBLE);
            mHoverPreviewContainerSmall.setVisibility(View.GONE);
            // fade the toolbar while we're in large previewer
            mToolbar.setAlpha(.1f);
            mHoverPreviewMediaContainerLarge.setVisibility(View.VISIBLE);

            //v.redd.it links will always be non-image. Display in video view.
            if (item.getDomain() == Constants.SubmissionDomain.VREDDIT) {
                // hold off on dispalying videoview for now. Done when vreddit
                // loading task finished (onVRedditMuxTaskCompleted)
            } else {
                if (item.getSubmissionType() == Constants.SubmissionType.IMAGE
                        || item.getSubmissionType() == Constants.SubmissionType.ALBUM
                        || item.getSubmissionType() == null) {
                    focusView(mHoverImagePreviewLarge);
                }
                if (item.getSubmissionType() == Constants.SubmissionType.GIF) {
                    //IREDDIT Gifs to be played in ImageView via Glide
                    if (item.getDomain() == Constants.SubmissionDomain.IREDDIT) {
                        focusView(mHoverImagePreviewLarge);
                    }
                    // All other gifs to be played using Exoplayer
                    else {
                        focusView(mExoplayerLarge);
                    }
                }
            }
        }
    }

    /* Focuses a view depending on which one needs to be displayed*/
    private void focusView(View focused) {
        mPreviewerVideoViewLarge.setVisibility(focused == mPreviewerVideoViewLarge ? View.VISIBLE : View.GONE);
        mHoverImagePreviewLarge.setVisibility(focused == mHoverImagePreviewLarge ? View.VISIBLE : View.GONE);
        mExoplayerLarge.setVisibility(focused == mExoplayerLarge ? View.VISIBLE : View.GONE);
    }

    private void validatePreferences() throws Exception {
        if (prefs_settings != null) {
            mHideNSFW = prefs_settings.getBoolean(Constants.SETTINGS_HIDE_NSFW, true);
            mAllowImagePreview = prefs_settings.getBoolean(Constants.SETTINGS_ALLOW_HOVER_PREVIEW, true);
            mAllowBigDisplayClickClose = prefs_settings.getBoolean(Constants.SETTINGS_ALLOW_BIGDISPLAY_CLOSE_CLICK, false);
            mPreviewSize = prefs_settings.getString(Constants.SETTINGS_PREVIEW_SIZE, Constants.SETTINGS_PREVIEW_SIZE_LARGE)
                    .equalsIgnoreCase(Constants.SETTINGS_PREVIEW_SIZE_LARGE)
                    ? Constants.HoverPreviewSize.LARGE : Constants.HoverPreviewSize.SMALL;
            mDisplayDomainIcon = prefs_settings.getBoolean(Constants.SETTINGS_ALLOW_DOMAIN_ICON, false);
            mHideNSFWThumbs = prefs_settings.getBoolean(Constants.SETTINGS_HIDE_NSFW_THUMBS, false);
            mDisplayFiletypeIcons = prefs_settings.getBoolean(Constants.SETTINGS_ALLOW_FILETYPE_ICON, false);
            mDisplayNSFWIcon = prefs_settings.getBoolean(Constants.SETTINGS_SHOW_NSFW_ICON, false);

            App.getMoxieInfoObj().setHideNSFW(mHideNSFW);
            //App.getMoxieInfoObj().setSubreddit("pics");
        } else {
            throw new Exception("Failed to retrieve SharedPreferences on validatePreferences(). "
                    + "Could not find prefs_settings KEY_GET_PREFS_SETTINGS.");
        }
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


    public class SubredditContentRecyclerAdapter extends PagedListAdapter<SubmissionObj, SubredditContentRecyclerAdapter.ItemViewHolder> {
        private Context mContext;

        SubredditContentRecyclerAdapter(Context mContext) {
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
            mProgressbarMain.setVisibility(View.GONE);
            // empty subreddit, nothing to display
            if (is404) return;

            // prevent null-ness from empty subreddit or any unexpected submission type errors
            SubmissionObj item = getItem(holder.getAdapterPosition()) == null
                    ? new SubmissionObj(true) : getItem(holder.getAdapterPosition());

            // initialize anything that might overlay the thumbnail
            // to GONE to avoid conflicts when recycling
            holder.thumbnailIconNSFW.setVisibility(View.GONE);
            holder.thumbnailIconFiletype.setVisibility(View.GONE);
            holder.thumbnailIconDomain.setVisibility(View.GONE);

            // Workaround for checking if requested subreddit is empty (or invalid).
            // If invalid subreddit, there should exist only 1 element and this field will be true
            // Note: The adapter continues calling onBindViewHolder for as many items as we
            // usually display on one recyclerview fetch (50?).
            // is404 prevents more 404 pages from being added to the backstack
            if (item.isSubredditEmpty()) {
                is404 = true;
                holder.itemView.setVisibility(View.GONE);
                mHomeEventListener.set404(true);
                // the requested subreddit was empty. Display something meaningful
                display404();
                return;
            }
            // assume reddit has not provided a thumbnail to be safe
            String thumbnail = Constants.URI_404_thumbnail;
            item.setSubmissionType(Utils.getSubmissionType(item.getUrl()));

            is404 = false;
            // Imgur
            //TODO: imgur albums. Example URL https://imgur.com/a/K8bJ9pV (nsfw)
            if (item.getDomain() == Constants.SubmissionDomain.IMGUR) {
                if (Utils.isImgurAlbum(item.getUrl())) {
                    item.setSubmissionType(Constants.SubmissionType.ALBUM);
                }
                // Check if submission type is null. This will happen if the item's URL is
                // to a non-direct IMAGE(not gif/video) link such as https://imgur.com/qTadRtq
                if (item.getSubmissionType() == null) {
                    // Here we assume indirect imgur links refer to images only
                    item.setSubmissionType(Constants.SubmissionType.IMAGE);
                }
                // We assume item will always have a thumbnail in an image format
                thumbnail = item.getThumbnail();
                holder.thumbnailIconDomain.setBackground(getResources().getDrawable(R.drawable.ic_imgur_i_black_bg));
            }
            //v.redd.it
            else if (item.getDomain() == Constants.SubmissionDomain.VREDDIT) {
                if ("hosted:video".equalsIgnoreCase(item.getPostHint())) {
                    item.setSubmissionType(Constants.SubmissionType.VIDEO);
                } else {
                    item.setSubmissionType(Constants.SubmissionType.GIF);
                }

                thumbnail = item.getThumbnail();
                holder.thumbnailIconDomain.setBackground(getResources().getDrawable(R.drawable.ic_reddit_blue_circle));
            }
            // i.redd.it
            else if (item.getDomain() == Constants.SubmissionDomain.IREDDIT) {
                thumbnail = item.getThumbnail();
                holder.thumbnailIconDomain.setBackground(getResources().getDrawable(R.drawable.ic_reddit_circle_orange));
            }
            //gfycat
            else if (item.getDomain() == Constants.SubmissionDomain.GFYCAT) {
                // We're given a URL in this format: //https://gfycat.com/SpitefulGoldenAracari
                // extract gfycat ID (looks like:SpitefulGoldenAracari)
                String gfycatHash = Utils.getGfycatHash(item.getUrl());
                // get Gfycat .mp4 "clean url"
                Utils.getGfycat(gfycatHash, item);
                // Assume all Gfycat links are of submission type GIF
                item.setSubmissionType(Constants.SubmissionType.GIF);
                thumbnail = item.getThumbnail();
                holder.thumbnailIconDomain.setBackground(getResources().getDrawable(R.drawable.ic_gfycat_circle_blue));

            }
            //youtube
            else if (item.getDomain() == Constants.SubmissionDomain.YOUTUBE) {
                //item.setSubmissionType(Constants.SubmissionType.VIDEO);
                thumbnail = item.getThumbnail();
                holder.thumbnailIconDomain.setBackground(getResources().getDrawable(R.drawable.ic_youtube_red));
            }

            // Double check Reddit assigned a valid image (jpg,png,etc) as the thumbnail.
            if (Arrays.asList(Constants.VALID_IMAGE_EXTENSION)
                    .contains(Utils.getFileExtensionFromUrl(item.getThumbnail()))) {
                thumbnail = item.getThumbnail();
            }
            // If not, check if submission URL is valid image to use as thumbnail.
            else if (Arrays.asList(Constants.VALID_IMAGE_EXTENSION)
                    .contains(Utils.getFileExtensionFromUrl(item.getUrl()))) {
                thumbnail = item.getUrl();
            } else {
                // will assign 404 thumbnail if not given a thumbnail at this point
                item.setThumbnail(thumbnail);
                //holder.thumbnailIconDomain.setBackground(null);
            }

            /* Load thumbnail into recyclerview */
            // Check if we need to hide thumbnail (settings option)
            if (item.isNSFW() && mHideNSFWThumbs) {
                GlideApp.load(getResources().getDrawable(R.drawable.ic_reddit_nsfw_dark))
                        .apply(new RequestOptions().centerInside().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(holder.thumbnailImageView);
            } else {
                GlideApp.load(thumbnail)
                        .listener(new RecyclerLoadProgressListener(item, holder))
                        .apply(new RequestOptions().centerCrop().diskCacheStrategy(DiskCacheStrategy.ALL))
                        .into(holder.thumbnailImageView);
            }

            /* open submission viewer when image clicked*/
            holder.thumbnailImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.onClick(holder.thumbnailImageView);
                }
            });

            /* Long press previewer */
            holder.thumbnailImageView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View pView) {
                    // do nothing if previewing has been disabled through settings
                    if (!mAllowImagePreview) {
                        return true;
                    }
                    // prevent recyclerview from handling touch events, otherwise bad things happen
                    mRecyclerHome.setHandleTouchEvents(false);
                    isImageViewPressed = true;
                    /* Small previewer on hold for now. */
                    /*
                    if (mPreviewSize == Constants.HoverPreviewSize.SMALL) {
                        mHoverPreviewTitleSmall.setText(item.getTitle());
                        //popupWindow = new PopupWindow(customView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        if (item.getSubmissionType() == Constants.SubmissionType.IMAGE) {
                            setupPreviewer(item);
                            GlideApp.load(item.getCleanedUrl() != null ? item.getCleanedUrl() : item.getUrl())
                                    .apply(new RequestOptions()
                                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                                    .into(mHoverImagePreviewSmall);
                        } else if (item.getSubmissionType() == Constants.SubmissionType.GIF) {

                        }
                    } else*/

                    if (mPreviewSize == Constants.HoverPreviewSize.LARGE) {
                     /*   mHoverPreviewTitleLarge.setText(item.getCompactTitle() != null
                                ? item.getCompactTitle() : item.getTitle());
                        mHoverPreviewSubredditLarge.setText("/r/" + item.getSubreddit());*/
                        setupPreviewer(item);

                        if (item.getSubmissionType() == Constants.SubmissionType.IMAGE) {
                            // Imgur Urls might be pointing to indirect image URLs
                            if (item.getDomain() == Constants.SubmissionDomain.IMGUR
                                    && !Arrays.asList(Constants.VALID_IMAGE_EXTENSION)
                                    .contains(Utils.getFileExtensionFromUrl(item.getUrl()))) {
                                mPreviewerProgressBar.setVisibility(View.VISIBLE);
                                // fixes indirect imgur url and uses Glide to load image on success
                                Utils.fixIndirectImgurUrl(item, Utils.getImgurHash(item.getUrl()),
                                        new OnTaskCompletedListener() {
                                            @Override
                                            public void downloadSuccess() {
                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        mPreviewerProgressBar.setVisibility(View.GONE);
                                                        GlideApp.load(item.getCleanedUrl())
                                                                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                                                                /* .listener(new GlideProgressListener(mPreviewerProgressBar))*/
                                                                .into(mHoverImagePreviewLarge);
                                                    }
                                                });

                                            }
                                            @Override
                                            public void downloadFailure() {
                                                super.downloadFailure();
                                            }
                                        });
                            }
                            // image should be ready to be displayed here
                            else {
                                GlideApp.load(item.getCleanedUrl() != null ? item.getCleanedUrl() : item.getUrl())
                                        .listener(new GlideProgressListener(mPreviewerProgressBar))
                                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                                        .into(mHoverImagePreviewLarge);
                            }
                        } else if (item.getSubmissionType() == Constants.SubmissionType.GIF
                                || item.getSubmissionType() == Constants.SubmissionType.VIDEO) {
                            // gif might have a .gifv (imgur) extension
                            // ...need to fetch corresponding .mp4
                            if (item.getDomain() == Constants.SubmissionDomain.IMGUR
                                    && Utils.getFileExtensionFromUrl(item.getUrl())
                                    .equalsIgnoreCase("gifv")) {
                                Utils.getMp4LinkImgur(item, Utils.getImgurHash(item.getUrl()), new OnTaskCompletedListener() {
                                    @Override
                                    public void downloadSuccess() {
                                        super.downloadSuccess();
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                // cleaned url should contain .mp4 link
                                                initializePreviewExoPlayer(item.getCleanedUrl());
                                            }
                                        });
                                    }

                                    @Override
                                    public void downloadFailure() {
                                        super.downloadFailure();
                                    }
                                });
                            }
                            // VREDDIT videos are high maintance
                            else if (item.getDomain() == Constants.SubmissionDomain.VREDDIT) {
                                mPreviewerProgressBar.setVisibility(View.VISIBLE);
                                String url = item.getEmbeddedMedia().getRedditVideo().getFallbackUrl();
                                try {
                                    new Utils.FetchVRedditGifTask(getContext(), url, new OnVRedditTaskCompletedListener() {
                                        @Override
                                        public void onVRedditMuxTaskCompleted(Uri uriToLoad) {
                                            mPreviewerVideoViewLarge.setVideoURI(uriToLoad);
                                            focusView(mPreviewerVideoViewLarge);
                                            mPreviewerVideoViewLarge.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                                @Override
                                                public void onPrepared(MediaPlayer mp) {
                                                    mp.start();
                                                    mp.setLooping(true);
                                                }
                                            });
                                            mPreviewerVideoViewLarge.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                                @Override
                                                public void onCompletion(MediaPlayer mp) {
                                                    mp.stop();
                                                    mp.release();
                                                }
                                            });
                                        }
                                    }).execute();
                                } catch (Exception e) {
                                    //LogUtil.e(e, "Error v.redd.it url: " + url);
                                }
                            }
                            // IREDDIT submissions will always be .gif (not .gifv)
                            // Also check for anything else that may be .gif here
                            else if (item.getDomain() == Constants.SubmissionDomain.IREDDIT
                                    || Utils.getFileExtensionFromUrl(item.getUrl()).equalsIgnoreCase("gif")) {
                                mPreviewerProgressBar.setVisibility(View.VISIBLE);
                                GlideApp
                                        .asGif()
                                        .load(item.getUrl())
                                        .into(mHoverImagePreviewLarge);
                            } else {
                                initializePreviewExoPlayer(item.getCleanedUrl() != null ? item.getCleanedUrl() : item.getUrl());
                            }
                        }
                        // submission is of unknown type (i.e. submission from /r/todayilearned)
                        else {
                            mPreviewerProgressBar.setVisibility(View.GONE);
                            GlideApp.load(Constants.URI_404)
                                    .listener(new GlideProgressListener(mPreviewerProgressBar))
                                    .into(mHoverImagePreviewLarge);
                        }
                    }
                    return true;
                }
            });

            holder.thumbnailImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View pView, MotionEvent pEvent) {
                    boolean singleTap = mGestureDetector.onTouchEvent(pEvent);
                    // do nothing if previewing has been disabled through settings or if
                    // the touch we're detecting is a secondary touch that no one cares about
                    if (!mAllowImagePreview && !singleTap) {
                        return true;
                    }
                    // save ID of the first pointer(touch) ID
                    mActivePointerId = pEvent.getPointerId(0);

                    // find current touch ID
                    final int action = pEvent.getAction();
                    int currPointerId = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;

                    // only care about doing stuff that relates to first finger touch
                    if (currPointerId == mActivePointerId) {
                        if (pEvent.getAction() == MotionEvent.ACTION_UP) {
                            // hide hoverView on click release
                            if (isImageViewPressed) {
                                // done with hover view, allow recyclerview to handle touch events
                                mRecyclerHome.setHandleTouchEvents(true);
                                isImageViewPressed = false;
                              /*  if (mPreviewSize == Constants.HoverPreviewSize.SMALL) {
                                    mHoverPreviewContainerSmall.setVisibility(View.GONE);

                                } else*/
                                if (mPreviewSize == Constants.HoverPreviewSize.LARGE) {
                                    mPreviewerProgressBar.setVisibility(View.GONE);
                                    mHoverPreviewContainerLarge.setVisibility(View.GONE);
                                    clearVideoView();
                                    mHoverImagePreviewLarge.setVisibility(View.GONE);
                                    mExoplayerLarge.setVisibility(View.GONE);
                                    stopExoPlayer();
                                    // restore the toolbar
                                    mToolbar.setAlpha(1);
                                }
                            }
                        }
                    }
                    return false;
                }
            });

        }

        private class RecyclerLoadProgressListener implements RequestListener<Drawable> {
            // recyclerview's thumbnail
            private ItemViewHolder holder;
            private SubmissionObj item;

            public RecyclerLoadProgressListener(SubmissionObj item, final ItemViewHolder holder) {
                this.item = item;
                this.holder = holder;
            }

            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                configureRecyclerThumbOverlay(item, holder);
                return false;
            }
        }

        // Release any resources that may be lingering after view has been recycled
        @Override
        public void onViewRecycled(@NonNull ItemViewHolder holder) {
            super.onViewRecycled(holder);
        }

        public class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            //TextView textView;
            ImageView thumbnailImageView;
            //YouTubeThumbnailView thumbnailYouTube;
            ImageView thumbnailIconDomain;
            ImageView thumbnailIconFiletype;
            ImageView thumbnailIconNSFW;

            public ItemViewHolder(View itemView) {
                super(itemView);
                thumbnailImageView = (ImageView) itemView.findViewById(R.id.thumbnail);
                //  thumbnailYouTube = (YouTubeThumbnailView) itemView.findViewById(R.id.recycler_youtube_thumbnail);
                thumbnailIconDomain = (ImageView) itemView.findViewById(R.id.thumbnail_domain_icon);
                thumbnailIconFiletype = (ImageView) itemView.findViewById(R.id.thumbnail_filetype_icon);
                thumbnailIconNSFW = (ImageView) itemView.findViewById(R.id.thumbnail_nsfw_icon);
            }

            @Override
            public void onClick(View view) {
                SubmissionObj submission = getItem(getLayoutPosition());
                openFullDisplayer(submission);
            }
        }
    }

    private void display404() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment emptySubredditFragment = Fragment404.newInstance();

        /* Bundle args = new Bundle();
        args.putSerializable(Constants.EXTRA_SUBMISSION_OBJ, submission);
        mediaDisplayerFragment.setArguments(args);
        */

        int parentContainerId = ((ViewGroup) getView().getParent()).getId();
        ft.add(parentContainerId, emptySubredditFragment, Constants.TAG_FRAG_404);
        ft.addToBackStack(null);
        ft.commit();
    }

    /* Shows/hides icons which will be overlaid on the recyclerview's viewholder elements.
     *  Configured through Settings. */
    private void configureRecyclerThumbOverlay(SubmissionObj item,
                                               SubredditContentRecyclerAdapter.ItemViewHolder thumbnailHolder) {
        // thumbnailHolder.thumbnailIconFiletype.setVisibility(mDisplayFiletypeIcons ? View.VISIBLE : View.GONE);
        //thumbnailHolder.thumbnailIconDomain.setVisibility(mDisplayDomainIcon ? View.VISIBLE : View.GONE);
        // thumbnailHolder.thumbnailIconNSFW.setVisibility(mDisplayNSFWIcon ? View.VISIBLE : View.GONE);
        if (mDisplayDomainIcon) {
            thumbnailHolder.thumbnailIconDomain.setVisibility(View.VISIBLE);
        }

        /* Display file type icons if option is enabled through settings*/
        if (item.getSubmissionType() != null && mDisplayFiletypeIcons) {
            switch (item.getSubmissionType()) {
                case IMAGE:
                    thumbnailHolder.thumbnailIconFiletype.setBackground(
                            getResources().getDrawable(R.drawable.ic_filetype_image));
                    thumbnailHolder.thumbnailIconFiletype.setVisibility(View.VISIBLE);
                    break;
                case GIF:
                    thumbnailHolder.thumbnailIconFiletype.setBackground(
                            getResources().getDrawable(R.drawable.ic_filetype_gif));
                    thumbnailHolder.thumbnailIconFiletype.setVisibility(View.VISIBLE);
                    break;
                case VIDEO:
                    thumbnailHolder.thumbnailIconFiletype.setBackground(
                            getResources().getDrawable(R.drawable.ic_filetype_video));
                    thumbnailHolder.thumbnailIconFiletype.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }

        // Display NSFW icon on top right if enabled through settings
        if (mDisplayNSFWIcon && item.isNSFW()) {
            thumbnailHolder.thumbnailIconNSFW.setVisibility(View.VISIBLE);
        } else {
            thumbnailHolder.thumbnailIconNSFW.setVisibility(View.GONE);
        }
    }

    private void openFullDisplayer(SubmissionObj submissionObj) {
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

        Fragment bigDisplayFragment = FragmentFullDisplay.newInstance();
        Bundle args = new Bundle();
        args.putSerializable(Constants.ARGS_SUBMISSION_OBJ, submissionObj);
        bigDisplayFragment.setArguments(args);

        bigDisplayFragment.setTargetFragment(FragmentHome.this, Constants.REQUESTCODE_GOTO_BIG_DISPLAY);


        int parentContainerId = ((ViewGroup) getView().getParent()).getId();
        ft.add(parentContainerId, bigDisplayFragment,Constants.TAG_FRAG_FULL_DISPLAYER);
        ft.addToBackStack(null);
        ft.commit();
    }

    private void refresh(boolean invalidateData) {
        mHomeEventListener.refreshFeed(invalidateData);
    }

    private void invalidateData() {
        SubmissionsViewModel submissionsViewModel
                = ViewModelProviders.of(FragmentHome.this).get(SubmissionsViewModel.class);
        submissionsViewModel.invalidate();
    }

    // Prompts user to confirm log out and proceeds to remove account
    private void confirmLogout(String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.TransparentDialog);
        builder.setTitle("Confirm log out");
        builder.setMessage("Remove user " + username + "?");
        builder.setIcon(R.drawable.ic_white_log_out);
        builder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // log out, switch to userless, and clean up
                //App.getAccountHelper().logout();
                App.getTokenStore().deleteLatest(username);
                App.getTokenStore().deleteRefreshToken(username);
                new FetchUserlessAccountTask().execute();
            }
        });
        builder.setNegativeButton(android.R.string.no, null);
        AlertDialog alertDialog = builder.create();

        // button color setup
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(getResources().getColor(R.color.colorWhite));
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(getResources().getColor(R.color.colorWhite));
            }
        });

        // resize the alert dialog
        alertDialog.show();
        alertDialog.getWindow().setLayout((6 * mScreenWidth) / 7, (4 * mScreenHeight) / 18);
    }

    // Clean up any lingering views, media players, prefs_settings, etc
    private void switchOrLogoutCleanup(String newCurrUser) {
        mDrawerLayout.closeDrawer(mNavigationViewLeft);
        // Update most recent user to new user
        prefs_settings.edit().putString(Constants.MOST_RECENT_USER, newCurrUser).apply();

        // start our subreddit stack over cause we're starting over again
        while (!App.getMoxieInfoObj().getmSubredditStack().isEmpty()) {
            App.getMoxieInfoObj().getmSubredditStack().pop();
        }

        //remove any sorting we had...null is ok :^)
        App.getMoxieInfoObj().setmSortBy(null);
        App.getMoxieInfoObj().setmTimePeriod(null);
        mHomeEventListener.startOver();
    }

    /* Updates SharedPreferences's current logged-in user.
     *  App.getTokenStore.getUsernames() contains the list of users in the order in which they were added.
     *  The user's index within this aforementioned list should be the same as the index in whatever
     *  list we end up displaying the logged in users (plus 1 because the getUsernames() list has
     *  "userless" at index 0).
     *
     * */
    private void updateCurrentUser(int userIndex) {
        if (userIndex >= App.getTokenStore().getUsernames().size()) {
            Log.e("ACCOUNT LOGIN EXCEPTION",
                    "Tried switching to user which is out of index. Swithcing to Userless mode");
            userIndex = 0;
        }

        // This is where we assume the 0th username is USERLESS.....
        String username = App.getTokenStore().getUsernames().get(userIndex);
        App.getAccountHelper().switchToUser(username);

        // update the most recent logged in user in sharedprefs
        prefs_settings.edit().putString(Constants.MOST_RECENT_USER, username).commit();
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

    /* Exoplayer for previewer*/

    private void initializePreviewExoPlayer(String url) {

        mExoplayerLarge.requestFocus();

        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);

        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(getContext(), trackSelector);

        mExoplayerLarge.setPlayer(player);

        // hide the controller since we're dealing with previewer here and user will be long clicking
        mExoplayerLarge.hideController();
        mExoplayerLarge.setControllerVisibilityListener(new PlaybackControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int i) {
                if (i == 0) {
                    mExoplayerLarge.hideController();
                }
            }
        });

        player.addListener(new PlayerEventListener());
        player.setPlayWhenReady(true);

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
                    //exoplayerProgressbar.setVisibility(View.VISIBLE);
                    break;
                case Player.STATE_BUFFERING:  // The player is buffering (loading the content)
                    //   exoplayerProgressbar.setVisibility(View.VISIBLE);
                    break;
                case Player.STATE_READY:      // The player is able to immediately play
                    // exoplayerProgressbar.setVisibility(View.GONE);
                    break;
                case Player.STATE_ENDED:      // The player has finished playing the media
                    //  exoplayerProgressbar.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private void releaseExoPlayer() {
        if (player != null) {
            player.release();
        }
    }

    private void stopExoPlayer() {
        if (player != null) {
            player.stop();
        }
    }

    private void clearVideoView() {
        mPreviewerVideoViewLarge.stopPlayback();
        mPreviewerVideoViewLarge.setVisibility(View.GONE);
    }


    //* Sets our reddit client to userless and refreshes Home on completion*//*
    private class FetchUserlessAccountTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            App.getAccountHelper().switchToUserless();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            switchOrLogoutCleanup(Constants.USERNAME_USERLESS);
        }
    }
}
