package com.sometimestwo.moxie;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;

import com.sometimestwo.moxie.Utils.Constants;
import com.sometimestwo.moxie.Utils.Utils;


/*
        TODOS:
            - toolbar random button
            - animated gifs
            - pinch zoomies
            - double tap to upvote
            - long press to show title of post
            - media viewer image options (download, etc)
            - view pager
            - invalid subreddit
            settings options:
            - browse mode (no comments, upvoting , etc) for lurkers
            - hide progress bar on exoplayer

            layout issues:
            - centering very tall image in large hover
            - hide toolbar on large hover preview
 */
public class ActivityHome extends AppCompatActivity implements HomeEventListener,
        OnCloseClickEventListener,
        Fragment404.Fragment404EventListener{

    private final String TAG = ActivityHome.class.getSimpleName();
    private SharedPreferences prefs_settings;

    // Permissions we'll need to make use of
    private boolean mAllowCloseOnClick;

    //screen size metrics for flexibility in displaying dialogs
    private DisplayMetrics mDisplayMetrics;
    private int mScreenWidth;
    private int mScreenHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;

        prefs_settings = this.getSharedPreferences(Constants.KEY_GET_PREFS_SETTINGS, Context.MODE_PRIVATE);
        mAllowCloseOnClick = prefs_settings.getBoolean(Constants.SETTINGS_ALLOW_BIGDISPLAY_CLOSE_CLICK, false);

        loadReddit(false);
    }

    private void loadReddit(boolean invalidateData ) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragment = FragmentHome.newInstance();
        Bundle args = new Bundle();
        args.putBoolean(Constants.ARGS_INVALIDATE_DATASOURCE, invalidateData);

        fragment.setArguments(args);
        ft.add(R.id.fragment_container_home, fragment, Constants.TAG_FRAG_HOME);
        ft.commit();
    }

    // Actually removes current fragment and creates new one
    protected void refreshFragment(String fragmentTag, boolean invalidateData ) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
        final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        try {
            ft.remove(fragment).commit();
            loadReddit(invalidateData);
        } catch (NullPointerException e) {
            throw new NullPointerException(this.toString()
                    + ". Could not refresh fragment! Probably provided incorrect fragment tag. " +
                    " Fragment tag provided: " + fragmentTag);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.navigation_menu, menu);
        getMenuInflater().inflate(R.menu.menu_default_header, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        // Need to make sure user is authenticated
        if(!App.getAccountHelper().isAuthenticated()){
            new Utils.FetchAuthenticatedUserTask( new OnRedditUserReadyListener() {
                @Override
                public void redditUserAuthenticated() {
                    ActivityHome.super.onStart();
                }
            }).execute();
        }
        else{
            super.onStart();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    public void onBackPressed() {
        // Back button should close nav view drawers if they're open (on either side)
        DrawerLayout navViewDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(isNavViewOpen(navViewDrawer)){
            navViewDrawer.closeDrawers();
        }
        // Back arrow should open left nav menu drawer if we're not currently in any fragments
        else if((getSupportFragmentManager().getBackStackEntryCount() == 0)
                && !navViewDrawer.isDrawerOpen(GravityCompat.START)){
            navViewDrawer.openDrawer(GravityCompat.START);
        }
        // Not at the home screen, pop back stack instead of closing activity
        else if(getSupportFragmentManager().getBackStackEntryCount() > 0){
            getSupportFragmentManager().popBackStack();
        }
    }

    /* Hacky workaround for differentiating between hardware back button and menu back button*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Back button should close nav view drawers if they're open (on either side)
        DrawerLayout navViewDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(getSupportFragmentManager().getBackStackEntryCount() > 0 || isNavViewOpen(navViewDrawer)){
                onBackPressed();
            }
            // Confirm exit app
            else{
                AlertDialog.Builder builder = new AlertDialog.Builder(ActivityHome.this, R.style.TransparentDialog);
                builder.setTitle("Confirm exit");
                builder.setMessage("Really exit app?");
                builder.setIcon(R.drawable.ic_white_exclamation);

                builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ActivityHome.this.exitApp();
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
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.INTENT_SETTINGS) {
            if (resultCode == RESULT_OK) {
                //refreshFragment(Constants.TAG_FRAG_HOME, false);
                //Log.e(TAG, "Returned from settings activity");
            }
        }
    }


    private void exitApp() {
        super.onBackPressed();
    }

    // A Navigation View is a menu that slides from the left or right
    private boolean isNavViewOpen(DrawerLayout drawer){
        return drawer.isDrawerOpen(GravityCompat.START) || drawer.isDrawerOpen(GravityCompat.END);
    }

    /*
        Interface implementations
     */
    @Override
    public void openSettings() {
        Intent settingsIntent = new Intent(this, ActivitySettings.class);
        //settingsIntent.putExtra()
        startActivityForResult(settingsIntent, Constants.INTENT_SETTINGS);
    }

    @Override
    public void refreshFeed( boolean invalidateData) {
        // ignore targetSubreddit. It's only here for the sake of ActivitySubredditViewer
        // We'll always want to refresh home when we're in ActivityHome, no target needed
        refreshFragment(Constants.TAG_FRAG_HOME, invalidateData);
    }


    @Override
    public void menuGoBack() {
        onBackPressed();
    }

    // hosting a 404 page as the top of the backstack
    @Override
    public void set404(boolean is404) {
        //this.mIs404 = is404;
    }

    @Override
    public void onCloseClickDetected() {
        if(mAllowCloseOnClick){
            getSupportFragmentManager().popBackStack();
        }
    }

    // Called on Retry button click
    @Override
    public void refresh404(String tag) {
        refreshFragment(tag, true);
    }

    // P
    @Override
    public void startOver() {
        while(getSupportFragmentManager().getBackStackEntryCount() > 0 ){
            getSupportFragmentManager().popBackStack();
        }
        refreshFeed(true);
    }
}
