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
import android.graphics.Point;
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
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.sometimestwo.moxie.Model.SubmissionObj;
import com.sometimestwo.moxie.Utils.Constants;
import com.sometimestwo.moxie.Utils.Helpers;

import net.dean.jraw.RedditClient;

import java.util.Arrays;

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

    // settings prefs
    SharedPreferences prefs;
    private boolean mIsLoggedIn = false;
    private boolean mPrefsAllowNSFW = false;
    private boolean mAllowImagePreview = false;
    // private boolean mAllowGifPreview = false;
    private int mPreviewSize = 0; // 0 small, 1 large

    // hover view
    private ImageView mHoverView;
    private RelativeLayout mHoverViewContainer;
    private TextView mHoverViewTitle;

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

        /* hover view*/
        mHoverViewContainer = (RelativeLayout) v.findViewById(R.id.hover_view_container);
        mHoverViewTitle = (TextView) v.findViewById(R.id.hover_view_title);
        mHoverView = (ImageView) v.findViewById(R.id.hover_view);
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
        mAllowImagePreview = prefs.getString(Constants.KEY_IMAGE_PREVIEW, Constants.SETTINGS_NO).equalsIgnoreCase(Constants.SETTINGS_YES);
        mAllowImagePreview = prefs.getString(Constants.KEY_GIF_PREVIEW, Constants.SETTINGS_NO).equalsIgnoreCase(Constants.SETTINGS_YES);
        mPreviewSize = prefs.getString(Constants.KEY_PREVIEW_SIZE, Constants.SETTINGS_PREVIEW_SIZE_SMALL).equalsIgnoreCase(Constants.SETTINGS_PREVIEW_SIZE_SMALL) ? 0 : 1;
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
            if (item.getAuthor().equalsIgnoreCase("deweysizemore")) {
                int a = 2;
            }
            //ignore any items that do not have thumbnail do display
            if (item != null && !item.isSelfPost()) {
                String thumbnailUrl = item.getThumbnail();
                String postUrl = item.getUrl();

                // Imgur
                if (item.getDomain().contains("imgur")) {
                    // find extension
                    String extension = Helpers.getFileExtensionFromPostUrl(postUrl);

                    // Submission links to non-direct image link such as "https://imgur.com/qTadRtq?r"
                    // Need to append "jpg": https://imgur.com/qTadRtq?r.jpg
                    if (!Arrays.asList(Constants.VALID_MEDIA_EXTENSION).contains(extension)) {
                        StringBuilder sb = new StringBuilder(item.getUrl());
                        sb.append(".jpg");
                        item.setUrl(sb.toString());

                        // While we're here, make sure thumbnail is valid image otherwise override
                        // it with the post URL. Glide will resize it to thumbnail (preformance hit)
                        if (!Arrays.asList(Constants.VALID_MEDIA_EXTENSION).contains(Helpers.getFileExtensionFromPostUrl(item.getThumbnail()))) {
                            item.setThumbnail(sb.toString());
                        }
                        extension = "jpg";
                    }
                    // image
                    if (Arrays.asList(Constants.VALID_IMAGE_EXTENSION).contains(extension)) {
                        GlideApp
                                .load(thumbnailUrl)
                                .apply(new RequestOptions()
                                        .centerCrop()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL))
                                .into(holder.thumbnailImageView);
                    }

                    // gif
                    if (Arrays.asList(Constants.VALID_GIF_EXTENSION).contains(extension)) {
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

                            GlideApp
                                    .load(item.getUrl())
                                    .apply(new RequestOptions()
                                            .diskCacheStrategy(DiskCacheStrategy.ALL))
                                    .into(mHoverView);
                            mHoverViewTitle.setText(item.getTitle());

                           // float scale = getResources().getDisplayMetrics().density;
                            //int dpAsPixels = (int) (mHoverViewTitle.getHeight()*scale + 0.5f);
                           // mHoverViewContainer.(0,dpAsPixels,0,0);
                            mHoverViewContainer.setVisibility(View.VISIBLE);
                            //mHoverView.setVisibility(View.VISIBLE);
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
                                    mHoverViewTitle.setText("");
                                    mHoverViewContainer.setVisibility(View.GONE);
                                    //mHoverView.setVisibility(View.GONE);
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


}
