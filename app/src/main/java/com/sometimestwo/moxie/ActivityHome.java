package com.sometimestwo.moxie;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;

import com.sometimestwo.moxie.Utils.Constants;


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
public class ActivityHome extends AppCompatActivity implements HomeEventListener, OnCloseClickEventListener{

    private final String TAG = ActivityHome.class.getSimpleName();
    private SharedPreferences prefs_settings;

    // False if user has navigated to a submission or different subreddit.
    // This allows us to know if we should handle onBackPressed() or not
    private boolean isHome = true;

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
        // User can become unauthenticated when inactive(tabbed out of app) for a long period (1hour).
        String mostRecentUser = prefs_settings.getString(Constants.MOST_RECENT_USER, Constants.USERNAME_USERLESS);
        if(!App.getAccountHelper().isAuthenticated()){
            if (!Constants.USERNAME_USERLESS.equalsIgnoreCase(mostRecentUser)) {
                App.getAccountHelper().switchToUser(mostRecentUser);
            }
            else {
                App.getAccountHelper().switchToUserless();
            }
        }

        super.onResume();
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
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer.isDrawerOpen(GravityCompat.START) || drawer.isDrawerOpen(GravityCompat.END) ){
            drawer.closeDrawers();
        }
        // Back arrow should open left nav menu drawer if we're not currently in any fragments
        else if((getSupportFragmentManager().getBackStackEntryCount() == 0)
                && !drawer.isDrawerOpen(GravityCompat.START)){
            drawer.openDrawer(GravityCompat.START);
        }
        // Not at the home screen, pop back stack instead of closing activity
        else if(getSupportFragmentManager().getBackStackEntryCount() > 0){
            getSupportFragmentManager().popBackStack();
        }
    }

    public void exitApp() {
        super.onBackPressed();
    }

    /* Hacky workaround for differentiating between hardware back button and menu back button*/
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if(getSupportFragmentManager().getBackStackEntryCount() > 0){
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
    public void isHome(boolean isHome) {
        this.isHome = isHome;
    }

    @Override
    public void menuGoBack() {
        onBackPressed();
    }

    @Override
    public void set404(boolean is404) {
        // This is added for the sake of being useful in ActivitySubredditViewer
    }

    @Override
    public void onCloseClickDetected() {
        if(mAllowCloseOnClick){
            getSupportFragmentManager().popBackStack();
        }
    }
}
