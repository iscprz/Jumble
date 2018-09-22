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
import android.util.DisplayMetrics;
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
import com.makeramen.roundedimageview.RoundedImageView;
import com.sometimestwo.moxie.API.GfycatAPI;
import com.sometimestwo.moxie.Imgur.client.ImgurClient;
import com.sometimestwo.moxie.Imgur.response.images.ImgurSubmission;
import com.sometimestwo.moxie.Imgur.response.images.SubmissionRoot;
import com.sometimestwo.moxie.Model.ExpandableMenuModel;
import com.sometimestwo.moxie.Model.GfyItem;
import com.sometimestwo.moxie.Model.GfycatWrapper;
import com.sometimestwo.moxie.Model.SubmissionObj;
import com.sometimestwo.moxie.Utils.Constants;
import com.sometimestwo.moxie.Utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.app.Activity.RESULT_OK;

public class FragmentHome extends Fragment {
    public final static String TAG = Constants.TAG_FRAG_HOME;
    public final static int KEY_INTENT_GOTO_SUBMISSIONVIEWER = 1;
    private final static int KEY_LOG_IN = 2;

    private DisplayMetrics mDisplayMetrics;
    private int mScreenWidth;
    private int mScreenHeight;

    private RequestManager GlideApp;
    private SwipeRefreshLayout mRefreshLayout;
    private MultiClickRecyclerView mRecyclerMain;
    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationViewLeft;
    private NavigationView mNavigationViewRight;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mNumDisplayColumns;
    private String mCurrSubreddit;
    //private String mCurrUsername = null;
    private boolean isImageViewPressed = false;
    private int mActivePointerId = -1;
    private boolean isViewingSubmission = false;
    private GestureDetector mGestureDetector;
    private ProgressBar mProgressBar;
    private boolean mInvalidateDataSource = false;
    private boolean is404 = false;

    // User's preferences. Initialize with default values for safety
    SharedPreferences sharedprefs_settings;
    private boolean mAllowNSFW = false;
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
    private Map<String, Integer> mExploreCatagoriesMap;
    // holds a list of the "Explore" catagories in memory
    private List<String> mExploreCatagoriesList;

    // log out button
    private TextView mButtonLogout;

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

        public void refreshFeed(boolean invalidateData);

        public void isHome(boolean isHome);

        public void goBack();

        public void set404(boolean is404);
    }

    public static FragmentHome newInstance() {
        return new FragmentHome();
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlideApp = Glide.with(this);
        //  mRedditClient = App.getAccountHelper().isAuthenticated() ? App.getAccountHelper().getReddit() : App.getAccountHelper().switchToUserless();
        setHasOptionsMenu(true);
        sharedprefs_settings = getContext().getSharedPreferences(Constants.KEY_GET_PREFS_SETTINGS, Context.MODE_PRIVATE);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        //mPopupView = inflater.inflate(R.layout.layout_popup_preview_small,null);
        //mParentView = v.findViewById(R.id.main_content);

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
        mRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.recycler_refresh);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FragmentHome.this.refresh(true);
            }
        });

        /* Recycler view setup*/
        mRecyclerMain = (MultiClickRecyclerView) v.findViewById(R.id.recycler_zoomie_view);
        mRecyclerMain.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mRecyclerMain.setHasFixedSize(true);

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
        mHoverPreviewSubredditLarge = (TextView) v.findViewById(R.id.hover_view_textview_subreddit);
        mHoverImagePreviewLarge = (ImageView) v.findViewById(R.id.large_previewer_imageview);

        /* Exo player */
        //mExoplayerContainerLarge = (FrameLayout) v.findViewById(R.id.container_exoplayer_large);
        mExoplayerLarge = (PlayerView) v.findViewById(R.id.large_previewer_exoplayer);

        bandwidthMeter = new DefaultBandwidthMeter();
        mediaDataSourceFactory = new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "Moxie"), (TransferListener<? super DataSource>) bandwidthMeter);
        window = new Timeline.Window();
        //ivHideControllerButton = findViewById(R.id.exo_controller);
        // progressBar = findViewById(R.id.progress_bar);

        /* Gfycat player*/
        // mHoverPreviewGfycatLarge = (GfycatPlayer) v.findViewById(R.id.large_previewer_gfycat);

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
            // Make sure we're referencing the right user
            //validateCurrUser();
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
        if (!isViewingSubmission) {
            menu.findItem(R.id.menu_comments_sortby).setVisible(false);
            menu.findItem(R.id.menu_submissions_sortby).setVisible(true);
        } else {
            menu.findItem(R.id.menu_comments_sortby).setVisible(true);
            menu.findItem(R.id.menu_submissions_sortby).setVisible(false);
        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SubredditSort sortBy;
        TimePeriod timePeriod;
        switch (item.getItemId()) {
            case android.R.id.home:
                // Hacky workaround for handling conflict with opening drawer.
                // Only offer the hamburger menu option if we're at the home screen
                if (!isViewingSubmission && mCurrSubreddit == null) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                } else {
                    mHomeEventListener.goBack();
                }
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
            if (mCurrSubreddit != null) {
                mToolbar.setTitle(getResources().getString(R.string.subreddit_prefix) + mCurrSubreddit);
            } else {
                mToolbar.setTitle("frontpage");
            }

            // Subtitle - remember that if sort by is null, getter will default to HOT
            mToolbar.setSubtitle(Utils.makeTextCute(App.getMoxieInfoObj().getmSortBy().toString()));


            // set hamburger menu icon
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
            }
            //  mCurrSubreddit = mRedditClient.getmRedditDataRequestObj().getmSubreddit();
        } catch (NullPointerException npe) {
            throw new NullPointerException("Null ptr exception trying to unpack arguments in " + TAG);
        }
    }

    /* Drawerlayout config to handle navigation views */
    private void setupDrawerLayout(View v) {
        mDrawerLayout = v.findViewById(R.id.drawer_layout);
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
        expandableListView = v.findViewById(R.id.expandable_list_right);
        prepareMenuData();
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
        mExploreRecyclerView.setAdapter(new ExploreGridRecyclerAdapter());

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
                    + mExploreCatagoriesMap.get(category);
            GlideApp.load(Uri.parse(bgUri))
                    .apply(new RequestOptions()
                            .centerInside()
                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                    .into(exploreItemGridItem.mExploreImage);

            exploreItemGridItem.mExploreImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });

            exploreItemGridItem.mExploreTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
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
        mExploreCatagoriesMap.put("Funny", R.drawable.explore_bg_funny);
        mExploreCatagoriesMap.put("Awwwww", R.drawable.explore_bg_aww);
        mExploreCatagoriesMap.put("Travel", R.drawable.explore_bg_travel);
        mExploreCatagoriesMap.put("Meme", R.drawable.explore_bg_meme);
        mExploreCatagoriesMap.put("GIFs", R.drawable.explore_bg_gif);
        mExploreCatagoriesMap.put("Food", R.drawable.explore_bg_food);
        mExploreCatagoriesMap.put("Sports", R.drawable.explore_bg_sports);
        mExploreCatagoriesMap.put("Gaming", R.drawable.explore_bg_gaming);
        mExploreCatagoriesMap.put("Design", R.drawable.explore_bg_design);
        mExploreCatagoriesMap.put("Art", R.drawable.explore_bg_art);
        mExploreCatagoriesMap.put("WTF", R.drawable.explore_bg_wtf);
        mExploreCatagoriesMap.put("Porn", R.drawable.explore_bg_porn);
        mExploreCatagoriesMap.put("Infographics", R.drawable.explore_bg_infographics);
        mExploreCatagoriesMap.put("Nature", R.drawable.explore_bg_nature);

        //Uri.parse("android.resource://com.sometimestwo.moxie/" + R.drawable.sample_1);
        //com.sometimestwo.moxie R.drawable.explore_bg_art
    }

    private void prepareMenuData() {
        List<ExpandableMenuModel> childModelsList = new ArrayList<>();

        ExpandableMenuModel expandableMenuModel = new ExpandableMenuModel(
                getResources().getString(R.string.menu_accounts),
                true,
                true);
        headerList.add(expandableMenuModel);

        // Always offer Guest option
        ExpandableMenuModel childModel = new ExpandableMenuModel(Constants.USERNAME_USERLESS_PRETTY, false, false);
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
                        // log in
                        if (getResources().getString(R.string.menu_log_in)
                                .equalsIgnoreCase(clickedItemTitle)) {
                            mDrawerLayout.closeDrawer(mNavigationViewLeft);
                        } else if (getResources().getString(R.string.menu_add_account)
                                .equalsIgnoreCase(clickedItemTitle)) {
                            mDrawerLayout.closeDrawer(mNavigationViewLeft);
                        }
                        // go to subreddit
                        else if (getResources().getString(R.string.menu_goto_subreddit)
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
                                    startActivity(visitSubredditIntent);
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
                        // user selected a non-guest account to log in to
                        else {
                            App.getAccountHelper().switchToUser(clickedMenuItemName);
                            switchOrLogoutCleanup(clickedMenuItemName);
                        }
                    }
                    // clicked "Add account" to add a new acc
                    else if (getResources().getString(R.string.menu_add_account).equalsIgnoreCase(clickedMenuItemName)) {
                        Intent loginIntent = new Intent(getContext(), ActivityNewUserLogin.class);
                        //unlockSessionIntent.putExtra("REQUEST_UNLOCK_SESSION", true);
                        startActivityForResult(loginIntent, KEY_LOG_IN);
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
        mProgressBar.setVisibility(View.VISIBLE);
        if (mPreviewSize == Constants.HoverPreviewSize.SMALL) {
            mHoverPreviewContainerSmall.setVisibility(View.VISIBLE);
            mHoverPreviewContainerLarge.setVisibility(View.GONE);
            //popupWindow = new PopupWindow(customView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            if (item.getSubmissionType() == Constants.SubmissionType.IMAGE) {
                //mHoverPreviewTitleSmall.setText(item.getTitle());
                mHoverImagePreviewSmall.setVisibility(View.VISIBLE);
            }
            if (item.getSubmissionType() == Constants.SubmissionType.GIF) { // and video?

            }
        } else if (mPreviewSize == Constants.HoverPreviewSize.LARGE) {
            if (mHoverPreviewContainerLarge.getParent() != null) {
                ((ViewGroup) mHoverPreviewContainerLarge.getParent()).removeView(mHoverPreviewContainerLarge);
            }
            // forcing the view to display over the entire screen
            ViewGroup vg = (ViewGroup) (getActivity().getWindow().getDecorView().getRootView());
            vg.addView(mHoverPreviewContainerLarge);

            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Window w = getActivity().getWindow(); // in Activity's onCreate() for instance
                w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            }*/

            mHoverPreviewContainerLarge.setVisibility(View.VISIBLE);
            mHoverPreviewContainerSmall.setVisibility(View.GONE);
            // fade the toolbar while we're in large previewer
            mToolbar.setAlpha(.1f);
            mHoverPreviewMediaContainerLarge.setVisibility(View.VISIBLE);
            if (item.getSubmissionType() == Constants.SubmissionType.IMAGE) {
                mHoverImagePreviewLarge.setVisibility(View.VISIBLE);
                mExoplayerLarge.setVisibility(View.GONE);
            }
            if (item.getSubmissionType() == Constants.SubmissionType.GIF) { // and video?
                initializePlayer(item.getUrl());
                mExoplayerLarge.setVisibility(View.VISIBLE);
                mHoverImagePreviewLarge.setVisibility(View.GONE);

            }
        }
    }

    private void validatePreferences() throws Exception {
        if (sharedprefs_settings != null) {

            mAllowNSFW = sharedprefs_settings.getBoolean(Constants.SETTINGS_ALLOW_NSFW, false);
            mAllowImagePreview = sharedprefs_settings.getBoolean(Constants.SETTINGS_ALLOW_HOVER_PREVIEW, true);
            mAllowBigDisplayClickClose = sharedprefs_settings.getBoolean(Constants.SETTINGS_ALLOW_BIGDISPLAY_CLOSE_CLICK, false);
            mPreviewSize = sharedprefs_settings.getString(Constants.SETTINGS_PREVIEW_SIZE, Constants.SETTINGS_PREVIEW_SIZE_SMALL)
                    .equalsIgnoreCase(Constants.SETTINGS_PREVIEW_SIZE_SMALL)
                    ? Constants.HoverPreviewSize.SMALL : Constants.HoverPreviewSize.LARGE;
            mDisplayDomainIcon = sharedprefs_settings.getBoolean(Constants.SETTINGS_ALLOW_DOMAIN_ICON, false);
            mHideNSFWThumbs = sharedprefs_settings.getBoolean(Constants.SETTINGS_HIDE_NSFW_THUMBS, false);
            mDisplayFiletypeIcons = sharedprefs_settings.getBoolean(Constants.SETTINGS_ALLOW_FILETYPE_ICON, false);
            mDisplayNSFWIcon = sharedprefs_settings.getBoolean(Constants.SETTINGS_SHOW_NSFW_ICON, false);

            mNumDisplayColumns = 3;//sharedprefs_settings.getInt(Constants.SETTINGS_NUM_DISPLAY_COLS);


            App.getMoxieInfoObj().setAllowNSFW(mAllowNSFW);
            //App.getMoxieInfoObj().setSubreddit("pics");

        } else {
            throw new Exception("Failed to retrieve SharedPreferences on validatePreferences(). "
                    + "Could not find sharedprefs_settings KEY_GET_PREFS_SETTINGS.");
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


    private class SubredditContentRecyclerAdapter extends PagedListAdapter<SubmissionObj, SubredditContentRecyclerAdapter.ItemViewHolder> {
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
            // empty subreddit, nothing to display
            if (is404) {
                return;
            }
            SubmissionObj item = (getItem(position) == null ? new SubmissionObj(true) : getItem(position));

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
            String thumbnail = Constants.THUMBNAIL_NOT_FOUND;
            item.setSubmissionType(Utils.getSubmissionType(item.getUrl()));

            if (item != null && !item.isSelfPost()) {
                is404 = false;
                // Imgur
                if (item.getDomain().contains("imgur")) {
                    // Check if submission type is null. This will happen if the item's URL is
                    // to a non-direct image/gif link such as https://imgur.com/qTadRtq
                    if (item.getSubmissionType() == null) {
                        String imgurHash = Utils.getImgurHash(item.getUrl());
                        // Async call to Imgur API
                        fixIndirectImgurUrl(item, imgurHash);
                    }

                    // imgur image
                    if (item.getSubmissionType() == Constants.SubmissionType.IMAGE) {
                    }
                    // imgur gif
                    else if (item.getSubmissionType() == Constants.SubmissionType.GIF) {
                        // Assume we're given .gifv link. Need to fetch .mp4 link from Imgur API
                        String imgurHash = Utils.getImgurHash(item.getUrl());
                        getMp4LinkImgur(item, imgurHash);
                    }
                    // We assume item will always have a thumbnail in an image format
                    thumbnail = item.getThumbnail();
                    if (mDisplayDomainIcon) {
                        holder.thumbnailDomainIcon.setBackground(getResources().getDrawable(R.drawable.ic_imgur_i_black_bg));
                        holder.thumbnailDomainIcon.setVisibility(View.VISIBLE);
                    }
                }
                //v.redd.it
                else if ("v.redd.it".equalsIgnoreCase(item.getDomain())) {
                    Log.e("VIDEO_DOMAIN_FOUND", " Found v.redd.it link. Not working yet.");
                    thumbnail = item.getThumbnail();
                    if (mDisplayDomainIcon) {
                        holder.thumbnailDomainIcon.setBackground(getResources().getDrawable(R.drawable.ic_reddit_blue_circle));
                        holder.thumbnailDomainIcon.setVisibility(View.VISIBLE);
                    }
                }

                // i.redd.it
                else if (item.getDomain().contains("i.redd.it")) {
                    thumbnail = item.getThumbnail();
                    if (mDisplayDomainIcon) {
                        holder.thumbnailDomainIcon.setBackground(getResources().getDrawable(R.drawable.ic_reddit_circle_orange));
                        holder.thumbnailDomainIcon.setVisibility(View.VISIBLE);
                    }
                }
                //gfycat
                else if (item.getDomain().contains("gfycat")) {
                    // We're given a URL in this format: //https://gfycat.com/SpitefulGoldenAracari
                    //extract gfycat ID (looks like:SpitefulGoldenAracari)
                    String gfycatHash = Utils.getGfycatHash(item.getUrl());
                    // get Gfycat .mp4 "clean url"
                    getGfycat(gfycatHash,item);
                    // Assume all Gfycat links are of submission type GIF
                    item.setSubmissionType(Constants.SubmissionType.GIF);
                    thumbnail = item.getThumbnail();
                    if (mDisplayDomainIcon) {
                        holder.thumbnailDomainIcon.setBackground(getResources().getDrawable(R.drawable.ic_gfycat_circle_blue));
                        holder.thumbnailDomainIcon.setVisibility(View.VISIBLE);
                    }
                }
                //youtube
                else if (item.getDomain().contains("youtube")) {
                    Log.e("VIDEO_DOMAIN_FOUND", " Found YOUTUBE link. Not working yet.");
                    if (mDisplayDomainIcon) {
                        holder.thumbnailDomainIcon.setBackground(getResources().getDrawable(R.drawable.ic_youtube_red));
                        holder.thumbnailDomainIcon.setVisibility(View.VISIBLE);
                    }
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

                /* Display file type icons if option is enabled through settings*/
                if (item.getSubmissionType() != null && mDisplayFiletypeIcons) {
                    switch (item.getSubmissionType()) {
                        case IMAGE:
                            holder.thumbnailFiletypeIcon.setBackground(
                                    getResources().getDrawable(R.drawable.ic_filetype_image));
                            holder.thumbnailFiletypeIcon.setVisibility(View.VISIBLE);
                            break;
                        case GIF:
                            holder.thumbnailFiletypeIcon.setBackground(
                                    getResources().getDrawable(R.drawable.ic_filetype_gif));
                            holder.thumbnailFiletypeIcon.setVisibility(View.VISIBLE);
                            break;
                        case VIDEO:
                            holder.thumbnailFiletypeIcon.setBackground(
                                    getResources().getDrawable(R.drawable.ic_filetype_video));
                            holder.thumbnailFiletypeIcon.setVisibility(View.VISIBLE);
                            break;
                        default:
                            break;
                    }
                }

                // Display NSFW icon on top right if enabled through settings
                if (mDisplayNSFWIcon && item.isNSFW()) {
                    holder.thumbnailNSFWIcon.setVisibility(View.VISIBLE);
                } else {
                    holder.thumbnailNSFWIcon.setVisibility(View.GONE);
                }

                // Finally load thumbnail into recyclerview
                if (item.isNSFW() && mHideNSFWThumbs) {
                    GlideApp.load(getResources().getDrawable(R.drawable.ic_reddit_nsfw_dark))
                            .apply(new RequestOptions().centerInside().diskCacheStrategy(DiskCacheStrategy.ALL))
                            .into(holder.thumbnailImageView);
                } else {
                    GlideApp.load(thumbnail)
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
                                        .load(item.getCleanedUrl() != null ? item.getCleanedUrl() : item.getUrl())
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
                            mHoverPreviewSubredditLarge.setText("/r/" + item.getSubreddit());
                            setupPreviewer(item);
                            if (item.getSubmissionType() == Constants.SubmissionType.IMAGE) {
                                GlideApp.load(item.getCleanedUrl() != null ? item.getCleanedUrl() : item.getUrl())
                                        .listener(new ProgressBarRequestListener(mProgressBar))
                                        /*.apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))*/
                                        .into(mHoverImagePreviewLarge);
                                // make sure the gif/video player isn't showing
                            } else if (item.getSubmissionType() == Constants.SubmissionType.GIF) {
                                /*if(item.getDomain().contains("gfycat")){
                                    mHoverPreviewGfycatLarge.setShouldLoadPreview(true);
                                    //mHoverPreviewGfycatLarge.setOnStartAnimationListener
                                    //mHoverPreviewGfycatLarge.setupGfycat(gfycatObject);
                                    mHoverPreviewGfycatLarge.play();
                                }*/
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
            ImageView thumbnailDomainIcon;
            ImageView thumbnailFiletypeIcon;
            ImageView thumbnailNSFWIcon;

            public ItemViewHolder(View itemView) {
                super(itemView);
                thumbnailImageView = (ImageView) itemView.findViewById(R.id.thumbnail);
                thumbnailDomainIcon = (ImageView) itemView.findViewById(R.id.thumbnail_domain_icon);
                thumbnailFiletypeIcon = (ImageView) itemView.findViewById(R.id.thumbnail_filetype_icon);
                thumbnailNSFWIcon = (ImageView) itemView.findViewById(R.id.thumbnail_nsfw_icon);
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

    private void openSubmissionViewer(SubmissionObj submission) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment mediaDisplayerFragment = FragmentSubmissionViewer.newInstance();
        Bundle args = new Bundle();
        args.putSerializable(Constants.EXTRA_SUBMISSION_OBJ, submission);
        mediaDisplayerFragment.setArguments(args);

        mediaDisplayerFragment.setTargetFragment(FragmentHome.this, KEY_INTENT_GOTO_SUBMISSIONVIEWER);

        int parentContainerId = ((ViewGroup) getView().getParent()).getId();
        ft.add(parentContainerId, mediaDisplayerFragment/*, Constants.TAG_FRAG_MEDIA_DISPLAY*/);
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

    // Clean up any lingering views, media players, sharedprefs_settings, etc
    private void switchOrLogoutCleanup(String newCurrUser) {
        mDrawerLayout.closeDrawer(mNavigationViewLeft);
        // Update most recent user to new user
        sharedprefs_settings.edit().putString(Constants.MOST_RECENT_USER, newCurrUser).apply();

        // start our subreddit stack over cause we're starting over again
        while (!App.getMoxieInfoObj().getmSubredditStack().isEmpty()) {
            App.getMoxieInfoObj().getmSubredditStack().pop();
        }

        //remove any sorting we had...null is ok :^)
        App.getMoxieInfoObj().setmSortBy(null);
        App.getMoxieInfoObj().setmTimePeriod(null);
        refresh(true);
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
        sharedprefs_settings.edit().putString(Constants.MOST_RECENT_USER, username).commit();
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


    /*************************Async network tasks *******************************************/

    //* Set's our reddit client to userless and refreshes Home on completion*//*
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


    /***********[GFYCAT SPECIFIC FUNCTIONS] ***************/
    /****************************************************/
    /****************************************************/

    private void getGfycat(String gfycatHash, SubmissionObj item){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.BASE_URL_GFYCAT)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GfycatAPI gfycatAPI = retrofit.create(GfycatAPI.class);
        gfycatAPI.getFeed(gfycatHash);
        Call<GfycatWrapper> call = gfycatAPI.getFeed(gfycatHash);
        call.enqueue(new Callback<GfycatWrapper>() {
            @Override
            public void onResponse(Call<GfycatWrapper> call, Response<GfycatWrapper> response) {
                //Log.d(TAG, "onResponse: feed: " + response.body().toString());
                Log.d(TAG, "onResponse: Server Response: " + response.toString());

                GfyItem gfyItem = response.body().getGfyItem();
                item.setCleanedUrl(gfyItem.getMobileUrl());
            }

            @Override
            public void onFailure(Call<GfycatWrapper> call, Throwable t) {
                Log.e(TAG, "onFailure: Unable to retrieve RSS: " + t.getMessage() );
                //Toast.makeText(MainActivity.this, "An Error Occured", Toast.LENGTH_SHORT).show();

            }
        });
    }



    /***********[IMGUR SPECIFIC FUNCTIONS] ***************/
    /****************************************************/
    /****************************************************/

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
                        if (imgurSubmissionData.getMp4() == null) {
                            item.setCleanedUrl(imgurSubmissionData.getLink());
                            item.setSubmissionType(Constants.SubmissionType.IMAGE);
                        }
                        // gif
                        else {
                            item.setCleanedUrl(imgurSubmissionData.getMp4());
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

        Given a imgur link ending with .gif/gifv such as https://i.imgur.com/4RxPsWI.gifv,
        retrieve corresponding .mp4 link: https://i.imgur.com/4RxPsWI.mp4 and set item's
        URL to new .mp4 link.
     */
    private void getMp4LinkImgur(SubmissionObj item, String imgurHash) {
        ImgurClient imgurClient = new ImgurClient();
        //UjpwIRe is a 404 gifv hash
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
                        Log.e("ERROR_IMGUR_FETCH", "Failed to retrieve imgur hash: " + imgurHash);
                        e.printStackTrace();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

}
